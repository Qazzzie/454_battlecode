package SaZaPraYiZDev;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {

    static RobotController rc;
    static EnlightenmentCenter enlightenmentCenterController;
    static Muckraker muckrakerController;
    static Slanderer slandererController;
    static Politician politicianController;
    static RobotUtils robotUtils;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // A RobotUtils object is created, taking the RobotController as a
        // parameter. This will be passed into the individual controller
        // objects.
        robotUtils = new RobotUtils(rc);

        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        runEnlightenmentCenter();
                        break;
                    case POLITICIAN:
                        runPolitician();
                        break;
                    case SLANDERER:
                        runSlanderer();
                        break;
                    case MUCKRAKER:
                        runMuckraker();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                //System.out.println(rc.getType() + " Exception");
                //e.printStackTrace();
            }
        }
    }

    /**
     * This function calls the run() method of the EnlightmentCenter object,
     * initializing it if it is being ran for the first time.
     *
     * @throws GameActionException If run() throws a GameActionException,
     *                             it is passed up and thrown.
     */
    static void runEnlightenmentCenter() throws GameActionException {
        if (enlightenmentCenterController == null)
            enlightenmentCenterController = new EnlightenmentCenter(rc, robotUtils);
        enlightenmentCenterController.run();
    }

    /**
     * This function calls the run() method of the Politician object,
     * initializing it if it is being ran for the first time.
     *
     * @throws GameActionException If run() throws a GameActionException,
     *                             it is passed up and thrown.
     */
    static void runPolitician() throws GameActionException {
        if (politicianController == null)
            politicianController = new Politician(rc, robotUtils);
        politicianController.run();
    }

    /**
     * This function calls the run() method of the Slanderer object,
     * initializing it if it is being ran for the first time.
     *
     * @throws GameActionException If run() throws a GameActionException,
     *                             it is passed up and thrown.
     */
    static void runSlanderer() throws GameActionException {
        if (slandererController == null)
            slandererController = new Slanderer(rc, robotUtils);
        slandererController.run();
    }

    /**
     * This function calls the run() method of the Muckraker object,
     * initializing it if it is being ran for the first time.
     *
     * @throws GameActionException If run() throws a GameActionException,
     *                             it is passed up and thrown.
     */
    static void runMuckraker() throws GameActionException {
        if (muckrakerController == null) {
            muckrakerController = new Muckraker(rc, robotUtils);
        }
        muckrakerController.run();
    }

}
