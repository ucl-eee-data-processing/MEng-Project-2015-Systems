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

package com.cloudera.oryx.lazarus.serving;

import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.typesafe.config.Config;
import org.apache.hadoop.conf.Configuration;
import com.cloudera.oryx.api.KeyMessage;
import com.cloudera.oryx.api.serving.AbstractServingModelManager;
import com.cloudera.oryx.api.serving.ServingModel;

/**
 * Reads models and updates produced by the Batch Layer and Speed Layer. Models are maps, encoded as JSON
 * strings, mapping words to count of distinct other words that appear with that word in an input line.
 * Updates are "word,count" pairs representing new counts for a word. This class manages and exposes the
 * mapping to the Serving Layer applications.
 */
public final class LazarusServingModelManager extends AbstractServingModelManager<String> {
    
    private final Map<String, double[] > modelWeights = 
        Collections.synchronizedMap(new HashMap<String, double[] >());
    
    public LazarusServingModelManager(Config config) {
        super(config);
    }

    @Override
    public void consume(Iterator<KeyMessage<String,String>> updateIterator, Configuration hadoopConf) throws IOException {
       //Update Serving Model
        while (updateIterator.hasNext()) {
            KeyMessage<String,String> km = updateIterator.next();
            String key = km.getKey();
            String message = km.getMessage();
            if(message != null){
              switch (key) {
                case "MODEL":
                   break;
                case "UP":
                    String [] messageArray = message.split("#");
                    int timeIndex = (int) Double.parseDouble(messageArray[0]);
                    String time = LazarusServingUtility.indexToTime(timeIndex);
                    double[] weights = LazarusServingUtility.stringToWeights(message);
                    System.out.println(Arrays.toString(weights));
                    modelWeights.put(time, weights);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown key " + key);
              }
            }
        }
    }
  
 
 // Retrieve Updated Model
  @Override
  public ServingModel getModel() {
    return new LazarusServingModel(modelWeights);
  }
}
