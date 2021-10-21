package SaZaPraYi;

import battlecode.common.*;

public class Muckraker {
    private static RobotController rc;
    private static RobotUtils utils;

    private static MapLocation locationOfBase;
    private static MapLocation locationOfEC;
    private static boolean goingToBase;
    private static int turnOfCooldown;

    private static final int NUMBER_OF_POLITICIANS_NEEDED_BEFORE_RETURN_TO_EC = 3;
    private static final int DISTANCE_BEFORE_SWITCHING_DIRECTIONS = 4;
    private static final int POLITICIAN_CONSIDERED_CLOSE_DISTANCE = 5;
    private static final int EC_COOLDOWN = 100;
    private static final double EC_DEFLAG_PERCENT_CHANCE = 0.5;

    /**
     * The constructor for the Muckraker controller object.
     *
     * @param _rc    The RobotController to use.
     * @param _utils The RobotUtils object to use.
     */
    public Muckraker(RobotController _rc, RobotUtils _utils) {
        rc = _rc;
        utils = _utils;
    }

    /**
     * The run function for the Muckraker controller object.
     *
     * @throws GameActionException if anything would cause one.
     */
    public void run() throws GameActionException {

        Team enemy = rc.getTeam().opponent();
        int senseRadius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;

        // If we don't have a home base location, we probably just got spawned,
        // and there is probably one nearby, so let's set it.
        if(locationOfBase == null)
            for(RobotInfo robot : rc.senseNearbyRobots(senseRadius))
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER)
                    locationOfBase = robot.getLocation();

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {

                // It's a slanderer... go get them
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);

//                    //After exposing one slanderer, friend speech get conviction
//                    double multi_factor = (1+0.001* robot.influence);
//                    int initroundnum = rc.getRoundNum();
//                    Team friend = rc.getTeam();
//                    while ( (rc.getRoundNum()-initroundnum) <50 ){
//                        for (RobotInfo bot: rc.senseNearbyRobots(actionRadius,friend)){
//                            if(bot.getType()== RobotType.POLITICIAN){
//                                //Multiplicative factor to totally convince
//                                System.out.println("Mutiplicatived");
//                            }
//                        }
//                    }

                    return;
                }
            }
        }

        if(rc.getFlag(rc.getID()) != RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal())
            utils.moveAwayFromOtherUnits();
        boolean shouldReturn = handleGreyECFollow();
        if(shouldReturn) return;

        //Avoid politician
        for (RobotInfo robot: rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemy)){
            if(robot.getType()== RobotType.POLITICIAN) {
                Direction enemy_loc = rc.getLocation().directionTo(robot.location);
                if (utils.tryMove(enemy_loc.opposite())) {
                    System.out.println("Moving away from ");
                }
            }
        }

        if (utils.tryMove(utils.randomDirection()))
            System.out.println("I moved!");
    }

    private boolean handleGreyECFollow() throws GameActionException {
        Team player = rc.getTeam();
        int senseRadius = rc.getType().sensorRadiusSquared;

        int currentFlag = rc.getFlag(rc.getID());
        if(currentFlag == RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
            int numberOfNearbyFriendlyPoliticians = 0;
            for(RobotInfo robot : rc.senseNearbyRobots(senseRadius, player)) {
                if(robot.getType() == RobotType.POLITICIAN)
                    if(rc.getLocation().distanceSquaredTo(robot.getLocation()) < POLITICIAN_CONSIDERED_CLOSE_DISTANCE)
                        numberOfNearbyFriendlyPoliticians++;
                if(robot.getType() == RobotType.MUCKRAKER
                        && rc.getFlag(robot.getID()) == RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
                    if(Math.random() < EC_DEFLAG_PERCENT_CHANCE) {
                        // cooldown flag
                        rc.setFlag(RobotUtils.flags.MUCKRAKER_EC_COOLDOWN.ordinal());
                        turnOfCooldown = rc.getRoundNum();
                        return false;
                    }
                }
            }
            if(numberOfNearbyFriendlyPoliticians > NUMBER_OF_POLITICIANS_NEEDED_BEFORE_RETURN_TO_EC) {
                goingToBase = false;
            }
            Direction toMove;
            if(goingToBase) {
                if(rc.getLocation().distanceSquaredTo(locationOfBase) < DISTANCE_BEFORE_SWITCHING_DIRECTIONS)
                    goingToBase = false;
                toMove = rc.getLocation().directionTo(locationOfBase);
            } else {
                if(rc.getLocation().distanceSquaredTo(locationOfEC) < senseRadius
                        && rc.senseRobotAtLocation(locationOfEC).getTeam() == player) {
                    rc.setFlag(RobotUtils.flags.NOTHING.ordinal());
                    return true;
                }
                if(rc.getLocation().distanceSquaredTo(locationOfEC) < DISTANCE_BEFORE_SWITCHING_DIRECTIONS)
                    goingToBase = true;
                toMove = rc.getLocation().directionTo(locationOfEC);
            }
            utils.tryMove(toMove);
            return true;
        }

        if(currentFlag == RobotUtils.flags.MUCKRAKER_EC_COOLDOWN.ordinal()) {
            if(rc.getRoundNum() > turnOfCooldown + EC_COOLDOWN)
                rc.setFlag(RobotUtils.flags.NOTHING.ordinal());
        }

        if(currentFlag == RobotUtils.flags.NOTHING.ordinal()) {
            for(RobotInfo robot : rc.senseNearbyRobots(senseRadius)) {
                if(robot.getTeam() == player) continue;
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    locationOfEC = robot.getLocation();
                    rc.setFlag(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
                    goingToBase = true;
                }
            }

        }
        return false;
    }

}
