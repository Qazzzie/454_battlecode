package SaZaPraYi;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RobotUtils {

    private static RobotController rc;

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

    public enum flags {
        NOTHING,
        MUCKRAKER_FOUND_GREY_EC,
        MUCKRAKER_EC_COOLDOWN
    }


    /**
     * The constructor for the RobotUtils object.
     * It takes a RobotController as a parameter.
     *
     * @param _rc The RobotController to use
     */
    public RobotUtils(RobotController _rc) {
        rc = _rc;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public Direction randomDirection() throws GameActionException {
        MapLocation tile = rc.getLocation();

        // Pick our random number up here
        double randomNumber = Math.random();

        // Gather all 8 potential locations into an ArrayList
        ArrayList<MapLocation> potentialLocations = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                MapLocation temp = new MapLocation(tile.x + i, tile.y + j);
                if (!rc.onTheMap(temp)) continue;
                if (rc.senseRobotAtLocation(temp) != null) continue;
                potentialLocations.add(temp);
            }
        }

        // Shuffle the list.
        Collections.shuffle(potentialLocations);

        // For each tile in our shuffled list, we have a x% chance
        // to pick it, and a 1 - x% chance to move to the next tile
        // in the list.
        for (MapLocation location : potentialLocations) {
            double passability = rc.sensePassability(location);
            if (randomNumber < passability) {
                return rc.getLocation().directionTo(location);
            }
        }

        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        //System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Returns the direction of a random adjacent empty tile if there is one,
     * otherwise returns null.
     *
     * @param tile The MapLocation to check the adjacent tiles of
     * @return Either a direction towards a nearby empty tile, or null
     * @throws GameActionException if anything in here would cause one
     */
    public Direction getDirectionOfRandomAdjacentEmptyTile(MapLocation tile) throws GameActionException {
        ArrayList<MapLocation> nearbyAdjacentTiles = new ArrayList<>();
        // First, put all the adjacent nearby tiles into a list...
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                if(j == 0 && i == 0) continue; // This would be the tile itself...
                MapLocation temp = new MapLocation(tile.x + i, tile.y + j);
                if (!rc.onTheMap(temp)) continue;
                if (rc.senseRobotAtLocation(temp) == null)
                    nearbyAdjacentTiles.add(temp);
            }
        }
        if(nearbyAdjacentTiles.size() > 0) {
            // Shuffle them...
            Collections.shuffle(nearbyAdjacentTiles);
            // And return a random one
            return rc.getLocation().directionTo(nearbyAdjacentTiles.get(0));
        }
        // If there isn't any nearby tiles, return null
        return null;
    }

    /**
     * Returns true or false whether or not the robot is touching the wall
     *
     * @return true if touching the wall, false if not.
     * @throws GameActionException if anything would cause one
     */
    public boolean isTouchingTheWall() throws GameActionException {
        MapLocation tile = rc.getLocation();
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                if(j == 0 && i == 0) continue; // This would be the tile itself...
                MapLocation temp = new MapLocation(tile.x + i, tile.y + j);
                if (!rc.onTheMap(temp)) return true;
            }
        }
        return false;
    }

    /**
     * Moves away from nearby friendly units.
     *
     * @throws GameActionException if anything here should cause one
     */
    public void moveAwayFromOtherUnits() throws GameActionException{
        // The number of tiles we should move away from the wall if we
        // are adjacent to it. (in the muckflooder bot this is like 5)
        int tilesToMoveAwayFromWall = 2;

        // location, radius, team variables
        MapLocation tile = rc.getLocation();
        int senseRadius = rc.getType().sensorRadiusSquared;
        Team playerTeam = rc.getTeam();

        // A list of nearby friendly units.
        ArrayList<RobotInfo> nearbyFriendlyUnits =
                new ArrayList<>(Arrays.asList(rc.senseNearbyRobots(senseRadius, playerTeam)));

        // We want to get the average location of all nearby friendly units.
        int avgX = 0, avgY = 0;
        if (nearbyFriendlyUnits.size() > 0) {
            for (RobotInfo robot : nearbyFriendlyUnits) {
                MapLocation position = robot.getLocation();
                avgX += position.x;
                avgY += position.y;
            }
            avgX /= nearbyFriendlyUnits.size();
            avgY /= nearbyFriendlyUnits.size();
            MapLocation averagePosition = new MapLocation(avgX, avgY);

            // Now that we have the average position of nearby friendly units,
            // we want to move in the opposite direction of that location.
            Direction toMove = tile.directionTo(averagePosition).opposite();

            // If we are touching a wall, move away from it.
            // Before I added this, the units were all sticking to the wall
            if (isTouchingTheWall()) {
                toMove = toMove.opposite();
                for (int i = 0; i < tilesToMoveAwayFromWall; i++) {
                    if (tryMove(toMove))
                        System.out.println("I moved!");
                }
            } else {
                // Otherwise move in the opposite-of-average direction
                if (tryMove(toMove))
                    System.out.println("I moved!");
            }
        }
    }
}
