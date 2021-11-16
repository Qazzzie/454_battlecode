package SaZaPraYi;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class PoliticianTest {

    private RobotController rc;
    private RobotUtils utils;
    private Politician p;

    private void setupTests(){
        rc = Mockito.mock(RobotController.class);
        utils = new RobotUtils(rc);
        p = new Politician(rc,utils);
    }

    @Test
    public void testUsableConviction(){
        setupTests();
        int initialConviction = 100;
        //int convictionPenalty = 10;
        int noofNearByRobots = 5;
        int expectedResult = 18;
        double empowerFactor = 1;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testUsableConvictionWhenNearByRobotIsZero(){
        setupTests();
        int initialConviction = 100;
        int convictionPenalty = 10;
        int noofNearByRobots = 0;
        int expectedResult = initialConviction - convictionPenalty;
        double empowerFactor = 1;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testUsableConvictionWhenEmpowerFactorisLessThan1(){
        setupTests();
        int initialConviction = 100;
        //int convictionPenalty = 10;
        int noofNearByRobots = 5;
        int expectedResult = 18;
        double empowerFactor = 0.9;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testUsableConvictionWhenEmpowerFactorisGreaterThan1(){
        setupTests();
        int initialConviction = 100;
        //int convictionPenalty = 10;
        int noofNearByRobots = 5;
        int expectedResult = 21;
        double empowerFactor = 1.2;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHandleNearbyGreyECMuckrakerHasMuckraker() throws GameActionException {
        setupTests();
        RobotInfo nearbyEnemyMuckraker = new RobotInfo(
                3,
                Team.B,
                RobotType.MUCKRAKER,
                10,
                10,
                new MapLocation(1, 0)
        );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{nearbyEnemyMuckraker});
        Mockito.when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        Mockito.when(rc.getFlag(nearbyEnemyMuckraker.ID))
                .thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        boolean result = p.handleNearbyGreyECMuckraker();
        assertTrue(result);
    }

    @Test
    public void testMoveTowardsEnemyWithNearbyEnemyEC() throws GameActionException {
        setupTests();
        RobotInfo nearbyEnemy = new RobotInfo(
                3,
                Team.B,
                RobotType.ENLIGHTENMENT_CENTER,
                10,
                10,
                new MapLocation(1, 0)
        );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{nearbyEnemy});
        Mockito.when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        Mockito.when(rc.getFlag(nearbyEnemy.ID))
                .thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        boolean result = p.handleMoveTowardsEnemy(Team.B, 1,1,10);
        assertTrue(result);
    }

    @Test
    public void testMoveTowardsEnemyWithNearbyEnemySlanderer() throws GameActionException {
        setupTests();
        RobotInfo nearbyEnemy = new RobotInfo(
                3,
                Team.B,
                RobotType.SLANDERER,
                10,
                10,
                new MapLocation(1, 0)
        );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{nearbyEnemy});
        Mockito.when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        Mockito.when(rc.getFlag(nearbyEnemy.ID))
                .thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
        boolean result = p.handleMoveTowardsEnemy(Team.B, 1,1,10);
        assertTrue(result);
    }

    @Test
    public void testMoveTowardsEnemyWithNoNearbyEnemies() throws GameActionException {
        setupTests();
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{});
        boolean result = p.handleMoveTowardsEnemy(Team.B, 1,1,10);
        assertFalse(result);
    }

    @Test
    public void testEmpowerNeutralECWithNearbyEC() throws GameActionException {
        setupTests();
        RobotInfo nearbyNeutralEC = new RobotInfo(
                3,
                Team.NEUTRAL,
                RobotType.ENLIGHTENMENT_CENTER,
                10,
                10,
                new MapLocation(1, 0)
        );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any()))
                .thenReturn(new RobotInfo[]{nearbyNeutralEC});
        boolean result = p.empowerNeutralEC(1, 1, Team.NEUTRAL);
        assertTrue(result);
    }

    @Test
    public void testEmpowerNeutralECWithNoNearbyEC() throws GameActionException {
        setupTests();
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any()))
                .thenReturn(new RobotInfo[]{});
        boolean result = p.empowerNeutralEC(1, 1, Team.NEUTRAL);
        assertFalse(result);
    }

    @Test
    public void testConvictOwnTeamWithNearbyFriendlyUnit() throws GameActionException {
        setupTests();
        RobotInfo nearbyFriendlyMuckraker = new RobotInfo(
                3,
                Team.A,
                RobotType.MUCKRAKER,
                10,
                10,
                new MapLocation(1, 0)
        );
        Mockito.when(rc.getRoundNum()).thenReturn(200);
        Mockito.when(rc.canEmpower(Mockito.anyInt())).thenReturn(true);
        boolean result = p.convictOwnTeam(10, 10);
        assertTrue(result);
    }

    @Test
    public void testConvictOwnTeamWhenTooEarly() throws GameActionException {
        setupTests();
        Mockito.when(rc.getRoundNum()).thenReturn(0);
        boolean result = p.convictOwnTeam(10, 10);
        assertFalse(result);
    }
}