/*
 * Copyright (c) 2015, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.cloudera.oryx.app.serving.als;

import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.openhft.koloboke.function.ObjDoubleToDoubleFunction;
import net.openhft.koloboke.function.Predicate;

import com.cloudera.oryx.app.als.Rescorer;
import com.cloudera.oryx.app.als.RescorerProvider;
import com.cloudera.oryx.app.serving.CSVMessageBodyWriter;
import com.cloudera.oryx.app.serving.IDValue;
import com.cloudera.oryx.app.serving.OryxServingException;
import com.cloudera.oryx.app.serving.als.model.ALSServingModel;
import com.cloudera.oryx.common.collection.NotContainsPredicate;
import com.cloudera.oryx.common.collection.Pair;
import com.cloudera.oryx.common.collection.Predicates;

/**
 * <p>Responds to a GET request to
 * {@code /recommendWithContext/[userID]/([itemID1(=value1)]/...)
 * (?howMany=n)(&offset=o)(&considerKnownItems=c)(&rescorerParams=...)}
 * </p>
 *
 * <p>This endpoint operates like a combination of {@code /recommend} and {@code /recommendToAnonymous}.
 * It creates recommendations for a user, but modifies the recommendation as if the user also
 * interacted with a given set of items. This creates no model updates. It's useful for recommending
 * in the context of some possibly temporary interactions, like products in a basket.</p>
 *
 * <p>{@code howMany}, {@code considerKnownItems} and {@code offset} behavior, and output, are as in
 * {@link Recommend}.</p>
 */
@Singleton
@Path("/recommendWithContext")
public final class RecommendWithContext extends AbstractALSResource {

  @GET
  @Path("{userID}/{itemID : .*}")
  @Produces({MediaType.TEXT_PLAIN, CSVMessageBodyWriter.TEXT_CSV, MediaType.APPLICATION_JSON})
  public List<IDValue> get(
      @PathParam("userID") String userID,
      @PathParam("itemID") List<PathSegment> pathSegments,
      @DefaultValue("10") @QueryParam("howMany") int howMany,
      @DefaultValue("0") @QueryParam("offset") int offset,
      @DefaultValue("false") @QueryParam("considerKnownItems") boolean considerKnownItems,
      @QueryParam("rescorerParams") List<String> rescorerParams) throws OryxServingException {

    check(howMany > 0, "howMany must be positive");
    check(offset >= 0, "offset must be nonnegative");

    ALSServingModel model = getALSServingModel();
    List<Pair<String,Double>> parsedPathSegments = EstimateForAnonymous.parsePathSegments(pathSegments);
    float[] userVector = model.getUserVector(userID);
    checkExists(userVector != null, userID);

    float[] tempUserVector = EstimateForAnonymous.buildTemporaryUserVector(model, parsedPathSegments, userVector);

    List<String> knownItems = new ArrayList<>();
    for (Pair<String,?> itemValue : parsedPathSegments) {
      knownItems.add(itemValue.getFirst());
    }
    if (!considerKnownItems) {
      knownItems.addAll(model.getKnownItems(userID));
    }

    Predicate<String> allowedFn = new NotContainsPredicate<>(new HashSet<>(knownItems));
    ObjDoubleToDoubleFunction<String> rescoreFn = null;
    RescorerProvider rescorerProvider = getALSServingModel().getRescorerProvider();
    if (rescorerProvider != null) {
      Rescorer rescorer = rescorerProvider.getRecommendRescorer(Collections.singletonList(userID),
                                                                rescorerParams);
      if (rescorer != null) {
        allowedFn = Predicates.and(allowedFn, buildRescorerPredicate(rescorer));
        rescoreFn = buildRescoreFn(rescorer);
      }
    }

    List<Pair<String,Double>> topIDDots = model.topN(
        new DotsFunction(tempUserVector),
        rescoreFn,
        howMany + offset,
        allowedFn);
    return toIDValueResponse(topIDDots, howMany, offset);
  }

}
