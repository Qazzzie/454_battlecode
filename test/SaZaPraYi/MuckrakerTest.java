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



}
