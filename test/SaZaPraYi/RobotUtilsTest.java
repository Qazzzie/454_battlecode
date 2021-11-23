package SaZaPraYi;

import battlecode.common.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class RobotUtilsTest {

    private RobotController rc;
    private RobotUtils utils;
    private EnlightenmentCenter ec;

    private void setupTests() {
        rc = Mockito.mock(RobotController.class);
        utils = new RobotUtils(rc);
        ec = new EnlightenmentCenter(rc, utils);
    }

    @Test
    public void testIsTouchingTheWallReturnsTrueForTouchingTheWall() throws GameActionException {
        setupTests();
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        assertTrue(utils.isTouchingTheWall());
    }

    @Test
    public void getDirectionOfRandomAdjacentLocationReturnsNull() throws GameActionException {
        setupTests();
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        assertNull(utils.getDirectionOfRandomAdjacentEmptyTile(rc.getLocation()));
    }

    @Test
    public void tryMoveReturnsFalseForBadLocation() throws GameActionException {
        setupTests();
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        assertFalse(utils.tryMove(Direction.NORTH));
    }

    @Test
    public void randomDirectionIsNotNull() throws GameActionException {
        setupTests();
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        assertNotNull(utils.randomDirection());
    }

    @Test
    public void moveAwayFromOtherUnitsMovesRobotAway() throws GameActionException {
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
                rc.getTeam(),
                RobotType.POLITICIAN,
                10,
                10,
                new MapLocation(2, -2));
        Mockito.when(rc.senseRobotAtLocation(unitA.location)).thenReturn(unitA);
        Mockito.when(rc.senseRobotAtLocation(unitB.location)).thenReturn(unitB);
        Mockito.when(rc.canMove(Mockito.any())).thenReturn(true);
        MapLocation location = new MapLocation(0,0);
        Mockito.when(rc.getLocation()).thenReturn(location);
        RobotInfo[] nearbyUnits = new RobotInfo[]{unitA, unitB};
        int senseRadius = rc.getType().sensorRadiusSquared;
        Mockito.when(rc.senseNearbyRobots(senseRadius, player)).thenReturn(nearbyUnits);
        boolean moved = utils.moveAwayFromOtherUnits();
        assertTrue(moved);
    }

    @Test
    public void TestSenseRobotsWith() throws GameActionException{
        setupTests();

        Team player = Team.A;
        int senseRadius = RobotType.MUCKRAKER.sensorRadiusSquared;
        RobotInfo unitA = new RobotInfo(3,
                player,
                RobotType.MUCKRAKER,
                10,
                10,
                new MapLocation(0, 3));

        RobotInfo[] nearbyUnits = new RobotInfo[]{unitA};

        Mockito.when(rc.senseNearbyRobots(senseRadius, player.opponent())).thenReturn(nearbyUnits);
        Mockito.when(rc.getFlag(unitA.getID())).thenReturn(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
        Mockito.when(rc.getTeam()).thenReturn(player);
        Mockito.when(rc.getType()).thenReturn(RobotType.MUCKRAKER);

        RobotInfo [] unitZ = new RobotInfo[]{};

        unitZ = RobotUtils.senseRobotsWith(RobotType.MUCKRAKER, RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC, false);

        assertEquals(unitZ[0].influence , 10);
    }
}
