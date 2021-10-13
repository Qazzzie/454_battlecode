package samtest1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    private enum flags {
        NOTHING,
        MUCKRAKER_FOUND_GREY_EC,
        MUCKRAKER_EC_COOLDOWN
    }

    static int turnCount;

    // *** GENERAL ROBOT GLOBALS ***
    // The location of a robot's home base
    private static MapLocation homeBaseLocation;
    // The ID of a robot's home base
    private static int homeBaseId;
    // The location a robot was in last turn
    private static MapLocation lastTurnsLocation;

    // *** SLANDERER GLOBALS ***
    // TODO: change all of this when we refactor the Slanderer movement
    // For slanderers, this is the main direction they should move in
    private static Direction mainDirection;
    // The direction moved in from last turn
    private static Direction lastTurnsDirection;
    // The percent chance a Slanderer should move in their main direction
    private static final float moveInMainDirectionPercent = 0.8f;

    // *** MUCKRAKER GLOBALS ***
    // For muckrakers, this is the location where they last killed
    // a slanderer
    private static MapLocation lastExposedSlandererLocation;
    // For muckrakers, this is the location of the greyEC they found
    private static MapLocation greyECLocation;
    // for muckrakers, this is the turn they got de-flagged
    private static int turnOfCooldown;


    // *** ENLIGHTENMENT CENTER GLOBALS ***
    // For ECs, this flag is true if they recently saw an enemy near the base
    private static boolean sawAnEnemyNearBase;


    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {


        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {

        /*
        DESIRED_SLANDERER_RATIO is the percentage of total units that
        should be nearby slanderers. The EC can only see nearby units
        40 units away, so it checks all of those, and if the number
        of slanderers is less than this ratio, it spawns a new one.
        This right now is 0.2f, or 20%. This may be a little high...
         */
        float DESIRED_SLANDERER_RATIO = 0.2f;

        /*
         This is the minimum number of slanderers that should be around
         the EC. If there are less than 5 slanderers, it will make another
         slanderer, UNLESS the EC senses that it is under attack
         (because then it will spawn Politicians to defend)
         */
        int MINIMUM_SLANDERERS = 5;

        /*
        This is the minimum amount of influence that the EC should
        spend on a unit. This was chosen because according to the
        rules, Politicians with less than 10 influence will just
        explode without doing anything when they empower.
         */
        int MINIMUM_UNIT_INFLUENCE = 10;

        /*
        This is the number of rounds before the EC should start
        voting. I just randomly chose this as 300, but it should
        probably be adjusted (as well as getBidAmount function
        itself). Before round 300, no voting is done.
         */
        int ROUNDS_BEFORE_START_VOTING = 300;

        /*
        The initialization of the toBuild robot type. I modified
        this slightly to pick only Muckrakers and Politicians, see
        the function implementation
         */
        RobotType toBuild = randomSpawnableRobotType();

        // Initial influence to spend on the unit, this gets changed
        int influence_to_spend_on_unit = 30;

        // Player and enemy teams
        Team player_team = rc.getTeam();
        Team enemy_team = player_team.opponent();

        // Nearby robots that can be empowered by Politicians
        ArrayList<RobotInfo> nearby_empowerables = getNearbyEmpowerables();

        // If there's robot(s) nearby that can be empowered, lets make a
        // Politician that's big enough to kill them.
        if (nearby_empowerables.size() > 0) {
            // We build a politican to go empower it
            toBuild = RobotType.POLITICIAN;

            // Below, we are getting the average conviction of nearby
            // empowerable units
            int total_conviction = 0;
            for (RobotInfo empowerable : nearby_empowerables) {
                // Along the way, if we see an enemy, update the
                // sawAnEnemyNearBase variable to use later
                if (empowerable.getTeam() == enemy_team) sawAnEnemyNearBase = true;
                total_conviction += empowerable.conviction;
            }
            int average_conviction = total_conviction / nearby_empowerables.size();

            // Our Politician should be big enough to empower everything nearby
            // the base, with an extra amount in case 2 new units come along
            // before next turn.
            influence_to_spend_on_unit = total_conviction + (average_conviction * 2);

            // Otherwise, let's spawn a unit like normal
        } else {
            int current_number_of_units = rc.getRobotCount();
            int current_sense_radius = rc.getType().sensorRadiusSquared;

            // We want to know how many allied slanderers are near us,
            // in case we need to make more. We loop through the nearby
            // allied units and see which ones are slanderers, and accumulate.
            int current_number_of_nearby_slanderers = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(current_sense_radius, player_team)) {
                if (robot.getType() == RobotType.SLANDERER) {
                    current_number_of_nearby_slanderers++;
                }
            }

            // Calculating the current ratio of current nearby slanderers
            // to total units on the board
            float current_slanderer_ratio = (float) current_number_of_nearby_slanderers / current_number_of_units;

            // If that ratio is too low, let's make more Slanderers.
            if (current_slanderer_ratio < DESIRED_SLANDERER_RATIO || current_number_of_nearby_slanderers < MINIMUM_SLANDERERS) {
                toBuild = RobotType.SLANDERER;
                // TODO: change this to a variable. This number should be tweaked
                influence_to_spend_on_unit *= 4.7;
            }
        }

        // Only make units if they are bigger than the minimum unit influence,
        // which is 10 (don't make politicians smaller than 10)
        if (influence_to_spend_on_unit > MINIMUM_UNIT_INFLUENCE) {
            // Politician influence ratio
            // TODO: change this to a variable. This is just a random number
            if (toBuild == RobotType.POLITICIAN) influence_to_spend_on_unit *= 2.4;

            // Spawn our units in each of our 4 directions.
            for (Direction dir : directions) {
                MapLocation tile_to_place = rc.adjacentLocation(dir);
                if (!rc.onTheMap(tile_to_place) || rc.senseRobotAtLocation(tile_to_place) != null) continue;
                if (rc.canBuildRobot(toBuild, dir, influence_to_spend_on_unit)) {
                    rc.buildRobot(toBuild, dir, influence_to_spend_on_unit);
                } else {
                    break;
                }
            }
        }

        // Use the leftover influence to bid for a vote using getBidRatio as
        // the ratio to bid with.
        int current_influence = rc.getInfluence();
        double bid_ratio = getBidRatio(turnCount);
        int influence_to_spend_on_bid = (int) (current_influence * bid_ratio);
        // Only vote if the round number is after 300 or whatever
        if (turnCount > ROUNDS_BEFORE_START_VOTING)
            rc.bid(influence_to_spend_on_bid);
    }

    static void runPolitician() throws GameActionException {

        // Variables for the teams
        Team player_team = rc.getTeam();
        Team enemy = player_team.opponent();
        Team neutral = Team.NEUTRAL;

        // Variables for the unit's radiuses (radii..?)
        int sensor_radius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;

        // We want to empower nearby enemy robots
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }

        // We want to sense if there are nearby neutral ECs,
        // move towards them, and empower them.
        // TODO: This should include enemy ECs too, maybe fix the above function
        RobotInfo[] nearby_neutral_ecs = rc.senseNearbyRobots(sensor_radius, neutral);
        if (nearby_neutral_ecs.length > 0) {
            // Pick the first one that got scanned and move towards it.
            RobotInfo target_ec = nearby_neutral_ecs[0];
            Direction to_move = rc.getLocation().directionTo(target_ec.getLocation());
            //TODO: Do we even need to move towards it here..?
            if (tryMove(to_move)) {
                System.out.println("I moved");
            }

            // Empower the EC.
            int distance_to_target_ec = rc.getLocation().distanceSquaredTo(target_ec.getLocation());
            rc.empower(distance_to_target_ec + 1);
            return;
        }

        // Move in a random direction
        Direction to_move = randomDirection();

        // Check if there are Muckrakers nearby that should be followed
        // to a grey EC
        for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius, player_team)) {
            if (robot.getType() == RobotType.MUCKRAKER) {
                if (rc.getFlag(robot.getID()) == flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
                    // If stuck, try moving somewhere random with 20% probability
                    if (stuck()) {
                        if (Math.random() < 0.2) //TODO: change this to a variable
                            tryMove(nearbyEmptySpotDirection(rc.getLocation()));
                    }
                    // Move towards the robot
                    to_move = rc.getLocation().directionTo(robot.getLocation());
                    break;
                }
            }
        }
        // Actually move in the to_move direction
        if (tryMove(to_move))
            System.out.println("I moved!");

        // Now move away from other nearby units
        moveAwayFromOtherUnits();
    }

    static void runSlanderer() throws GameActionException {
        // get sensor radius for Slanderer
        int sensor_radius = rc.getType().sensorRadiusSquared;

        // The maximum distance before the slanderer should start
        // moving back towards the base
        // TODO: turn 1.5 into a variable
        int max_distance_from_home_base = (int) (sensor_radius / 1.5);

        // The minimum distance a slanderer should be to a base.
        // If it's closer than this, move away.
        int min_distance_from_home_base = 10;

        // Variables for teams
        Team player_team = rc.getTeam();
        Team enemy_team = rc.getTeam().opponent();

        // If we just spawned, set the nearest EC as our home base location.
        if (homeBaseLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius, player_team)) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    homeBaseLocation = robot.getLocation();
                    homeBaseId = robot.getID();
                }
            }
        }

        // Our main direction. We should move in this direction more than
        // all the others.
        // TODO: this is probably pointless, there's a better way to do this...
        if (mainDirection == null) {
            mainDirection = randomDirection();
        }

        // We want to see if there are any nearby enemy muckrakers,
        // and if so, we want to move in the opposite direction of them
        MapLocation current_location = rc.getLocation();
        for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius, enemy_team)) {
            if (robot.getType() == RobotType.MUCKRAKER) {
                // run away
                Direction to_move = current_location.directionTo(robot.getLocation()).opposite();
                if (tryMove(to_move)) {
                    System.out.println("I ran away from a muckraker");
                    return;
                }
            }
        }

        // How far away are we from the home base?
        int current_distance_to_home_base = current_location.distanceSquaredTo(homeBaseLocation);

        // What direction is the home base?
        Direction direction_to_home_base = current_location.directionTo(homeBaseLocation);

        // If we're too far away, move towards the base.
        if (current_distance_to_home_base > max_distance_from_home_base) {
            if (tryMove(direction_to_home_base)) {
                System.out.println("I moved towards the base!");
                return;
            }
        }

        // If we're too close to the base, move away.
        if (current_distance_to_home_base < min_distance_from_home_base) {
            // Pick a random direction to move in
            // TODO: This behavior seems really horrible, and should be changed
            Direction to_move_in;
            double randomVal = Math.random();
            // Move in the main direction with a 33% chance
            if (randomVal < 0.33) to_move_in = mainDirection;
                // Another 33% chance to move in a straight line away from the base
            else if (randomVal < 0.66) to_move_in = direction_to_home_base.opposite();
                // Otherwise, move in a random direction.
            else to_move_in = randomDirection();
            if (tryMove(to_move_in)) {
                System.out.println("I moved!");
                return;
            }
        }

        // Number of nearby slanderers should be around us before
        // we should move away
        // TODO: This should be global in its own class
        int max_acceptable_number_of_nearby_slanderers = 3;

        // We need to know how many slanderers are near us
        int number_of_nearby_slanderers = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius, player_team)) {
            if (robot.getType() == RobotType.SLANDERER)
                number_of_nearby_slanderers++;
        }

        // If there are too many slanderers near us, we should move away
        // TODO: I hate this logic. I did this before i made the moveAwayFromOtherUnits functionality
        if (number_of_nearby_slanderers > max_acceptable_number_of_nearby_slanderers) {
            System.out.println("Too many nearby slanderers");
            Direction to_move_in;
            if (Math.random() < moveInMainDirectionPercent) to_move_in = mainDirection;
            else to_move_in = randomDirection();
            if (tryMove(to_move_in))
                System.out.println("I moved!");
        }

        // TODO: Either do the above or do this, not both.
        moveAwayFromOtherUnits();
    }

    static void runMuckraker() throws GameActionException {
        // This is the number of nearby politicians a muckraker who
        // found a grey EC should have around them before they start
        // heading back to the location of the grey EC. Three politicians
        // nearby seems good.
        final int NUMBER_OF_POLITICIANS_BEFORE_GO_TO_GREY_EC = 3;

        // How close to the EC should we be before we stop and go
        // the other way?
        final int WITHIN_GREY_EC_RETURN_DISTANCE = 5;

        // The number of rounds a Muckraker should wait after it's
        // turned off the Grey EC color because there was another muckraker
        // with that color near it. 100 rounds seems long enough for the
        // other Muckraker to convert the EC.
        final int EC_COOLDOWN = 100;

        // Variables for teams
        Team player_team = rc.getTeam();
        Team enemy = player_team.opponent();

        // Variables for sensors
        int action_radius = rc.getType().actionRadiusSquared;
        int sensor_radius = rc.getType().sensorRadiusSquared;

        // If we just spawned, set our home base location to the nearest
        // enlightenment center's location
        if (homeBaseLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius, player_team)) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    homeBaseLocation = robot.getLocation();
                    homeBaseId = robot.getID();
                    break;
                }
            }
        }

        // Move in a random direction
        Direction to_move = randomDirection();

        // A 70% chance to move in the same direction we
        // moved in last turn
        // TODO: there's probably a better way to have muckrakers explore than this
        if (lastTurnsDirection != null)
            if (Math.random() < 0.7)
                to_move = lastTurnsDirection;

        // Update the lastTurnsDirection variable
        lastTurnsDirection = to_move;

        // If it's been 100 rounds since we got de-flagged, set our flag
        // back to NOTHING, the default.
        int flag = rc.getFlag(rc.getID());
        if (flag == flags.MUCKRAKER_EC_COOLDOWN.ordinal()) {
            if (turnCount > turnOfCooldown + EC_COOLDOWN) {
                rc.setFlag(flags.NOTHING.ordinal());
            }
        }

        // We found a grey EC, so we will act accordingly...
        if (flag == flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
            // If we're stuck, move to a random nearby location with 20% chance
            // TODO: for some reason this still doesnt work LOL
            if (stuck()) {
                if (Math.random() < 0.2)
                    tryMove(nearbyEmptySpotDirection(rc.getLocation()));
            }

            // We want to check how many nearby politicians are around.
            int number_of_politicians_around = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius)) {
                if (robot.team == player_team && robot.type == RobotType.POLITICIAN)
                    number_of_politicians_around++;
                // If there's an enemy muckraker around, we should de-flag ourselves
                // with 50% chance, and set our cooldown accordingly
                if (robot.getType() == RobotType.MUCKRAKER) {
                    if (rc.getFlag(robot.getID()) == flags.MUCKRAKER_FOUND_GREY_EC.ordinal()) {
                        if (Math.random() < 0.5) {
                            rc.setFlag(flags.MUCKRAKER_EC_COOLDOWN.ordinal());
                            turnOfCooldown = turnCount;
                        }
                    }
                }
            }

            // If we have enough politicians nearby, let's head to the grey EC location
            if (number_of_politicians_around >= NUMBER_OF_POLITICIANS_BEFORE_GO_TO_GREY_EC) {
                // If we're close enough, set our flag back to NOTHING, the default
                if (rc.getLocation().distanceSquaredTo(greyECLocation) < WITHIN_GREY_EC_RETURN_DISTANCE)
                    rc.setFlag(flags.NOTHING.ordinal());
                // Go to the grey EC
                to_move = rc.getLocation().directionTo(greyECLocation);
            } else {
                // Go to the home base
                to_move = rc.getLocation().directionTo(homeBaseLocation);
            }
            tryMove(to_move);
        } else {
            // If we killed a slanderer in the past, there's probably more there,
            // so let's head in that direction...
            if (lastExposedSlandererLocation != null) {
                tryMove(rc.getLocation().directionTo(lastExposedSlandererLocation));
            }
            // If there's an enemy nearby, KILL THEM....
            for (RobotInfo robot : rc.senseNearbyRobots(action_radius)) {
                if (robot.team == enemy && robot.type.canBeExposed()) {
                    if (rc.canExpose(robot.location)) {
                        rc.expose(robot.location);
                        // Remember the location of their death, so we can come
                        // back here later...
                        lastExposedSlandererLocation = robot.location;
                        return;
                    }
                }
            }

            // If we see a nearby grey EC, let's put our flag on signalling that
            // we found it
            if (rc.getFlag(rc.getID()) != flags.MUCKRAKER_EC_COOLDOWN.ordinal()) {
                for (RobotInfo robot : rc.senseNearbyRobots(sensor_radius)) {
                    if (robot.team != player_team && robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                        rc.setFlag(flags.MUCKRAKER_FOUND_GREY_EC.ordinal());
                        greyECLocation = robot.location;
                        return;
                    }
                }
            }

            // Actually move here
            // TODO: this should be more local to the actual move...
            if (tryMove(to_move))
                System.out.println("I moved!");

            // Move away from other units
            moveAwayFromOtherUnits();
        }

    }

    /**
     * Returns true if we are "stuck", or in the same
     * location we were last turn.
     *
     * @return true if stuck, false if not
     */
    static boolean stuck() {
        // TODO: should this be the same position over multiple turns..?
        return rc.getLocation() == lastTurnsLocation;
    }

    /**
     * Returns a random Direction.
     * This was modified from the original. Now, it gets all 8 nearby tiles,
     * puts them into a list, and shuffles them at random. Then, for each
     * tile, the passibility is used as a percent. If we roll higher than that
     * percent, we move to the next tile in the list.
     * <p>
     * For example, a tile with 1.0 passability will always be picked,
     * but a tile with 0.1 passability has a 10% chance to be skipped.
     *
     * @return a random Direction
     */
    static Direction randomDirection() throws GameActionException {
        MapLocation tile = rc.getLocation();

        // Pick our random number up here
        double random_number = Math.random();

        // Gather all 8 potential locations into an ArrayList
        ArrayList<MapLocation> potential_locations = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                MapLocation temp = new MapLocation(tile.x + i, tile.y + j);
                if (!rc.onTheMap(temp)) continue;
                if (rc.senseRobotAtLocation(temp) != null) continue;
                potential_locations.add(temp);
            }
        }

        // Shuffle the list.
        Collections.shuffle(potential_locations);

        // For each tile in our shuffled list, we have a x% chance
        // to pick it, and a 1 - x% chance to move to the next tile
        // in the list.
        for (MapLocation location : potential_locations) {
            double passability = rc.sensePassability(location);
            if (random_number < passability) {
                return rc.getLocation().directionTo(location);
            }
        }

        // This should actually never be called, lol...
        // TODO: throw an error here instead
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {

        // TODO: everything in here should be parameterized

        // Percent chance we should spawn a politician
        double politician_ratio = 0.65;

        // If we saw an enemy, higher percent chance to spawn politician
        // TODO: Have a cooldown timer on this, in case we havent been under attack for 100 say rounds
        if (sawAnEnemyNearBase) politician_ratio = 0.8;

        // Roll for either politician or muckraker
        if (Math.random() > politician_ratio) return RobotType.POLITICIAN;
        return RobotType.MUCKRAKER;
    }

    static void moveAwayFromOtherUnits() throws GameActionException {
        // The number of tiles we should move away from the wall if we
        // are adjacent to it. (in the muckflooder bot this is like 5)
        int tiles_to_move_away_from_wall = 2;

        // location, radius, team variables
        MapLocation tile = rc.getLocation();
        int sense_radius = rc.getType().sensorRadiusSquared;
        Team player_team = rc.getTeam();

        // A list of nearby friendly units.
        ArrayList<RobotInfo> nearby_units =
                new ArrayList<>(Arrays.asList(rc.senseNearbyRobots(sense_radius, player_team)));

        // We want to get the average location of all nearby friendly units.
        int avg_x = 0, avg_y = 0;
        if (nearby_units.size() > 0) {
            for (RobotInfo robot : nearby_units) {
                MapLocation position = robot.getLocation();
                avg_x += position.x;
                avg_y += position.y;
            }
            avg_x /= nearby_units.size();
            avg_y /= nearby_units.size();
            MapLocation average_position = new MapLocation(avg_x, avg_y);

            // Now that we have the average position of nearby friendly units,
            // we want to move in the opposite direction of that location.
            Direction to_move = tile.directionTo(average_position).opposite();

            // If we are touching a wall, move away from it.
            // Before I added this, the units were all sticking to the wall
            if (!rc.onTheMap(rc.adjacentLocation(to_move))) {
                to_move = to_move.opposite();
                for (int i = 0; i < tiles_to_move_away_from_wall; i++) {
                    if (tryMove(to_move))
                        System.out.println("I moved!");
                }
            } else {
                // Otherwise move in the opposite-of-average direction
                if (tryMove(to_move))
                    System.out.println("I moved!");
            }
        }
    }

    /**
     * Gets nearby empowerable robots.
     *
     * @return A list of nearby empowerable robots
     */
    static ArrayList<RobotInfo> getNearbyEmpowerables() {
        ArrayList<RobotInfo> nearby_empowerables = new ArrayList<>();
        int sense_radius = rc.getType().sensorRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(sense_radius)) {
            if (robot.team == rc.getTeam()) continue;
            nearby_empowerables.add(robot);
        }
        return nearby_empowerables;
    }

    /**
     * This function tells us how much we should bid on a vote.
     * I think a good bid function should not bid much early,
     * but bid a lot towards the end of the game.
     * This is a function that will need a <b>lot</b> of tweaking
     *
     * @param turn_number The current turn number
     * @return the ratio of current influence we should bid
     */
    static double getBidRatio(int turn_number) {
        // TODO: find an absolutely perfect bid function that makes mathematicians cry tears of joy.
        double x = turn_number / 1500.0;
        // function i came up with: (4x^(0.7e+1))/5, at 1500 it is around 80%
        return ((4.0 * Math.pow(x, 0.7 * Math.exp(1.0) + 1.0)) / 5.0);
        //return Math.pow(x-0.7,5) + Math.pow(x-0.2,3) + 0.2;
    }


    /**
     * Gets the direction of a nearby empty spot of a given tile.
     *
     * @param tile The tile to find a nearby spot of
     * @return The direction of a nearby spot
     * @throws GameActionException if we can't sense a nearby tile
     */
    static Direction nearbyEmptySpotDirection(MapLocation tile) throws GameActionException {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (j == 0 && i == 0) continue;
                MapLocation temp = new MapLocation(tile.x + i, tile.y + j);
                if (rc.senseRobotAtLocation(temp) == null)
                    return rc.getLocation().directionTo(temp);
            }
        }
        return null;
    }


    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException if its impossible to move (i think)
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            lastTurnsLocation = rc.getLocation();
            rc.move(dir);
            return true;
        } else return false;
    }
}