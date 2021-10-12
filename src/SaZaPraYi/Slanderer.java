package SaZaPraYi;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Slanderer {
    private static RobotController rc;
    private static RobotUtils utils;

    /**
     * The constructor for the Slanderer controller object.
     *
     * @param _rc    The RobotController to use.
     * @param _utils The RobotUtils object to use.
     */
    public Slanderer(RobotController _rc, RobotUtils _utils) {
        rc = _rc;
        utils = _utils;
    }

    /**
     * The run function for the Slanderer controller object.
     *
     * @throws GameActionException if anything would cause one.
     */
    public void run() throws GameActionException {
        if (utils.tryMove(utils.randomDirection()))
            System.out.println("I moved!");
    }
}
