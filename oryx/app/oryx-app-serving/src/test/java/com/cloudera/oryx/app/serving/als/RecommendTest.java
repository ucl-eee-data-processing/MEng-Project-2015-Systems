/*
 * Copyright (c) 2014, Cloudera and Intel, Inc. All Rights Reserved.
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

package com.cloudera.oryx.app.serving.als;

import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import com.cloudera.oryx.app.serving.IDValue;

public final class RecommendTest extends AbstractALSServingTest {

  @Test
  public void testRecommend() {
    List<IDValue> recs = target("/recommend/U0").request()
        .accept(MediaType.APPLICATION_JSON_TYPE).get(LIST_ID_VALUE_TYPE);
    testTopByValue(6, recs, false);
    Assert.assertEquals("I1", recs.get(0).getID());
    Assert.assertEquals(0.4653969f, recs.get(0).getValue(), FLOAT_EPSILON);
  }

  @Test
  public void testRecommendCSV() {
    String response = target("/recommend/U0").request().get(String.class);
    testCSVTopByScore(6, response);
  }

  @Test
  public void testHowMany() {
    testHowMany("/recommend/U5", 10, 2);
    testHowMany("/recommend/U5", 2, 2);
    testHowMany("/recommend/U5", 1, 1);
  }

  @Test(expected = BadRequestException.class)
  public void testBadHowMany() {
    testHowMany("/recommend/U5", -1, 0);
  }

  @Test
  public void testOffset() {
    testOffset("/recommend/U6", 2, 1, 2);
    testOffset("/recommend/U6", 3, 1, 2);
    testOffset("/recommend/U6", 1, 1, 1);
    testOffset("/recommend/U6", 3, 3, 0);
  }

  @Test(expected = BadRequestException.class)
  public void testBadOffset() {
    testOffset("/recommend/U6", 3, -1, 0);
  }

  @Test
  public void testConsiderKnownItems() {
    List<IDValue> normal = target("/recommend/U4").request()
        .accept(MediaType.APPLICATION_JSON_TYPE).get(LIST_ID_VALUE_TYPE);
    Assert.assertEquals(3, normal.size());
    Assert.assertEquals("I2", normal.get(0).getID());
    Assert.assertEquals(0.14134796f, normal.get(0).getValue(), FLOAT_EPSILON);

    List<IDValue> withConsider = target("/recommend/U4")
        .queryParam("considerKnownItems", "true").request()
        .accept(MediaType.APPLICATION_JSON_TYPE).get(LIST_ID_VALUE_TYPE);
    Assert.assertEquals(9, withConsider.size());
    Assert.assertEquals("I7", withConsider.get(0).getID());
    Assert.assertEquals(2.0047457f, withConsider.get(0).getValue(), FLOAT_EPSILON);
  }

  @Test(expected = NotFoundException.class)
  public void testNoArg() {
    target("/recommend").request().get(String.class);
  }

  @Test
  public void testRescorer() {
    List<IDValue> normal = target("/recommend/U4").queryParam("rescorerParams", "foo").request()
        .accept(MediaType.APPLICATION_JSON_TYPE).get(LIST_ID_VALUE_TYPE);
    Assert.assertEquals(1, normal.size());
    Assert.assertEquals("I3", normal.get(0).getID());
    Assert.assertEquals(2.0f * -0.2599307f, normal.get(0).getValue(), FLOAT_EPSILON);
  }

}
