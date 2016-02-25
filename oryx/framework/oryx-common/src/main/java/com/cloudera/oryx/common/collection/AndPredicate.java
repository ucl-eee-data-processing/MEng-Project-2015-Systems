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

package com.cloudera.oryx.common.collection;

import net.openhft.koloboke.function.Predicate;

/**
 * Logical AND of two {@link Predicate}s.
 *
 * @param <T> operand type
 */
final class AndPredicate<T> implements Predicate<T> {

  // Consider supporting arbitrary # later
  private final Predicate<T> a;
  private final Predicate<T> b;

  AndPredicate(Predicate<T> a, Predicate<T> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public boolean test(T value) {
    return a.test(value) && b.test(value);
  }
}
