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
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import com.cloudera.oryx.lazarus.speed.TimeProcessor;
import com.cloudera.oryx.lazarus.speed.LazarusSpeedUtility;
import com.cloudera.oryx.api.serving.OryxResource;
import java.util.Vector;

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
  public Map<String, Double> get(@PathParam("start") String start, @PathParam("end") String end) {
    Map<String, double[] > modelWeights = getModel().getModelWeights();
    for (String key : modelWeights.keySet()){
        if (key != null){
          System.out.println(key + " " + Arrays.toString(modelWeights.get(key)));
        }
    } 
    System.out.println("Printing out Generated Time Stampss----------------------");
    Map<String, Long> stringTime = LazarusServingUtility.stringUnixTime(start,end);
    for (String key : stringTime.keySet()){
        if (key != null){
          System.out.println(key + " " + stringTime.get(key).toString());
        }
    } 
    System.out.println("Printing out Scaled Features ----------------------");
    Map<String, ArrayList<Double>> scaledTestData = 
            LazarusServingUtility.scaledTestDataSet(start,end);
    
    for (String key : scaledTestData.keySet()){
        if (key != null){
          System.out.println(key + " " + scaledTestData.get(key).toString());
        }
    } 
    System.out.println("Predicted Results ...................................");
    Map<String, Double> energyUsage = predictEnergyUsage(modelWeights, scaledTestData);
    for (String key : energyUsage.keySet()){
          System.out.println(key + " " + energyUsage.get(key).toString());
    } 
    return energyUsage;
  }
  
  private LazarusServingModel getModel() {
    @SuppressWarnings("unchecked")
    LazarusServingModel model = (LazarusServingModel) getServingModelManager().getModel();
    return model;
  }
  
  private static Map<String, Double> predictEnergyUsage(Map<String, double[] > modelWeights,
                                                        Map<String, ArrayList<Double>> scaledTestData){
    Map<String, Double>  predictedValues = new HashMap<String, Double>();
    for(String key : scaledTestData.keySet()){
        String time = key.split("T")[1];
        if(!modelWeights.containsKey(time)){
            predictedValues.put(key, Double.NaN);
        }else{
            double[] interceptWeights = modelWeights.get(time);
            double[] weights = Arrays.copyOfRange(interceptWeights,2,5);
            ArrayList<Double> scaledData = scaledTestData.get(key);
            double dotProduct = interceptWeights[1];
            for (int i=0; i< weights.length; i++){
                dotProduct = dotProduct + weights[i]* scaledData.get(i).doubleValue();
            }
            predictedValues.put(key,new Double(dotProduct));
        }
    }
    return predictedValues;
  }
  
}