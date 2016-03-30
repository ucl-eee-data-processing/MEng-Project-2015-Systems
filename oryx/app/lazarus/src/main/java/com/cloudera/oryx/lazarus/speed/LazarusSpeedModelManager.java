/*
 * Copyright (c) 2014, Cloudera, Inc. All Rights Reserved.
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
package com.cloudera.oryx.lazarus.speed;
// Timothy's Imports
import com.cloudera.oryx.lazarus.speed.LazarusSpeedUtility;
import com.cloudera.oryx.lazarus.serving.LazarusServingUtility;
// End of Timothy's Imports
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.apache.hadoop.mapreduce.Job;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import com.cloudera.oryx.api.KeyMessage;
import com.cloudera.oryx.api.speed.SpeedModelManager;
import com.cloudera.oryx.lazarus.batch.ExampleBatchLayerUpdate;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.rdd.RDD;
import org.apache.spark.mllib.regression.LinearRegressionWithSGD;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scala.Tuple2;

/**
 * Also counts and emits counts of number of distinct words that occur with
 * words. Listens for updates from the Batch Layer, which give the current
 * correct count at its last run. Updates these counts approximately in response
 * to the same data stream that the Batch Layer sees, but assumes all words seen
 * are new and distinct, which is only approximately true. Emits updates of the
 * form "word,count".
 */
public final class LazarusSpeedModelManager implements SpeedModelManager<String, String, String>, Serializable {
    
    final RegressionModelBuilder rmb = new RegressionModelBuilder();
    final DataPreProcessor dpp = new DataPreProcessor();
    
    private final Map<String, double[] > modelWeights = LazarusSpeedUtility.initializeWeights();   
    private final List<String> updates = Arrays.asList(new String[48]);

    //Updates the Model in Memory
    @Override
    public void consume(Iterator<KeyMessage<String, String>> updateIterator,
            Configuration hadoopConf) throws IOException {
        
        while (updateIterator.hasNext()) {
            KeyMessage<String, String> km = updateIterator.next();
            String key = km.getKey();
            String message = km.getMessage();
            int modelIndex = updates.indexOf(message);
            switch (key) {
                case "MODEL":
                    // Functionality to ready from already Existing Models in Kafka
                    break;
                case "UP":
                    if (message != null && modelIndex != -1 ){
                        String timeStamp = LazarusServingUtility.indexToTime(modelIndex);
                        double[] weights = LazarusServingUtility.stringToWeights(message);
                        modelWeights.put(timeStamp, weights);
                        System.out.println(Arrays.toString(weights));                        
                        
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown key " + key);
            }
        }
    }
    
    @Override
    public Iterable<String> buildUpdates(JavaPairRDD<String, String> newData) {
        System.out.println("Consuming Input Data ..  UTKU.............................");
        System.out.println("Consuming Input Data ...............................");
        System.out.println("Consuming Input Data .....uTUKU..........................");
        System.out.println(newData + "  THIS IS NEW DATA!!!!");
        System.out.println("Iterating through the consumed Data ...............................");
        System.out.println(newData.collect() + "   THIS IS COLLECT ---- 22222 !!!!!!!!!");
        System.out.println("Finished Iterating Through the Data...............................");

        //returns an javaRDD of labeled points after preprocessing the data
        JavaRDD<LabeledPoint> rdd_records;
        rdd_records = newData.values().map(
            new Function<String, LabeledPoint>() {
            @Override
            public LabeledPoint call(String line) throws Exception {
            return dpp.getLabeledPoint(line);
            }
        });

        String time = LazarusSpeedUtility.twentyFourHourTime(rdd_records.first().features().apply(0));
        System.out.println(time + "<<<<<<<<<<<<<<<<<<<TIIIIIIIIIIIIIME!!!!");
        System.out.println("Retrieving the Corres Weights ...............");
        System.out.println(Arrays.toString(modelWeights.get(time)));
       
       LinearRegressionModel model = rmb.buildModel(rdd_records, modelWeights.get(time));
       //rmb.thetaMap.put(time, rmb.getWeights(model));
       String stringWeights = LazarusServingUtility.weightsToString(rmb.getWeights(model));
       int timeIndex = LazarusServingUtility.timeToIndex(time);
       updates.set(timeIndex,stringWeights);
       return updates;
    }
    
    @Override
    public void close() {
       throw new UnsupportedOperationException("Not supported yet."); //To 
    }

}
