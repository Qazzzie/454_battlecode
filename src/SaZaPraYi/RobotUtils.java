package SaZaPraYi;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

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
}
