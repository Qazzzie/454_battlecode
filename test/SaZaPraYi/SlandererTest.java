package SaZaPraYi;
import static org.junit.Assert.*;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

public class SlandererTest {
    private RobotController rc;
    private RobotUtils utils;
    private Slanderer S;
    private void setupTests() {
        rc = Mockito.mock(RobotController.class);
        utils = new RobotUtils(rc);
        S = new Slanderer(rc, utils);
    }

    @Test
    public void testAvoidSlandererFlagging()throws GameActionException {
        setupTests();
        Team player = Team.A;
        Mockito.when(rc.getTeam()).thenReturn(player);
        Mockito.when(rc.getType()).thenReturn(RobotType.SLANDERER);
        RobotInfo unitA = new RobotInfo(3,
                rc.getTeam(),
                RobotType.SLANDERER,
                10,
                10,
                new MapLocation(0, 3));
        RobotInfo unitB = new RobotInfo(4,
                rc.getTeam(),
                RobotType.SLANDERER,
                10,
                10,
                new MapLocation(2, -2));

        Mockito.when(rc.getFlag(unitB.getID())).thenReturn(RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal());

        Mockito.when(rc.senseRobotAtLocation(unitA.location)).thenReturn(unitA);
        Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);
        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
        MapLocation location = new MapLocation(0,0);
        Mockito.when(rc.getLocation()).thenReturn(location);
        RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};
        int senseRadius = rc.getType().sensorRadiusSquared;
        Mockito.when(rc.senseNearbyRobots(senseRadius, player)).thenReturn(nearbyUnits);
        boolean moved = S.avoidSlandererFlagging();
        assertTrue(moved);
    }

//    @Test
//    public void testAvoidEnemy() throws GameActionException{
//        setupTests();
//        Team player = Team.A;
//        Mockito.when(rc.getTeam()).thenReturn(player);
//        Mockito.when(rc.getType()).thenReturn(RobotType.SLANDERER);
//        RobotInfo unitA = new RobotInfo(3,
//                rc.getTeam(),
//                RobotType.SLANDERER,
//                10,
//                10,
//                new MapLocation(0, 3));
//        RobotInfo unitB = new RobotInfo(4,
//                rc.getTeam().opponent(),
//                RobotType.SLANDERER,
//                10,
//                10,
//                new MapLocation(2, -2));
//
//        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
//        MapLocation location = new MapLocation(0,0);
//        Mockito.when(rc.getLocation()).thenReturn(location);
//
//        Direction EDirection = rc.getLocation().directionTo(unitB.location).opposite();
//        Mockito.when(rc.getLocation().directionTo(unitB.location)).thenReturn(EDirection);
//
//        RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};
//        int senseRadius = rc.getType().sensorRadiusSquared;
//        Mockito.when(rc.senseNearbyRobots(senseRadius, player)).thenReturn(nearbyUnits);
//        boolean moved = S.avoidEnemy();
//        assertTrue(moved);
//    }
}

