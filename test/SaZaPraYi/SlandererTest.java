package SaZaPraYi;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class SlandererTest {

    private RobotController rc;
    private RobotUtils utils;
    private Slanderer s;

    private void setupTests() {
        rc = Mockito.mock(RobotController.class);
        utils = new RobotUtils(rc);
        s = new Slanderer(rc, utils);
    }


    @Test
    public void testavoidEnemyTypePolitician() throws GameActionException {
        setupTests();
        RobotInfo enemy = new RobotInfo(
                100, Team.B, RobotType.POLITICIAN,
                10,10,
                new MapLocation(5,5)
                );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{enemy});
        Mockito.when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        //Mockito.when(rc.getTeam()).thenReturn(Team.B);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        boolean result = s.avoidEnemy(Team.B);
        assertTrue(result);
    }

    @Test
    public void testavoidEnemyTypeMuckracker() throws GameActionException {
        setupTests();
        RobotInfo enemy = new RobotInfo(
                100, Team.B, RobotType.MUCKRAKER,
                10,10,
                new MapLocation(5,5)
        );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{enemy});
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        //Mockito.when(rc.getTeam()).thenReturn(Team.B);
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        boolean result = s.avoidEnemy(Team.B);
        assertTrue(result);
    }

    @Test
    public void testavoidFlaggedSlanderer() throws GameActionException {
        setupTests();
        //int flag = 3;
        RobotInfo flaggedSlanderer = new RobotInfo(
                101, Team.A, RobotType.SLANDERER,
                10,10,
                new MapLocation(10,5)
        );
        Mockito.when(rc.senseNearbyRobots(Mockito.anyInt(), Mockito.any())).thenReturn(new RobotInfo[]{flaggedSlanderer});
        Mockito.when(rc.getType()).thenReturn(RobotType.SLANDERER);
        Mockito.when(rc.getFlag(flaggedSlanderer.ID))
                .thenReturn(RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal());
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        boolean result = s.avoidFlaggedSlanderer(Team.A);
        assertTrue(result);
    }

}

