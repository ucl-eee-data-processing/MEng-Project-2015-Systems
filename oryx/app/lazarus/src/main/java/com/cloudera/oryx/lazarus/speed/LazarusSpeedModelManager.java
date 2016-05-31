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
import com.cloudera.oryx.lazarus.serving.LazarusServingUtility;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import com.cloudera.oryx.api.KeyMessage;
import com.cloudera.oryx.api.speed.SpeedModelManager;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import java.io.IOException;


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
    
    //Curren
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
            switch (key) {
                case "MODEL":
                    // Functionality to ready from already Existing Models in Kafka
                    break;
                case "UP":
                    if (message != null){
                        String [] messageArray = message.split("-");
                        System.out.println(messageArray[0]);
                        System.out.println(Arrays.toString(messageArray));
                        int modelIndex = (int) Double.parseDouble(messageArray[0]);
                        String timeStamp = LazarusServingUtility.indexToTime(modelIndex);
                        System.out.println("Message !!!!!!!!!!!!!!!!!!!!!!!!!");
                        System.out.println(message);
                        double[] weights = LazarusServingUtility.stringToWeights(message);
                        System.out.println("New Weightsssss");
                        System.out.println(Arrays.toString(weights));
                        modelWeights.put(timeStamp, weights);     
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown key " + key);
            }
        }
    }
    
    @Override
    public Iterable<String> buildUpdates(JavaPairRDD<String, String> newData) {
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
       double[] previousWeights = Arrays.copyOfRange(modelWeights.get(time),2,5);
       System.out.println("Updates >>>>>>>>>>>  >>>>>");
       System.out.println(Arrays.toString(previousWeights));
       //System.out.println(previousWeights);
       LinearRegressionModel model;
       model = rmb.buildModel(rdd_records, previousWeights );
       double intercept = model.intercept();
       System.out.println("INTERCEPT ..............................>>>>>>>>>...");
       System.out.println(intercept);
       int timeIndex = LazarusServingUtility.timeToIndex(time);
       String stringWeights = LazarusServingUtility.weightsToString(timeIndex ,intercept ,rmb.getWeights(model));
       updates.set(timeIndex,stringWeights);
       return updates;
    }
    
    @Override
    public void close() {
       throw new UnsupportedOperationException("Not supported yet."); //To 
    }

}
