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

package com.cloudera.oryx.lazarus.serving;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Arrays;
import com.cloudera.oryx.api.serving.OryxResource;

/**
 * Responds to a GET request to {@code /distinct}. Returns all distinct words and their count.
 * Responds to a GET request to {@code /distinct/[word]} as well to get the count for one word.
 */
@Path("/predict")
public final class Predict extends OryxResource {

  @GET
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  public Map<String,Integer> get() {
     LazarusServingUtility servingUtility = new LazarusServingUtility();
     Map<String,Integer> predictedData = servingUtility.predictedDummy("2016-01-01T09:30","2016-01-01T23:30");
     return predictedData;
  }
  
  @GET
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @Path("/{start}/{end}")
  public Map<String,Integer> get(@PathParam("start") String start, @PathParam("end") String end) {
    LazarusServingUtility servingUtility = new LazarusServingUtility();
    Map<String,Integer> predictedData = servingUtility.predictedDummy(start,end);
    ArrayList<Double> tmp = servingUtility.weatherData("42.44","-76.53");
    double [] weights = {2.689,56.7777,89.0034}; 
    String str = LazarusServingUtility.weightsToString(weights);
    System.out.println(str);
    double [] strDouble = LazarusServingUtility.stringToWeights("2.689-56.7777-89.0034");
    System.out.println(Arrays.toString(strDouble));
    return predictedData;
  }
  
  @GET
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @Path("{word}")
  public int get(@PathParam("word") String word) {
    Integer count = getModel().getWords().get(word);
    System.out.println("Model");
    System.out.println(getModel());
    return count == null ? 0 : count;
  }
  private LazarusServingModel getModel() {
    @SuppressWarnings("unchecked")
    LazarusServingModel model = (LazarusServingModel) getServingModelManager().getModel();
    return model;
  }

}