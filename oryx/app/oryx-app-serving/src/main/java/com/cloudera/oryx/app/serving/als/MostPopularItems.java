/*
 * Copyright (c) 2014, Cloudera and Intel, Inc. All Rights Reserved.
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

import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import com.cloudera.oryx.app.als.Rescorer;
import com.cloudera.oryx.app.als.RescorerProvider;
import com.cloudera.oryx.app.serving.OryxServingException;
import com.cloudera.oryx.common.collection.Pair;
import com.cloudera.oryx.common.collection.PairComparators;
import com.cloudera.oryx.app.serving.CSVMessageBodyWriter;
import com.cloudera.oryx.app.serving.IDCount;
import com.cloudera.oryx.app.serving.als.model.ALSServingModel;

/**
 * <p>Responds to a GET request to
 * {@code /mostPopularItems(?howMany=n)(&offset=o)(&rescorerParams=...)}</p>
 *
 * <p>Results are items that the most users have interacted with, as item and count pairs.</p>
 *
 * <p>{@code howMany} and {@code offset} behavior are as in {@link Recommend}. Output
 * is also the same, except that item IDs are returned with integer counts rather than
 * scores.</p>
 *
 * @see MostActiveUsers
 */
@Singleton
@Path("/mostPopularItems")
public final class MostPopularItems extends AbstractALSResource {

  @GET
  @Produces({MediaType.TEXT_PLAIN, CSVMessageBodyWriter.TEXT_CSV, MediaType.APPLICATION_JSON})
  public List<IDCount> get(@DefaultValue("10") @QueryParam("howMany") int howMany,
                           @DefaultValue("0") @QueryParam("offset") int offset,
                           @QueryParam("rescorerParams") List<String> rescorerParams)
      throws OryxServingException {
    ALSServingModel model = getALSServingModel();
    RescorerProvider rescorerProvider = model.getRescorerProvider();
    Rescorer rescorer = null;
    if (rescorerProvider != null) {
      rescorer = rescorerProvider.getMostPopularItemsRescorer(rescorerParams);
    }
    return mapTopCountsToIDCounts(model.getItemCounts(), howMany, offset, rescorer);
  }

  static List<IDCount> mapTopCountsToIDCounts(Map<String,Integer> counts,
                                              int howMany,
                                              int offset,
                                              final Rescorer rescorer) {
    Iterable<Pair<String,Integer>> countPairs = Iterables.transform(
        counts.entrySet(),
        new Function<Map.Entry<String,Integer>, Pair<String,Integer>>() {
          @Override
          public Pair<String,Integer> apply(Map.Entry<String,Integer> input) {
            return new Pair<>(input.getKey(), input.getValue());
          }
        });

    if (rescorer != null) {
      countPairs = Iterables.filter(countPairs, new Predicate<Pair<String,Integer>>() {
        @Override
        public boolean apply(Pair<String, Integer> input) {
          return !rescorer.isFiltered(input.getFirst());
        }
      });
    }

    List<Pair<String,Integer>> allTopCountPairs =
        Ordering.from(PairComparators.<Integer>bySecond()).greatestOf(countPairs, howMany + offset);
    List<Pair<String,Integer>> topCountPairs =
        selectedSublist(allTopCountPairs, howMany, offset);

    return Lists.transform(
        topCountPairs,
        new Function<Pair<String, Integer>, IDCount>() {
          @Override
          public IDCount apply(Pair<String,Integer> idCount) {
            return new IDCount(idCount.getFirst(), idCount.getSecond());
          }
        });
  }

}
