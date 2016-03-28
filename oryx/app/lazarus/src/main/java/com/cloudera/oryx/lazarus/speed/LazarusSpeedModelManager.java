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

    private final Map<String, Integer> distinctOtherWords
            = Collections.synchronizedMap(new HashMap<String, Integer>());
    //Need to declare global Params for Theta
    private final Map<String, Integer> totalEnergyConsumed
            = Collections.synchronizedMap(new HashMap<String, Integer>());
   private final Map<String, double[] > modelWeights = 
           Collections.synchronizedMap(new HashMap<String, double[] >());

    //Updates the Model in Memory
    @Override
    public void consume(Iterator<KeyMessage<String, String>> updateIterator,
            Configuration hadoopConf) throws IOException {
        //If the model in Memory is empty
        if(!updateIterator.hasNext()){
            for(int i = 0; i < 48; i ++ ){
                String timeStamp = LazarusServingUtility.indexToTime(i);
                modelWeights.put(timeStamp, new double []{0.0, 0.0, 0.0});
            }
        }
        System.out.println(Arrays.toString(modelWeights.entrySet().toArray()));
        System.out.println("Array Length ---------->");
        System.out.println(Arrays.toString(modelWeights.entrySet().toArray()).length());
        System.out.println("Consume Method ---------------------------------->\n\n\n");
        
        while (updateIterator.hasNext()) {
            KeyMessage<String, String> km = updateIterator.next();
            String key = km.getKey();
            String message = km.getMessage();
            System.out.println("Publishing Message ---------------------");
            System.out.println(message);
            System.out.println(key);
            System.out.println("End of line -------------------------------->\n\n\n");
            switch (key) {
                case "MODEL":
                    @SuppressWarnings("unchecked") Map<String, Integer> model = (Map<String, Integer>) new ObjectMapper().readValue(message, Map.class);
                    distinctOtherWords.keySet().retainAll(model.keySet());
                    for (Map.Entry<String, Integer> entry : model.entrySet()) {
                        distinctOtherWords.put(entry.getKey(), entry.getValue());
                    }
                    break;
                case "UP":
                    // ignore
                    break;
                default:
                    throw new IllegalArgumentException("Unknown key " + key);
            }
        }
    }
    // Consume Previous Updates and Publishes the Models
    @Override
    public Iterable<String> buildUpdates(JavaPairRDD<String, String> newData) {
        //Initial Theta to be zeros 
        List<String> updates = new ArrayList<>();
        //updates.add("Timothy");
        //updates.add("Okwii");
        //updates.add("Daniel");

        //Needs to read OryxUpdate too
        //OryxInputCode for updating the models is to be implemented here
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

        //gets the time of the day 
        String time1 = TimeProcessor.getTimeOfDay(rdd_records.first().features().apply(0)); //first feature is always time            
        System.out.println(time1 + "<<<<<<<<<<<<<<<<<<<TIIIIIIIIIIIIIME!!!!");

        //builds the model by retrieving the latest thetas from the thetaMap        
        LinearRegressionModel model = rmb.buildModel(rdd_records, rmb.thetaMap.get(time1));

        // return the updates theta to the corresponding time(key)
        rmb.thetaMap.put(time1, rmb.getWeights(model));
        System.out.println("SUCESSFULLY PUT!");

        System.out.println(">>> ENTER TO CONTINUE<<<<<");
        //          System.exit(1);
        //       new java.util.Scanner(System.in).nextLine();

        //return null;
        return updates;
    }
    
    
    
    // End of Implementation
    @Override
    public void close() {
       // throw new UnsupportedOperationException("Not supported yet."); //To 
    }

}
