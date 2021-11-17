package SaZaPraYi;

import battlecode.common.GameActionException;
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

    @Test
    public void testBidActuallyBids() throws GameActionException {
        setupTests();
        Mockito.when(rc.getRoundNum()).thenReturn(1000);
        Mockito.when(rc.getInfluence()).thenReturn(1000);
        boolean result = ec.bid();
        assertTrue(result);
    }

    @Test
    public void testBidWithNoInfluence() throws GameActionException {
        setupTests();
        Mockito.when(rc.getRoundNum()).thenReturn(1000);
        Mockito.when(rc.getInfluence()).thenReturn(0);
        boolean result = ec.bid();
        assertFalse(result);
    }

    @Test
    public void testBidOnFirstRound() throws GameActionException {
        setupTests();
        Mockito.when(rc.getRoundNum()).thenReturn(0);
        Mockito.when(rc.getInfluence()).thenReturn(1000);
        boolean result = ec.bid();
        assertFalse(result);
    }

    @Test
    public void testRandomSpawnableRobotTypeReturnsRobot() {
        RobotType result = EnlightenmentCenter.randomSpawnableRobotType();
        assertNotSame(result, RobotType.ENLIGHTENMENT_CENTER);
        assertNotSame(result, RobotType.SLANDERER);
    }
}
