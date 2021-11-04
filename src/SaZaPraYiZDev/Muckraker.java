package SaZaPraYiZDev;

import battlecode.common.*;

import java.util.ArrayList;

public class Muckraker {
    private static RobotController rc;
    private static RobotUtils utils;

    // The location of our home base, set when spawned
    private static MapLocation locationOfBase;

    // The location of a convertable EC we found
    private static MapLocation locationOfEC;

    // True if we are heading back to base, false otherwise (grey EC logic)
    private static boolean goingToBase;

    // Turn where we got de-flagged (grey EC logic)
    private static int turnOfCooldown;

    // This is the turn a muckraker started its grey EC quest.
    private static int turnStartedGreyECQuest;


    // This is the number of nearby politicians a muckraker who
    // found a grey EC should have around them before they start
    // heading back to the location of the grey EC. Three politicians
    // nearby seems good.
    private static final int NUMBER_OF_POLITICIANS_NEEDED_BEFORE_RETURN_TO_EC = 3;


    // How close to the EC should we be before we stop and go
    // the other way?
    private static final int DISTANCE_BEFORE_SWITCHING_DIRECTIONS = 4;

    // If the distance to a politician is shorter than this, it's considered close
    private static final int POLITICIAN_CONSIDERED_CLOSE_DISTANCE = 5;


    // The number of rounds a Muckraker should wait after it's
    // turned off the Grey EC color because there was another muckraker
    // with that color near it. 100 rounds seems long enough for the
    // other Muckraker to convert the EC.
    private static final int EC_COOLDOWN = 100;

    // The percent chance where a muckraker should de-flag if there's another
    // muckraker nearby. Right now this is 50%
    private static final double EC_DEFLAG_PERCENT_CHANCE = 0.5;

    // After this many turns, a muckraker with the grey EC flag should deflag itself.
    private static final int TURNS_BEFORE_SELF_DEFLAG = 400;

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

        exposeUnits();

        gaurdEnemyEC();

        // If we aren't doing grey EC stuff, space out from other units.
        if(rc.getFlag(rc.getID()) != RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal())
            utils.moveAwayFromOtherUnits();

        // Do grey EC logic. Return if that's what we should do
        boolean shouldReturn = handleGreyECFollow();
        if(shouldReturn) return;

