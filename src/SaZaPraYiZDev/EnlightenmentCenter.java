package SaZaPraYiZDev;

import battlecode.common.*;

import java.util.ArrayList;


public class EnlightenmentCenter {

    /*
    DESIRED_SLANDERER_RATIO is the percentage of total units that
    should be nearby slanderers. The EC can only see nearby units
    40 units away, so it checks all of those, and if the number
    of slanderers is less than this ratio, it spawns a new one.
    This right now is 0.2f, or 20%. This may be a little high...
     */
    static final double DESIRED_SLANDERER_RATIO = 0.05;

    /*
    POLITICIAN_SPAWN_PERCENTAGE is the percent chance a Politician
    should be chosen to be spawned. Currently, it's 60%. If a Politician
    isn't chosen, a Muckraker is chosen.
     */
    static final double POLITICIAN_SPAWN_PERCENTAGE = 0.2;

    /*
    This is the minimum number of slanderers that should be around
    the EC. If there are less than 5 slanderers, it will make another
    slanderer, UNLESS the EC senses that it is under attack
    (because then it will spawn Politicians to defend)
     */
    static final int MINIMUM_SLANDERERS = 10;

    /*
    This is the minimum amount of influence that the EC should
    spend on a unit. This was chosen because according to the
    rules, Politicians with less than 10 influence will just
    explode without doing anything when they empower.
     */
    static final int MINIMUM_POLITICIAN_UNIT_INFLUENCE = 80;

    /*
    This is the minimum amount of influence that the EC should
    spend on a slanderer.
     */
    static final int MINIMUM_SLANDERER_UNIT_INFLUENCE = 40;

    /*
    This is the minimum amount of influence that the EC should
    spend on a muckraker.
    */
    private static final int MINIMUM_MUCKRAKER_UNIT_INFLUENCE = 1;

    /*
    Ratio used to make slanderers bigger based on current influence
     */
    static final double NORMAL_SLANDERER_INFLUENCE_RATIO = 0.75;

    /*
    Ratio used to make politicians bigger based on current influence
     */
    static final double NORMAL_POLITICIAN_INFLUENCE_RATIO = 0.75;

    /*
    Ratio used to make muckrakers bigger based on current influence
     */
    static final double NORMAL_MUCKRAKER_INFLUENCE_RATIO = 0.03;

    /*
    This is the number of rounds before the EC should start
    voting. I just randomly chose this as 300, but it should
    probably be adjusted (as well as getBidAmount function
    itself). Before round 300, no voting is done.
     */
    static final int ROUNDS_BEFORE_START_VOTING = 300;

    /*
    The number of rounds we should wait before having a "big vote" again.
    When this is 5, "big vote" every 5th round.
     */
    static final int VOTE_BIG_EVERY_N_ROUNDS = 4;

    /*
    When we have a "BIG VOTE", increase our influence to bid by this ratio
     */
    static final double BIG_VOTE_RATIO = 1.6;

    /*
    When spawning a Politician to defend, add an additional
    this many enemy unit average conviction costs to the total
    influence to spend on the defender.
    Think of this as a buffer that will cover the cost of
    2 additional enemy units that come into radius of the
    politician by the time it is ready to empower.
     */
    static final int DEFENDER_ADDITIONAL_UNIT_BUFFER = 2;

