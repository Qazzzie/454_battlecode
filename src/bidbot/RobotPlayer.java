package bidbot;

import battlecode.common.*;

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

        // Bid 50% of our current influence at the start of the round.
        int bid_amount = (int) (rc.getInfluence() * 0.50);
        rc.bid(bid_amount);

        // variables for buliding our robot
        RobotType to_build;
        int amount_to_spend;

        // Roll a random value to pick the kind of robot to spawn:w
        double random_value = Math.random();
        if (random_value < 0.70) {
            // Spawn a slanderer with a 70% chance
            to_build = RobotType.SLANDERER;
            // Spend 20% of our current influence on a slanderer
            amount_to_spend = (int) (rc.getInfluence() * 0.2);
        } else if (random_value < 0.87) {
            // Spawn a muckraker with a 17% chance
            to_build = RobotType.MUCKRAKER;
            // Spend 10% of our current influence on a muckraker
            amount_to_spend = (int) (rc.getInfluence() * 0.1);
        } else {
            // Spawn a muckraker with a 13% chance
            to_build = RobotType.POLITICIAN;
            // Spend 10% of our current influence on a politician
            amount_to_spend = (int) (rc.getInfluence() * 0.1);
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
            // If we are next to a non-friendly robot, empower.
            // This includes grey ECs
            if (robot.getTeam() != rc.getTeam()) {
                rc.empower(actionRadius);
            }
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    /*
    ============================================================
    Everything below is the same as examplefuncsplayer (i think)
    ============================================================
     */

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (tryMove(randomDirection()))
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