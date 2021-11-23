package SaZaPraYi;

import battlecode.common.*;
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

    @Test
    public void testInitializeRobotListsMakesNonNullLists() {
        setupTests();
        ec.initializeRobotLists();
        assertNotNull(ec.getNearbyAlliedRobots());
        assertNotNull(ec.getNearbyEnemyRobots());
        assertNotNull(ec.getNearbyGreyRobots());
    }

    @Test
    public void testBuildRobotsBuildsRobot() throws GameActionException {
        setupTests();
        Team playerTeam = Team.A;
        int senseRadius = RobotType.ENLIGHTENMENT_CENTER.sensorRadiusSquared;
        Mockito.when(rc.getTeam()).thenReturn(playerTeam);
        Mockito.when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);
        Mockito.when(rc.senseNearbyRobots(senseRadius)).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        Mockito.when(rc.onTheMap(Mockito.any(MapLocation.class))).thenReturn(true);
        Mockito.when(rc.canBuildRobot(Mockito.any(RobotType.class), Mockito.any(Direction.class), Mockito.anyInt()))
                .thenReturn(true);
        boolean builtARobot = ec.buildRobots();
        assertTrue(builtARobot);
    }
}
