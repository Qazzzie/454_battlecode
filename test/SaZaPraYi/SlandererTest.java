package SaZaPraYi;
import static org.junit.Assert.*;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
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
    public void testAvoid()throws GameActionException {
        setupTests();
        Mockito.when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        assertTrue(utils.isTouchingTheWall());
    }
}

