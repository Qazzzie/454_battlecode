package SaZaPraYi;



import org.mockito.Mockito;
import static org.junit.Assert.*;
import battlecode.common.*;
import org.junit.Test;

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


            Mockito.when(rc.getFlag(unitB.getID())).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_ENEMY_EC.ordinal());

            Mockito.when(rc.senseRobotAtLocation(unitA.location)).thenReturn(unitA);
            Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);

            Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
            MapLocation location = new MapLocation(0,0);
            Mockito.when(rc.getLocation()).thenReturn(location);
            RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};

            Mockito.when(rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam())).thenReturn(nearbyUnits);
            boolean moved = M.followEnemyECFlaggingMuckrakers();
            assertTrue(moved);
        }


        @Test
        public void followEnemyECFlaggingMuckrakers() throws GameActionException{

            Mockito.when(rc.senseRobotAtLocation(unitA.location)).thenReturn(unitA);
            Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);
            MapLocation location = new MapLocation(0,0);
            RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};
            int senseRadius = rc.getType().sensorRadiusSquared;

            Mockito.when(rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent())).thenReturn(nearbyUnits);
            Mockito.when(rc.canExpose(unitB.location)).thenReturn(true);
            //        Mockito.when(rc.expose(unitB.location)).then(true);
            boolean exposed = M.exposeUnits();
            assertTrue(exposed);
        }



//    @Test
//    public void avoidPolitician() throws GameActionException{
//        setupTests();
//        Team player = Team.A;
//        Mockito.when(rc.getTeam()).thenReturn(player);
//        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
//
//        RobotInfo unitB = new RobotInfo(4,
//                rc.getTeam().opponent(),
//                RobotType.POLITICIAN,
//                10,
//                10,
//                new MapLocation(2, -2));
//
//        Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);
//        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
//        MapLocation location = new MapLocation(0,0);
//
            //failing i think bc of how directionTo works but I could be wrong
//        Direction pDirection = rc.getLocation().directionTo(unitB.location);

//        Mockito.when(rc.getLocation().directionTo(Mockito.anyObject())).thenReturn(pDirection);
//
//        RobotInfo[] nearbyUnits = new RobotInfo[]{unitB};
//
//        Mockito.when(rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent())).thenReturn(nearbyUnits);
//        boolean moved = M.avoidPolitician();
//        assertTrue(moved);
//    }
}






