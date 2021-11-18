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


    /*
    If a Politician is closer than this distance to a muckraker with a grey EC
    flag, we try to move away from it by one tile.
    */
    private static final int DISTANCE_TOO_CLOSE_TO_MUCKRAKER = 3;

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
        int usableConviction = 0;
        double empowerFactor = rc.getEmpowerFactor(friend,0);

        // calculate conviction value that can be used to empower
        usableConviction = getUsableConviction(initialConviction,noofNearbyRobots,empowerFactor);

        //empower when Neutral EC is nearby
        empowerNeutralEC(senseRadius,actionRadius,neutralEC);

        //move towards enemy area to reduce their power when own power is less
        if (initialConviction <= CONVICTION_PENALTY){
            // check/sense for enemy robots within radius and move towards their direction
            for (RobotInfo robot: rc.senseNearbyRobots(senseRadius, enemy)){
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    int distanceToEC = rc.getLocation().distanceSquaredTo(robot.getLocation());
                    if(distanceToEC < actionRadius) {
                        if(rc.canEmpower(distanceToEC + 1)) rc.empower(distanceToEC + 1);
                    } else {
                        utils.tryMove(rc.getLocation().directionTo(robot.getLocation()));
                        return;
                    }
                }
                Direction enemy_location = rc.getLocation().directionTo(robot.location);
                if (utils.tryMove(enemy_location)){
                    //System.out.println("Moving towards enemy");
                }
            }
        }

        // Check if the number of enemies are present within attackable radius.
        // If found check for own conviction value and if the value is greater than 10 empower enemies.
        if(attackable.length != 0 && rc.canEmpower(actionRadius) && usableConviction > 0){
            //System.out.println("empowering...");
            if(rc.canEmpower(actionRadius)) rc.empower(actionRadius);
            //System.out.println("empowered");
            return;
        }

        // If there's a Muckraker nearby that has a Grey EC flag, let's follow it.
//        RobotInfo [] muckrakerToFollow = RobotUtils.senseRobotsWith(RobotType.MUCKRAKER, RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC, true);
        RobotInfo muckrakerToFollow = nearbyMuckrakerWithGreyECFlag();

        if(muckrakerToFollow != null) {
            // follow it
            MapLocation location = rc.getLocation();
            MapLocation muckrakersLocation = muckrakerToFollow.getLocation();
            Direction directionToMuckraker = location.directionTo(muckrakersLocation);
            int distanceToMuckraker = location.distanceSquaredTo(muckrakersLocation);
            utils.tryMove(directionToMuckraker);
            // If we're too close to the muckraker, move away
            if(distanceToMuckraker < DISTANCE_TOO_CLOSE_TO_MUCKRAKER) {
                utils.tryMove(directionToMuckraker.opposite());
            }
            return;
        }

        // Move away from other friendly units
        utils.moveAwayFromOtherUnits();

        // If no enemies are found nearby within defined round, convict own nearby team members after every defined interval of rounds.
        convictOwnTeam(usableConviction,actionRadius);


        /*
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
        */

        // If none of the above conditions are satisfied allow Politicians to move in random directions.
        utils.tryMove(utils.randomDirection());
        //if (utils.tryMove(utils.randomDirection()))
         //   System.out.println("I moved!");
    }

    /**
     * If there's a muckraker nearby that has a grey EC flag, return the RobotInfo object of
     * it, otherwise this returns null.
     *
     * @return the RobotInfo object of a nearby muckraker that has a grey EC flag, otherwise null.
     * @throws GameActionException if anything in here should cause one
     */
    public RobotInfo nearbyMuckrakerWithGreyECFlag() throws GameActionException {
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

    public int getUsableConviction(int initialConviction, int noofNearbyRobots, double empowerFactor){
        if (noofNearbyRobots > 0) {
            if (empowerFactor < 1){
                // when empower factor is less than 1 and if we multiply our conviction value it decreases the value
                // in such case set empowerFactor = 1 (as if we are not multiplying conviction value at all)
                empowerFactor = 1;
            }
            // usable conviction must be >0 to give a speech.
            return (int) (((initialConviction - CONVICTION_PENALTY) * empowerFactor) / noofNearbyRobots);
        }
        else {
            return (int) (initialConviction - CONVICTION_PENALTY);
        }
    }

    public void empowerNeutralEC(int senseRadius, int actionRadius, Team neutralEC) throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(senseRadius, neutralEC)) {
            if(rc.senseNearbyRobots(actionRadius, neutralEC).length > 0) {
                //System.out.println("empowering....");
                if(rc.canEmpower(actionRadius)) rc.empower(actionRadius);
                //System.out.println("empowered");
            } else {
                utils.tryMove(rc.getLocation().directionTo(robot.getLocation()));
                return;
            }
        }
    }

    public boolean convictOwnTeam(int usableConviction, int actionRadius) throws GameActionException {
        if (rc.getRoundNum() >= MINIMUM_ROUNDS_BEFORE_CONVICTION){
            if(rc.getRoundNum() % CONVICT_EVERY_N_ROUNDS == 0 && usableConviction > 0 && rc.canEmpower(actionRadius)){
                //System.out.println("empowering...");
                if(rc.canEmpower(actionRadius)) rc.empower(actionRadius);
                //System.out.println("empowered");
                return true;
            }
        }
        return false;
    }
}
