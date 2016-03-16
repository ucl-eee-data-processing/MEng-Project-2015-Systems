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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.api.java.JavaPairRDD;

import com.cloudera.oryx.api.KeyMessage;
import com.cloudera.oryx.api.speed.SpeedModelManager;
import com.cloudera.oryx.example.batch.ExampleBatchLayerUpdate;

/**
 * Also counts and emits counts of number of distinct words that occur with words.
 * Listens for updates from the Batch Layer, which give the current correct count at its
 * last run. Updates these counts approximately in response to the same data stream
 * that the Batch Layer sees, but assumes all words seen are new and distinct, which is only
 * approximately true. Emits updates of the form "word,count".
 */
public final class ExampleSpeedModelManager implements SpeedModelManager<String,String,String> {

  private final Map<String,Integer> distinctOtherWords =
      Collections.synchronizedMap(new HashMap<String,Integer>());

  @Override
  public void consume(Iterator<KeyMessage<String,String>> updateIterator,
                      Configuration hadoopConf) throws IOException {
    while (updateIterator.hasNext()) {
      KeyMessage<String,String> km = updateIterator.next();
      String key = km.getKey();
      String message = km.getMessage();
      switch (key) {
        case "MODEL":
          @SuppressWarnings("unchecked")
          Map<String,Integer> model = (Map<String,Integer>) new ObjectMapper().readValue(message, Map.class);
          distinctOtherWords.keySet().retainAll(model.keySet());
          for (Map.Entry<String,Integer> entry : model.entrySet()) {
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

  @Override
  public Iterable<String> buildUpdates(JavaPairRDD<String,String> newData) {
    List<String> updates = new ArrayList<>();
    for (Map.Entry<String,Integer> entry :
         ExampleBatchLayerUpdate.countDistinctOtherWords(newData).entrySet()) {
      String word = entry.getKey();
      int count = entry.getValue();
      int newCount;
      synchronized (distinctOtherWords) {
        Integer oldCount = distinctOtherWords.get(word);
        newCount = oldCount == null ? count : oldCount + count;
        distinctOtherWords.put(word, newCount);
      }
      updates.add(word + "," + newCount);
    }
    return updates;
  }

  @Override
  public void close() {
    // do nothing
  }

}