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

package com.cloudera.oryx.app.speed.kmeans;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.dmg.pmml.PMML;

import com.cloudera.oryx.app.kmeans.KMeansPMMLUtilsTest;
import com.cloudera.oryx.common.collection.Pair;
import com.cloudera.oryx.common.pmml.PMMLUtils;
import com.cloudera.oryx.common.text.TextUtils;
import com.cloudera.oryx.kafka.util.DatumGenerator;

public final class MockKMeansModelGenerator implements DatumGenerator<String,String> {

  @Override
  public Pair<String,String> generate(int id, RandomGenerator random) {
    if (id == 0) {
      PMML pmml = KMeansPMMLUtilsTest.buildDummyClusteringModel();
      return new Pair<>("MODEL", PMMLUtils.toString(pmml));
    } else {
      List<?> data = Arrays.asList(id % 3, Arrays.asList(id, id), id);
      return new Pair<>("UP", TextUtils.joinJSON(data));
    }
  }

}