    private static RobotController rc;
    private static RobotUtils utils;

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
        // Build robots
        buildRobots();
        bid();
    }

    /**
     * This contains the logic for actually building robots
     *
     * @throws GameActionException if anything would cause one
     */
    private void buildRobots() throws GameActionException {
        Team playerTeam = rc.getTeam();
        Team enemyTeam = playerTeam.opponent();

        RobotType robotTypeToBuild;
        int influenceToSpendOnUnit;

        int senseRadius = rc.getType().sensorRadiusSquared;

        // Initializing nearby robot lists
        ArrayList<RobotInfo> nearbyAlliedRobots = new ArrayList<>();
        ArrayList<RobotInfo> nearbyEnemyRobots = new ArrayList<>();
        ArrayList<RobotInfo> nearbyGreyRobots = new ArrayList<>();

        // Sort nearby robots into the above lists
        for(RobotInfo robot : rc.senseNearbyRobots(senseRadius)) {
            if(robot.getTeam() == playerTeam) nearbyAlliedRobots.add(robot);
            else if(robot.getTeam() == enemyTeam) nearbyEnemyRobots.add(robot);
            else nearbyGreyRobots.add(robot);
        }

        if(nearbyGreyRobots.size() > 0) {
            robotTypeToBuild = RobotType.POLITICIAN;
            influenceToSpendOnUnit = nearbyGreyRobots.get(0).conviction;
            // The influence to spend on a Politician should be at least the minimum influence.
            influenceToSpendOnUnit = Math.max(influenceToSpendOnUnit, MINIMUM_POLITICIAN_UNIT_INFLUENCE);
        }

        // If there's enemies nearby, spawn a Politician defender
        else if(nearbyEnemyRobots.size() > 0) {
            robotTypeToBuild = RobotType.POLITICIAN;
            int totalConvictionOfNearbyEnemyUnits = 0;
            for(RobotInfo enemy : nearbyEnemyRobots) {
                totalConvictionOfNearbyEnemyUnits += enemy.conviction;
            }
            int averageConviction = totalConvictionOfNearbyEnemyUnits / nearbyEnemyRobots.size();
            influenceToSpendOnUnit =
                        totalConvictionOfNearbyEnemyUnits + (averageConviction * DEFENDER_ADDITIONAL_UNIT_BUFFER);
            // The influence to spend on a defender Politician should be at least the minimum influence.
            influenceToSpendOnUnit = Math.max(influenceToSpendOnUnit, MINIMUM_POLITICIAN_UNIT_INFLUENCE);
        // Otherwise, spawn units as normal
        } else {
            // If we don't have enough slanderers, spawn one
            int currentNumberOfRobots = rc.getRobotCount();
            int currentNumberOfNearbySlanderers = 0;
            for(RobotInfo robot : nearbyAlliedRobots)
                if(robot.getType() == RobotType.SLANDERER)
                    currentNumberOfNearbySlanderers++;
            double currentSlandererToTotalUnitRatio = (double) currentNumberOfNearbySlanderers / currentNumberOfRobots;
            if(currentSlandererToTotalUnitRatio < DESIRED_SLANDERER_RATIO
                    || currentNumberOfNearbySlanderers < MINIMUM_SLANDERERS) {
                robotTypeToBuild = RobotType.SLANDERER;
                influenceToSpendOnUnit = MINIMUM_SLANDERER_UNIT_INFLUENCE;
                influenceToSpendOnUnit += rc.getInfluence() * NORMAL_SLANDERER_INFLUENCE_RATIO; // TODO fix this
            // Otherwise, spawn a Politician or Muckraker
            } else {
                robotTypeToBuild = randomSpawnableRobotType();
                switch(robotTypeToBuild) {
                    case MUCKRAKER:
                        influenceToSpendOnUnit = MINIMUM_MUCKRAKER_UNIT_INFLUENCE;
                        influenceToSpendOnUnit += rc.getInfluence() * NORMAL_MUCKRAKER_INFLUENCE_RATIO; // TODO fix this
                        break;
                    case POLITICIAN:
                    default:
                        influenceToSpendOnUnit = MINIMUM_POLITICIAN_UNIT_INFLUENCE;
                        influenceToSpendOnUnit += rc.getInfluence() * NORMAL_POLITICIAN_INFLUENCE_RATIO; // TODO fix this
                        break;
                }
            }
        }
        // Actually spawn the robot
        Direction directionToPlace = utils.getDirectionOfRandomAdjacentEmptyTile(rc.getLocation());
        if(directionToPlace != null) {
            if(rc.canBuildRobot(robotTypeToBuild, directionToPlace, influenceToSpendOnUnit)) {
                rc.buildRobot(robotTypeToBuild, directionToPlace, influenceToSpendOnUnit);
            }
        }
    }


    /**
     * This contains the logic for actually making bids
     *
     * @throws GameActionException if anything would cause one
     */
    private void bid() throws GameActionException {
        int currentInfluence = rc.getInfluence();
        int turnCount = rc.getRoundNum();
        double bidRatio = getBidRatio(turnCount);
        int influenceToSpendOnBid = (int) (currentInfluence * bidRatio);
        // Should we have a BIG VOTE this round..?
        if (turnCount % VOTE_BIG_EVERY_N_ROUNDS == 0) influenceToSpendOnBid *= BIG_VOTE_RATIO;
        influenceToSpendOnBid = Math.min(influenceToSpendOnBid, currentInfluence);
        // This prevents spending a negative amount on a bid
        if(influenceToSpendOnBid <= 0) return;
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
        //double dampener = 0.8; // TODO: remove this, this is just a temporary dampener
        return ((4.0 * Math.pow(x, 0.7 * Math.exp(1.0) + 1.0)) / 5.0);
    }

    /**
     * Returns either a Politician or Muckraker based on static ratios above.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        double randomValue = Math.random();
        if(randomValue < POLITICIAN_SPAWN_PERCENTAGE) return RobotType.POLITICIAN;
        return RobotType.MUCKRAKER;
    }

}
