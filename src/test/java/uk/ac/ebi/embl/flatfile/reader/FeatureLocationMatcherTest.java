package uk.ac.ebi.embl.flatfile.reader;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.*;

import java.io.IOException;

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

    public void testLocationMatcher_RightPartialRemoteRangeSamePosition() {
        featureLocationMatcher.match("J00194.1:340..>340");
        RemoteRange location = (RemoteRange) featureLocationMatcher.getLocation();
        assertFalse(featureLocationMatcher.isLeftPartial());
        assertTrue(featureLocationMatcher.isRightPartial());
        assertTrue(location.getBeginPosition() == 340);
        assertTrue(location.getEndPosition() == 340);
        assertTrue(location.getAccession().equals("J00194"));
        assertTrue(location.getVersion() == 1);
    }

    public void testLocationMatcher_RightPartialRemoteRangeSamePosition_AlternateSyntax() {
        featureLocationMatcher.match("J00194.1:>340");
        RemoteBase location = (RemoteBase) featureLocationMatcher.getLocation();
        assertFalse(featureLocationMatcher.isLeftPartial());
        assertTrue(featureLocationMatcher.isRightPartial());
        assertTrue(location.getBeginPosition() == 340);
        assertTrue(location.getEndPosition() == 340);
        assertTrue(location.getAccession().equals("J00194"));
        assertTrue(location.getVersion() == 1);
    }

    /*
    This test shows that it may be an issue to use <50 to represent <50..50 as they may not be semantically the same.
    <50     is read as -> a left partial local base (THERE IS NO SUCH THING AS LEFT OR RIGHT PARTIAL BASE!)
    <50..50 is read as -> a left partial local range (with same begin and end positions)
     */
    public void nottestLocationMatcher_LeftAndRightPartialSyntax() {
        featureLocationMatcher.match("<50");
        LocalBase location1 = (LocalBase) featureLocationMatcher.getLocation(); // NOTE read as local base
        assertTrue(featureLocationMatcher.isLeftPartial());
        assertTrue(location1.getBeginPosition() == 50);
        assertTrue(location1.getEndPosition() == 50);

        featureLocationMatcher = new FeatureLocationMatcher(null);
        featureLocationMatcher.match("<50..50");
        LocalRange location2 = (LocalRange) featureLocationMatcher.getLocation(); // NOTE read as remote base
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
    }

}
