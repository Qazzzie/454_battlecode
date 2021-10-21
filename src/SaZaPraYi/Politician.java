package SaZaPraYi;

import battlecode.common.*;

public class Politician {

    /*
    This is the number of conviction unit that is lost from the available conviction
    unit whenever a politician gives a speech.
    (i.e. Whenever a politician gives a speech its 10 units are lost and only
    remaining conviction units are distributed among listener robots.)
    */
    static final int CONVICTION_PENALTY = 10;

    /*
    This is the minimum number of rounds a politician waits before it starts to
    convict its own team members even when there are no enemies present. This is
    a randomly picked round number to start with, which can change as the game progresses.
    */
    static final int MINIMUM_ROUNDS_BEFORE_CONVICTION = 150;

    /*
    Politician can convict its own team after 150 rounds. We don't want the politicians to
    perform conviction on its team member on all rounds. But convicting own team members
    and influencing them increases their loyalty towards the team. So, after 150th round
    of the game, politician tries to convict its team member, every 10th round.
    */
    static final int CONVICT_EVERY_N_ROUNDS = 10;

    private static RobotController rc;
    private static RobotUtils utils;

    /**
     * The constructor for the Politician controller object.
     *
     * @param _rc    The RobotController to use.
     * @param _utils The RobotUtils object to use.
     */
    public Politician(RobotController _rc, RobotUtils _utils) {
        rc = _rc;
        utils = _utils;
    }

    /**
     * The run function for the Politician controller object.
     *
     * @throws GameActionException if anything would cause one.
     */
    public void run() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        Team friend = rc.getTeam();
        Team neutralEC = Team.NEUTRAL;

        int actionRadius = rc.getType().actionRadiusSquared;
        int senseRadius = rc.getType().sensorRadiusSquared;

        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] friendly = rc.senseNearbyRobots(actionRadius,friend);
        RobotInfo[] allrobots = rc.senseNearbyRobots(actionRadius);

        int noofNearbyRobots = attackable.length + friendly.length;
        int initialConviction = rc.getConviction();
        int useableConviction = 0;
        if (noofNearbyRobots > 0) {
            // usable conviction must be >0 to give a speech.
            useableConviction = (initialConviction - CONVICTION_PENALTY) / noofNearbyRobots;
        }

        //empower when Neutral EC is nearby
        for(RobotInfo robot : rc.senseNearbyRobots(senseRadius, neutralEC)) {
            if(rc.senseNearbyRobots(actionRadius, neutralEC).length > 0) {
                System.out.println("empowering....");
                rc.empower(actionRadius);
                System.out.println("empowered");
            } else {
                utils.tryMove(rc.getLocation().directionTo(robot.getLocation()));
                return;
            }
        }

        //move towards enemy area to reduce their power when own power is less
        if (useableConviction <= CONVICTION_PENALTY){
            // check/sense for enemy robots within radius and move towards their direction
            for (RobotInfo robot: rc.senseNearbyRobots(senseRadius, enemy)){
                Direction enemy_location = rc.getLocation().directionTo(robot.location);
                if (utils.tryMove(enemy_location)){
                    System.out.println("Moving towards enemy");
                }
            }
        }

        // Check if the number of enemies are present within attackable radius.
        // If found check for own conviction value and if the value is greater than 10 empower enemies.
        if(attackable.length != 0 && rc.canEmpower(actionRadius) && useableConviction > 0){
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }


        RobotInfo muckrakerToFollow = nearbyMuckrakerWithGreyECFlag();
        if(muckrakerToFollow != null) {
            // follow it
            utils.tryMove(rc.getLocation().directionTo(muckrakerToFollow.getLocation()));
            return;
        }

        utils.moveAwayFromOtherUnits();

        // If no enemies are found nearby within defined round, convict own nearby team members after every defined interval of rounds.
        if (rc.getRoundNum() >= MINIMUM_ROUNDS_BEFORE_CONVICTION){
            if(rc.getRoundNum() % CONVICT_EVERY_N_ROUNDS == 0 && useableConviction > 0 && rc.canEmpower(actionRadius)){
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }

        }


        /*
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
        */

        // If none of the above conditions are satisfied allow Politicians to move in random directions.
        if (utils.tryMove(utils.randomDirection()))
            System.out.println("I moved!");
    }

    private RobotInfo nearbyMuckrakerWithGreyECFlag() throws GameActionException {
        int senseRadius = rc.getType().sensorRadiusSquared;
        for(RobotInfo robot : rc.senseNearbyRobots(senseRadius)) {
            if(robot.getType() == RobotType.MUCKRAKER) {
                if(rc.getFlag(robot.getID()) == RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
                    return robot;
                }
            }
        }
        return null;
    }

}