        //Avoid politician
        for (RobotInfo robot: rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemy)){
            if(robot.getType()== RobotType.POLITICIAN) {
                Direction enemy_loc = rc.getLocation().directionTo(robot.location);
                if (utils.tryMove(enemy_loc.opposite())) {
                    //System.out.println("Moving away from ");
                }
            }
        }

        utils.tryMove(utils.randomDirection());
        //if (utils.tryMove(utils.randomDirection()))
        //    System.out.println("I moved!");
    }

    /**
     * This function handles the logic for the convertable EC flag.
     *
     * @return true if we should return from run(), otherwise false.
     * @throws GameActionException if anything in here should cause one
     */
    private boolean handleGreyECFollow() throws GameActionException {
        Team player = rc.getTeam();
        int senseRadius = rc.getType().sensorRadiusSquared;

        int currentFlag = rc.getFlag(rc.getID());
        if(currentFlag == RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
            // If we haven't finished our grey EC quest by a certain round, deflag.
            // This prevents permanently stuck muckrakers and politicians.
            if(rc.getRoundNum() > turnStartedGreyECQuest + TURNS_BEFORE_SELF_DEFLAG) {
                rc.setFlag(RobotUtils.flags.MUCKRAKER_EC_COOLDOWN.ordinal());
                return false;
            }
            // Scan nearby friendly robots for Politicians and other muckrakers with a grey EC flag.
            int numberOfNearbyFriendlyPoliticians = 0;
            for(RobotInfo robot : rc.senseNearbyRobots(senseRadius, player)) {

                if(robot.getType() == RobotType.POLITICIAN)
                    // If a friendly politician is close enough, add it to the number.
                    if(rc.getLocation().distanceSquaredTo(robot.getLocation()) < POLITICIAN_CONSIDERED_CLOSE_DISTANCE)
                        numberOfNearbyFriendlyPoliticians++;

                // If there's a friendly muckraker nearby competing with us, de-flag with a percent chance.
                if(robot.getType() == RobotType.MUCKRAKER
                        && rc.getFlag(robot.getID()) == RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
                    if(Math.random() < EC_DEFLAG_PERCENT_CHANCE) {
                        // We should set our cooldown flag, and the turn we got de-flagged.
                        rc.setFlag(RobotUtils.flags.MUCKRAKER_EC_COOLDOWN.ordinal());
                        turnOfCooldown = rc.getRoundNum();
                        return false;
                    }
                }
            }

            // If we have enough politicians nearby, lets start going back to the grey EC.
            if(numberOfNearbyFriendlyPoliticians > NUMBER_OF_POLITICIANS_NEEDED_BEFORE_RETURN_TO_EC) {
                goingToBase = false;
            }

            // Move either towards the base or the grey EC
            Direction toMove;
            if(goingToBase) {
                if(rc.getLocation().distanceSquaredTo(locationOfBase) < DISTANCE_BEFORE_SWITCHING_DIRECTIONS)
                    goingToBase = false;
                toMove = rc.getLocation().directionTo(locationOfBase);
            } else {
                // If we successfully converted the EC, turn back to a normal muckraker
                if(rc.getLocation().distanceSquaredTo(locationOfEC) < senseRadius
                        && rc.senseRobotAtLocation(locationOfEC).getTeam() == player) {
                    rc.setFlag(RobotUtils.flags.NOTHING.ordinal());
                    return true;
                }
                if(rc.getLocation().distanceSquaredTo(locationOfEC) < DISTANCE_BEFORE_SWITCHING_DIRECTIONS)
                    goingToBase = true;
                toMove = rc.getLocation().directionTo(locationOfEC);
            }
            // Move in the direction we picked
            utils.tryMove(toMove);
            return true;
        }

        // If it's been long enough and we have a cooldown flag, go back to normal.
        if(currentFlag == RobotUtils.flags.MUCKRAKER_EC_COOLDOWN.ordinal()) {
            if(rc.getRoundNum() > turnOfCooldown + EC_COOLDOWN)
                rc.setFlag(RobotUtils.flags.NOTHING.ordinal());
        }

        // If there's a grey EC near us and we arent cooldowned, set our flag and head to base.
        if(currentFlag == RobotUtils.flags.NOTHING.ordinal()) {
            for(RobotInfo robot : rc.senseNearbyRobots(senseRadius)) {
                if(robot.getTeam() == player) continue;
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    locationOfEC = robot.getLocation();
                    rc.setFlag(RobotUtils.flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
                    turnStartedGreyECQuest = rc.getRoundNum();
                    goingToBase = true;
                }
            }

        }
        return false;
    }


    /**
     * This function handles the logic for the Gaurding enemy EC's.
     *
     * @return Returns false if there is NONE of the following:
     * Enemy EC with empty spaces
     * Ally muckraker with MUCKRAKER_FOUND_ENEMY_EC nearby
     * otherwise true, and muckraker prioritizes clustering around Enemy EC
     * @throws GameActionException if anything in here should cause one
     */
    private boolean gaurdEnemyEC() throws GameActionException {
        //Dp we have backup? if so, raise flag

        int nearby_muckrakers = 0;
        MapLocation [] robotsNearby =  rc.detectNearbyRobots();
        for (RobotInfo allyMuckraker : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam())) {
            if (allyMuckraker.getType() == RobotType.MUCKRAKER) {
                nearby_muckrakers++;
            }
        }

        if(nearby_muckrakers > 5 && nearby_muckrakers < 8) {
            //find nearby enemy EC's
            for (RobotInfo enemy_i : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent())) {
                if (enemy_i.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    rc.setFlag(RobotUtils.flags.MUCKRAKER_FOUND_ENEMY_EC.ordinal());
                    // create list of empty spots near enemy EC
                    Direction enemy_ec_direction = rc.getLocation().directionTo(enemy_i.location);
                    MapLocation enemy_ec_location = enemy_i.location;
                    System.out.println("Enemy EC spotted!");
                    ArrayList<MapLocation> nearbyAdjacentTiles = new ArrayList<>();
                    // First, put all the adjacent nearby tiles into a list...
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            if (j == 0 && i == 0) continue; // This would be the tile itself...
                            MapLocation temp = new MapLocation(enemy_ec_location.x + i, enemy_ec_location.y + j);

                            if (rc.canSenseLocation(temp)) {
                                if (!rc.onTheMap(temp)) continue; //not on the map
                                if (rc.getLocation() == temp) {//we are already on an empty spot
                                    System.out.println("Defending Enemy EC!");
                                    exposeUnits();//
                                    return true;
                                }
                                if (rc.senseRobotAtLocation(temp) == null)
                                    nearbyAdjacentTiles.add(temp);
                            }
                        }
                    }
                    //move towards empty spots by enemy EC
                    if (nearbyAdjacentTiles.size() > 0) {
                        for (int i = 0; i <= nearbyAdjacentTiles.size(); i++) {
                            if (utils.tryMove(rc.getLocation().directionTo(nearbyAdjacentTiles.get(i)))) {
                                System.out.println("moving towards enemy EC");
                                exposeUnits();
                                return true;
                            } else if (utils.tryMove(utils.getDirectionOfRandomAdjacentEmptyTile(nearbyAdjacentTiles.get(i)))) {
                                System.out.println("moving towards enemy EC, excuse me");
                                exposeUnits();
                                return true;
                            }
                        }
                    }
                } else rc.setFlag(RobotUtils.flags.NOTHING.ordinal());
            }
        }

        //follow muckrakers with their flags up
        for (RobotInfo allyMuckraker : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam())) {
            if (allyMuckraker.getType() == RobotType.MUCKRAKER && rc.getFlag(allyMuckraker.getID()) == RobotUtils.flags.MUCKRAKER_FOUND_ENEMY_EC.ordinal() ){
                if(utils.tryMove(rc.getLocation().directionTo(allyMuckraker.location))){
                    System.out.println("Following ally muckraker to Enemy EC!");
                    return true;
                }
            }
        }
        return false;
    }

    // expose nearby units
    private boolean exposeUnits() throws GameActionException{
        Team enemy = rc.getTeam().opponent();
        int senseRadius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;

//        MapLocation [] robotsNearby =  rc.detectNearbyRobots();

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {

                // It's a slanderer... go get them
                if (rc.canExpose(robot.location)) {
                    //System.out.println("e x p o s e d");
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

                    return true;
                }
            }
        }
        return false;
    }

}
