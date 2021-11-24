package SaZaPraYi;



import org.mockito.Mockito;
import static org.junit.Assert.*;
import battlecode.common.*;
import org.junit.Test;

import java.util.ArrayList;

public class MuckrakerTest {
        private RobotController rc;
        private RobotUtils utils;
        private Muckraker M;

        private void setupTests() {
            rc = Mockito.mock(RobotController.class);
            utils = new RobotUtils(rc);
            M = new Muckraker(rc, utils);
        }

        @Test
        public void exposeUnits() throws GameActionException{
            setupTests();

            setupTests();
            Team player = Team.A;
            Mockito.when(rc.getTeam()).thenReturn(player);
            Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);

            RobotInfo unitA = new RobotInfo(3,
                    rc.getTeam(),
                    RobotType.MUCKRAKER,
                    10,
                    10,
                    new MapLocation(0, 3));
            RobotInfo unitB = new RobotInfo(4,
                    rc.getTeam().opponent(),
                    RobotType.SLANDERER,
                    10,
                    10,
                    new MapLocation(2, -2));

            Mockito.when(rc.senseRobotAtLocation(unitA.location)).thenReturn(unitA);
            Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);
            MapLocation location = new MapLocation(0,0);
            RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};
            int senseRadius = rc.getType().sensorRadiusSquared;

            Mockito.when(rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent())).thenReturn(nearbyUnits);
            Mockito.when(rc.canExpose(unitB.location)).thenReturn(true);
            boolean exposed = M.exposeUnits();
            assertTrue(exposed);
        }

        @Test
        public void followEnemyECFlaggingMuckrakers() throws GameActionException{
        setupTests();
        Team player = Team.A;
        Mockito.when(rc.getTeam()).thenReturn(player);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        int actionRadius = RobotType.MUCKRAKER.actionRadiusSquared;

            RobotInfo unitA = new RobotInfo(3,
                    rc.getTeam(),
                    RobotType.MUCKRAKER,
                    10,
                    10,
                    new MapLocation(0, 3));
            RobotInfo unitB = new RobotInfo(4,
                    rc.getTeam().opponent(),
                    RobotType.SLANDERER,
                    10,
                    10,
                    new MapLocation(2, -2));


            Mockito.when(rc.getFlag(unitB.getID())).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_ENEMY_EC.ordinal());
            Mockito.when(rc.senseRobotAtLocation(unitA.location)).thenReturn(unitA);
            Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);
            MapLocation location = new MapLocation(0,0);
            RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};
            int senseRadius = rc.getType().sensorRadiusSquared;

            //Mockito.when(rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent())).thenReturn(nearbyUnits);
            Mockito.when(rc.senseNearbyRobots(actionRadius, Team.B)).thenReturn(new RobotInfo[]{unitB});
            Mockito.when(rc.canExpose(unitB.location)).thenReturn(true);
            //        Mockito.when(rc.expose(unitB.location)).then(true);
            boolean exposed = M.exposeUnits();
            assertTrue(exposed);
        }

        @Test
        public void testAvoidPolitician() throws GameActionException {
            setupTests();
            RobotInfo enemy = new RobotInfo(
                    100, Team.B, RobotType.POLITICIAN,
                    10, 10,
                    new MapLocation(5, 5)
            );
            Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{enemy});
            Mockito.when(rc.getType()).thenReturn(RobotType.POLITICIAN);
            Mockito.when(rc.getTeam()).thenReturn(Team.B);
            Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));

            Direction enemy_loc = rc.getLocation().directionTo(enemy.location);

            Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
            boolean result = M.avoidPolitician();
            assertTrue(result);
        }

    @Test
    public void testHandleGreyEcFollowWhenNearbyGreyEC() throws GameActionException {
       setupTests();
       RobotInfo greyEC = new RobotInfo(3,
           Team.NEUTRAL,
           RobotType.ENLIGHTENMENT_CENTER,
           1,
           1,
           new MapLocation(0, 3));
       RobotInfo nearbyMuckrakerWithFlag = new RobotInfo(6,
           Team.A,
           RobotType.MUCKRAKER,
           10,
           10,
           new MapLocation(0,5));
       int ourId = 1;
       int ourSenseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
       M.setLocationOfBase(new MapLocation(0,-10));
       M.setLocationOfGreyEC(greyEC.location);
       Mockito.when(rc.senseRobotAtLocation(greyEC.location)).thenReturn(greyEC);
       Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{greyEC,nearbyMuckrakerWithFlag});
       Mockito.when(rc.getTeam()).thenReturn(Team.A);
       Mockito.when(rc.senseNearbyRobots(ourSenseRadius, Team.A)).thenReturn(new RobotInfo[]{greyEC});
       Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
       Mockito.when(rc.getID()).thenReturn(ourId);
       Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
       Mockito.when(rc.getFlag(ourId)).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
       Mockito.when(rc.getFlag(nearbyMuckrakerWithFlag.ID))
           .thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
       boolean shouldReturn = M.handleGreyECFollow();
       assertTrue(shouldReturn);
    }

    @Test
    public void testHandleGreyEcFollowWhenNoNearbyEC() throws GameActionException {
      setupTests();
      Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{});
      Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
      Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
      boolean shouldReturn = M.handleGreyECFollow();
      assertFalse(shouldReturn);
    }

    @Test
    public void testHandleMovementAsBouncyMuckraker() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        M.setMuckrakerType(Muckraker.muckrakerTypes.BOUNCY);
        M.setLocationOfBase(new MapLocation(100,100));
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.A)).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getID()).thenReturn(1);
        boolean shouldReturn = M.handleMovement();
        assertTrue(shouldReturn);
    }

    @Test
    public void testHandleMovementAsNormalMuckraker() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        M.setMuckrakerType(Muckraker.muckrakerTypes.NORMAL);
        M.setLocationOfBase(new MapLocation(100,100));
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.A)).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getID()).thenReturn(1);
        boolean shouldReturn = M.handleMovement();
        assertTrue(shouldReturn);
    }

    @Test
    public void testHandlingOfNearbyGreyEC() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        int currentFlag = RobotUtils.flags.NOTHING.ordinal();
        int ourId = 1;
        RobotInfo greyEC = new RobotInfo(3,
                Team.NEUTRAL,
                RobotType.ENLIGHTENMENT_CENTER,
                1,
                1,
                new MapLocation(0, 3));
        Mockito.when(rc.senseRobotAtLocation(greyEC.location)).thenReturn(greyEC);
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{greyEC});
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.A)).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.getID()).thenReturn(ourId);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getFlag(ourId)).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        boolean encounteredGreyEC = M.handleGreyECFollow();
        assertTrue(encounteredGreyEC);
    }

    @Test
    public void testHandlingOfNearbyGreyECWithNearbyMuckraker() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        int currentFlag = RobotUtils.flags.NOTHING.ordinal();
        int ourId = 1;
        RobotInfo greyEC = new RobotInfo(3,
                Team.NEUTRAL,
                RobotType.ENLIGHTENMENT_CENTER,
                1,
                1,
                new MapLocation(0, 3));
        RobotInfo nearbyMuckrakerWithFlag = new RobotInfo(4,
                Team.A,
                RobotType.MUCKRAKER,
                1,
                1,
                new MapLocation(0,5));
        M.setLocationOfEC(greyEC.location);
        Mockito.when(rc.senseRobotAtLocation(greyEC.location)).thenReturn(greyEC);
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.A)).thenReturn(new RobotInfo[]{nearbyMuckrakerWithFlag});
        Mockito.when(rc.senseNearbyRobots(senseRadius)).thenReturn(new RobotInfo[]{greyEC, nearbyMuckrakerWithFlag});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.getID()).thenReturn(ourId);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getFlag(ourId)).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        Mockito.when(rc.getFlag(nearbyMuckrakerWithFlag.ID)).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        boolean encounteredGreyEC = M.handleGreyECFollow();
        assertTrue(encounteredGreyEC);
    }

    @Test
    public void testHandleGreyECEncounter() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        int currentFlag = RobotUtils.flags.NOTHING.ordinal();
        int ourId = 1;
        RobotInfo greyEC = new RobotInfo(3,
                Team.NEUTRAL,
                RobotType.ENLIGHTENMENT_CENTER,
                1,
                1,
                new MapLocation(0, 3));
        Mockito.when(rc.senseRobotAtLocation(greyEC.location)).thenReturn(greyEC);
        Mockito.when(rc.senseNearbyRobots(senseRadius)).thenReturn(new RobotInfo[]{greyEC});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.getID()).thenReturn(ourId);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getFlag(ourId)).thenReturn(RobotUtils.flags.NOTHING.ordinal());
        boolean encounteredGreyEC = M.handleGreyECEncounter(Team.A, senseRadius, currentFlag);
        assertTrue(encounteredGreyEC);
    }

    @Test
    public void testGuardEnemyECWithNearbyEnemyEC() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        int actionRadius = RobotType.MUCKRAKER.actionRadiusSquared;
        int ourId = 1;
        RobotInfo enemyEC = new RobotInfo(3,
                Team.B,
                RobotType.ENLIGHTENMENT_CENTER,
                1,
                1,
                new MapLocation(0,3));
        ArrayList<RobotInfo> nearbyMuckrakers = new ArrayList<>();
        int muckrakerIDOffset = 10;
        for(int i = 0; i < 8; i++) {
            RobotInfo muckrakerToAdd = new RobotInfo(i + muckrakerIDOffset,
                    Team.A,
                    RobotType.MUCKRAKER,
                    1,
                    1,
                    new MapLocation(-1, 4-i));
            nearbyMuckrakers.add(muckrakerToAdd);
        }
        Mockito.when(rc.senseRobotAtLocation(enemyEC.location)).thenReturn(enemyEC);
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.A)).thenReturn(nearbyMuckrakers.toArray(new RobotInfo[0]));
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.B)).thenReturn(new RobotInfo[]{enemyEC});
        Mockito.when(rc.senseNearbyRobots(actionRadius, Team.B)).thenReturn(new RobotInfo[]{enemyEC});
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{enemyEC});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.getID()).thenReturn(ourId);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getFlag(Mockito.anyInt())).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_ENEMY_EC.ordinal());
        Mockito.when(rc.canSenseLocation(Mockito.any())).thenReturn(true);
        Mockito.when(rc.onTheMap(Mockito.any())).thenReturn(true);
        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
        boolean nearbyEnemyECExists = M.guardEnemyEC();
        assertTrue(nearbyEnemyECExists);
    }

    @Test
    public void testGuardEnemyECWithNoNearbyEnemyEC() throws GameActionException {
        setupTests();
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        int actionRadius = RobotType.MUCKRAKER.actionRadiusSquared;
        int ourId = 1;
        ArrayList<RobotInfo> nearbyMuckrakers = new ArrayList<>();
        int muckrakerIDOffset = 10;
        for(int i = 0; i < Muckraker.MUCKRAKERS_NEEDED_TO_SWARM + 1; i++) {
            RobotInfo muckrakerToAdd = new RobotInfo(i + muckrakerIDOffset,
                    Team.A,
                    RobotType.MUCKRAKER,
                    1,
                    1,
                    new MapLocation(-1, 4-i));
            nearbyMuckrakers.add(muckrakerToAdd);
        }
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.A)).thenReturn(nearbyMuckrakers.toArray(new RobotInfo[0]));
        Mockito.when(rc.senseNearbyRobots(senseRadius, Team.B)).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.senseNearbyRobots(actionRadius, Team.B)).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt())).thenReturn(new RobotInfo[]{});
        Mockito.when(rc.getTeam()).thenReturn(Team.A);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0,0));
        Mockito.when(rc.getID()).thenReturn(ourId);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        Mockito.when(rc.getFlag(Mockito.anyInt())).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_ENEMY_EC.ordinal());
        Mockito.when(rc.canSenseLocation(Mockito.any())).thenReturn(true);
        Mockito.when(rc.onTheMap(Mockito.any())).thenReturn(true);
        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
        boolean nearbyEnemyECExists = M.guardEnemyEC();
        assertFalse(nearbyEnemyECExists);
    }
}
