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

package com.cloudera.oryx.lambda;

import org.junit.Test;

import com.cloudera.oryx.common.OryxTest;

public final class FunctionsTest extends OryxTest {

  @Test
  public void testLast() throws Exception {
    assertEquals(3.0, Functions.last().call(7.0, 3.0));
  }

  @Test
  public void testNoOp() throws Exception {
    // Shouldn't do anything
    Functions.noOp().call(null);
  }

  @Test
  public void testIdentity() throws Exception {
    Object o = new Object();
    assertSame(o, Functions.identity().call(o));
    assertNull(Functions.identity().call(null));
  }

}
