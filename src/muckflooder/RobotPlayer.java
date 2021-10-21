package muckflooder;

import battlecode.common.*;

import java.util.ArrayList;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

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

    static int turnCount;

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

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
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

        // Bid 10% of our current influence at the start of the round.
        int bid_amount = (int) (rc.getInfluence() * 0.1);
        // If it's round 500 or greater, bid 90% of our current influence.
        if (rc.getRoundNum() > 500) bid_amount = (int) (rc.getInfluence() * 0.9);
        rc.bid(bid_amount);

        // variables for buliding our robot
        RobotType to_build;
        int amount_to_spend;

        // Roll a random value to pick the kind of robot to spawn
        double random_value = Math.random();
        if (random_value < 0.70) {
            // Spawn a muckraker with a 70% chance
            to_build = RobotType.MUCKRAKER;
            // Muckrakers should cost 40% of our current influence
            amount_to_spend = (int) (rc.getInfluence() * 0.4);
        } else if (random_value < 0.80) {
            // Spawn a slanderer with a 10% chance
            to_build = RobotType.SLANDERER;
            // Slanderers should cost 20% of our current influence
            amount_to_spend = (int) (rc.getInfluence() * 0.2);
        } else {
            // Spawn a slanderer with a 20% chance
            to_build = RobotType.POLITICIAN;
            // Politicians should cost 20% of our current influence
            amount_to_spend = (int) (rc.getInfluence() * 0.2);
        }

        // Actually spawn our units
        for (Direction dir : directions) {
            MapLocation tile_to_place = rc.adjacentLocation(dir);
            if (!rc.onTheMap(tile_to_place) || rc.senseRobotAtLocation(tile_to_place) != null) continue;
            if (rc.canBuildRobot(to_build, dir, amount_to_spend)) {
                rc.buildRobot(to_build, dir, amount_to_spend);
            } else {
                break;
            }
        }
    }

    static void runPolitician() throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius)) {
            // Empower anything nearby that isn't on our team
            if (robot.getTeam() != rc.getTeam()) {
                rc.empower(actionRadius);
            }
        }
        // Move in a random direction
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        // Move in a random direction
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        ArrayList<RobotInfo> nearby_muckrakers = new ArrayList<>();
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius)) {
            // Expose any nearby enemies that can be exposed
            if (robot.getTeam() == enemy && robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
            // if it's a friendly muckraker, add it to our nearby muckrakers list
            if (robot.getTeam() != enemy && robot.type == RobotType.MUCKRAKER) {
                nearby_muckrakers.add(robot);
            }
        }
        Direction to_move = randomDirection();

        // Get the average location of nearby muckrakers, and move away from it
        if (nearby_muckrakers.size() > 0) {
            int avg_x = 0, avg_y = 0;
            for (RobotInfo muckraker : nearby_muckrakers) {
                MapLocation position = muckraker.getLocation();
                avg_x += position.x;
                avg_y += position.y;
            }
            avg_x /= nearby_muckrakers.size();
            avg_y /= nearby_muckrakers.size();
            MapLocation average_position_of_nearby_muckrakers = new MapLocation(avg_x, avg_y);
            // Move away from the average location
            to_move = rc.getLocation().directionTo(average_position_of_nearby_muckrakers).opposite();

            // If we are adjacent to a wall, move 5 tiles in the opposite direction
            if (!rc.onTheMap(rc.adjacentLocation(to_move))) {
                to_move = to_move.opposite();
                for (int i = 0; i < 5; i++) {
                    if (tryMove(to_move))
                        System.out.println("I moved!");
                }
            }
        }
        if (tryMove(to_move))
            System.out.println("I moved!");
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}