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

import java.util.Map;
import java.util.HashMap;

import com.cloudera.oryx.api.serving.ServingModel;

/**
 * {@link ServingModel} produced by {@link ExampleServingModelManager}.
 */
public final class LazarusServingModel implements ServingModel {

  private final Map<String, double[] > modelWeights; 

  LazarusServingModel(Map<String, double[]> modelWeights) {
    this.modelWeights = modelWeights;
  }
  @Override
  public float getFractionLoaded() {
    return 1.0f;
  }
  
  public Map<String, double[]> getModelWeights() {
    return modelWeights;
  }
}