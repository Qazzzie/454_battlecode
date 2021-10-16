package SaZaPraYi;

import battlecode.common.*;

import java.util.ArrayList;
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
     * Gets an array of eight possible directions.
     *
     * @return an array of eight possible directions.
     */
    public Direction[] getDirections() {
        return directions;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public Direction randomDirection() {
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
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
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
}
