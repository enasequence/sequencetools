/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.*;

public class FeatureLocationMatcherTest extends TestCase {

  private FeatureLocationMatcher featureLocationMatcher;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.featureLocationMatcher = new FeatureLocationMatcher(null);
  }

  @Test
  public void testLocationMatcher_LocalBase() throws IOException {
    featureLocationMatcher.match("467");
    LocalBase location = (LocalBase) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertFalse(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 467);
    assertTrue(location.getEndPosition() == 467);
  }

  public void testLocationMatcher_RemoteBase() {
    featureLocationMatcher.match("J00194.1:467");
    RemoteBase location = (RemoteBase) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertFalse(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 467);
    assertTrue(location.getEndPosition() == 467);
    assertTrue(location.getAccession().equals("J00194"));
    assertTrue(location.getVersion() == 1);
  }

  public void testLocationMatcher_LocalRange() {
    featureLocationMatcher.match("340..565");
    LocalRange location = (LocalRange) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertFalse(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 340);
    assertTrue(location.getEndPosition() == 565);
  }

  public void testLocationMatcher_RemoteRange() {
    featureLocationMatcher.match("J00194.1:340..565");
    RemoteRange location = (RemoteRange) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertFalse(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 340);
    assertTrue(location.getEndPosition() == 565);
    assertTrue(location.getAccession().equals("J00194"));
    assertTrue(location.getVersion() == 1);
  }

  public void testLocationMatcher_LeftPartialLocalRange() {
    featureLocationMatcher.match("<340..565");
    LocalRange location = (LocalRange) featureLocationMatcher.getLocation();
    assertTrue(featureLocationMatcher.isLeftPartial());
    assertFalse(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 340);
    assertTrue(location.getEndPosition() == 565);
  }

  public void testLocationMatcher_LeftPartialRemoteRange() {
    featureLocationMatcher.match("J00194.1:<340..565");
    RemoteRange location = (RemoteRange) featureLocationMatcher.getLocation();
    assertTrue(featureLocationMatcher.isLeftPartial());
    assertFalse(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 340);
    assertTrue(location.getEndPosition() == 565);
    assertTrue(location.getAccession().equals("J00194"));
    assertTrue(location.getVersion() == 1);
  }

  public void testLocationMatcher_RightPartialLocalRange() {
    featureLocationMatcher.match("340..>565");
    LocalRange location = (LocalRange) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertTrue(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 340);
    assertTrue(location.getEndPosition() == 565);
  }

  public void testLocationMatcher_RightPartialRemoteRange() {
    featureLocationMatcher.match("J00194.1:340..>565");
    RemoteRange location = (RemoteRange) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertTrue(featureLocationMatcher.isRightPartial());
    assertTrue(location.getBeginPosition() == 340);
    assertTrue(location.getEndPosition() == 565);
    assertTrue(location.getAccession().equals("J00194"));
    assertTrue(location.getVersion() == 1);
  }

  /*
  This test shows the representation difference of <50 and <50..50 in the code:
  <50     -> read as left partial local base (despite the fact the partiality is not used in junction with base location)
  <50..50 -> read as left partial local range
  In both cases, the begin and end positions are the same as expected.
   */
  public void nottestLocationMatcher_LeftAndRightPartialSyntax() {
    // local cases
    featureLocationMatcher.match("<50");
    LocalBase location1 =
        (LocalBase) featureLocationMatcher.getLocation(); // NOTE read as local base
    assertTrue(featureLocationMatcher.isLeftPartial());
    assertTrue(location1.getBeginPosition() == 50);
    assertTrue(location1.getEndPosition() == 50);

    featureLocationMatcher = new FeatureLocationMatcher(null);
    featureLocationMatcher.match("<50..50");
    LocalRange location2 =
        (LocalRange) featureLocationMatcher.getLocation(); // NOTE read as remote base
    assertTrue(featureLocationMatcher.isLeftPartial());
    assertTrue(location2.getBeginPosition() == 50);
    assertTrue(location2.getEndPosition() == 50);

    featureLocationMatcher = new FeatureLocationMatcher(null);
    featureLocationMatcher.match(">50");
    LocalBase location3 = (LocalBase) featureLocationMatcher.getLocation();
    assertTrue(featureLocationMatcher.isRightPartial());
    assertTrue(location3.getBeginPosition() == 50);
    assertTrue(location3.getEndPosition() == 50);

    featureLocationMatcher = new FeatureLocationMatcher(null);
    featureLocationMatcher.match("50..>50");
    LocalRange location4 = (LocalRange) featureLocationMatcher.getLocation();
    assertTrue(featureLocationMatcher.isRightPartial());
    assertTrue(location4.getBeginPosition() == 50);
    assertTrue(location4.getEndPosition() == 50);

    // remote cases
    featureLocationMatcher = new FeatureLocationMatcher(null);
    featureLocationMatcher.match("J00194.1:>340");
    RemoteBase location5 = (RemoteBase) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertTrue(featureLocationMatcher.isRightPartial());
    assertTrue(location5.getBeginPosition() == 340);
    assertTrue(location5.getEndPosition() == 340);
    assertTrue(location5.getAccession().equals("J00194"));
    assertTrue(location5.getVersion() == 1);

    featureLocationMatcher.match("J00194.1:340..>340");
    RemoteRange location6 = (RemoteRange) featureLocationMatcher.getLocation();
    assertFalse(featureLocationMatcher.isLeftPartial());
    assertTrue(featureLocationMatcher.isRightPartial());
    assertTrue(location6.getBeginPosition() == 340);
    assertTrue(location6.getEndPosition() == 340);
    assertTrue(location6.getAccession().equals("J00194"));
    assertTrue(location6.getVersion() == 1);
  }
}
