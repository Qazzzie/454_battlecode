package SaZaPraYi;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;


public class EnlightenmentCenter {

    static final double DESIRED_SLANDERER_RATIO = 0.2;
    static final int MINIMUM_SLANDERERS = 5;
    int MINIMUM_UNIT_INFLUENCE = 10;
    int ROUNDS_BEFORE_START_VOTING = 300;

    private static RobotController rc;
    private static RobotUtils utils;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };


    /**
     * The constructor for the EnlightenmentCenter controller object.
     *
     * @param _rc    The RobotController to use.
     * @param _utils The RobotUtils object to use.
     */
    public EnlightenmentCenter(RobotController _rc, RobotUtils _utils) {
        rc = _rc;
        utils = _utils;
    }

    /**
     * The run function for the EnlightenmentCenter controller object.
     *
     * @throws GameActionException if anything would cause one.
     */
    public void run() throws GameActionException {
        buildRobots();
        bid();
    }

    private void buildRobots() throws GameActionException {
        
    }

    private void bid() throws GameActionException {
        int currentInfluence = rc.getInfluence();
        int turnCount = rc.getRoundNum();
        double bidRatio = getBidRatio(turnCount);
        int influenceToSpendOnBid = (int) (currentInfluence * bidRatio);
        if (turnCount > ROUNDS_BEFORE_START_VOTING)
            rc.bid(influenceToSpendOnBid);
    }

    /**
     * This function tells us how much we should bid on a vote.
     * I think a good bid function should not bid much early,
     * but bid a lot towards the end of the game.
     * This is a function that will need a <b>lot</b> of tweaking
     *
     * @param turnNumber The current turn number
     * @return the ratio of current influence we should bid
     */
    static double getBidRatio(int turnNumber) {
        // TODO: find an absolutely perfect bid function that makes mathematicians cry tears of joy.
        double x = turnNumber / 1500.0;
        // function i came up with: (4x^(0.7e+1))/5, at 1500 it is around 80%
        return Math.pow(x - 0.7, 5) + Math.pow(x - 0.2, 3) + 0.2;
        //return ((4.0 * Math.pow(x, 0.7 * Math.exp(1.0) + 1.0)) / 5.0);
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

}
