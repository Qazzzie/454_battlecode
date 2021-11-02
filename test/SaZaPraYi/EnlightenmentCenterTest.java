package SaZaPraYi;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

public class EnlightenmentCenterTest {

    private RobotController rc;
    private RobotUtils utils;
    private EnlightenmentCenter ec;


    private void setupTests() {
        rc = Mockito.mock(RobotController.class);
        utils = new RobotUtils(rc);
        ec = new EnlightenmentCenter(rc, utils);
    }

    @Test
    public void testBidRatioIsGreaterThanZeroAfterTurn1000() {
        setupTests();
        int fakeTurnNumber = 1000;
        double result = ec.getBidRatio(fakeTurnNumber);
        assertTrue(result > 0);
    }

    @Test
    public void testBidRatioIsLessThan1AfterTurn1000() {
        int fakeTurnNumber = 1000;
        double result = ec.getBidRatio(fakeTurnNumber);
        assertTrue(result < 1);
    }

    @Test
    public void randomSpawnableRobotTypeIsNotNull() {
        setupTests();
        RobotType result = ec.randomSpawnableRobotType();
        assertNotNull(result);
    }
}
