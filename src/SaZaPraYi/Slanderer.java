package SaZaPraYi;

import battlecode.common.*;

//import javax.lang.model.type.NullType;
//import java.util.ArrayList;


public class Slanderer {
    private static RobotController rc;
    private static RobotUtils utils;
    boolean enemy_spotted =  false;

    /**
     * The constructor for the Slanderer controller object.
     *
     * @param _rc    The RobotController to use.
     * @param _utils The RobotUtils object to use.
     */
    public Slanderer(RobotController _rc, RobotUtils _utils) {
        rc = _rc;
        utils = _utils;
    }

    /**
     * The run function for the Slanderer controller object.
     *
     * @throws GameActionException if anything would cause one.
     */
    public void run() throws GameActionException {
        int sensorRadiusSquared = rc.getType().sensorRadiusSquared;
        Direction to_move = utils.randomDirection();

        // Move away from sensed enemies.
        if(rc.senseNearbyRobots(sensorRadiusSquared, rc.getTeam().opponent()).length!=0) {
            //System.out.println("Enemy sensed");
            enemy_spotted = true;
            for (RobotInfo enemy_i : rc.senseNearbyRobots(sensorRadiusSquared, rc.getTeam().opponent())) {
                Direction away_from_enemy_i = rc.getLocation().directionTo(enemy_i.location).opposite();
                if (utils.tryMove(away_from_enemy_i)) {
                    //System.out.println("Moving away from enemy");
                }
                else normalMove();
            }
        }
        else normalMove(); //second priority, to be demoted by moving away from slanderers with their enemy_spotted flag up

    }

    //avoid walls and otherwise move randomly.
    public void normalMove()throws GameActionException{
        Direction to_move = utils.randomDirection();
        if (!rc.onTheMap(rc.adjacentLocation(to_move))) {
            to_move = to_move.opposite();
            utils.tryMove(to_move);
            //if (utils.tryMove(to_move))
                //System.out.println("I moved away from the edge!");
        } else {utils.tryMove(to_move);}
        //} else if (utils.tryMove(to_move))
        //    System.out.println("I moved randomly!");
    }
}



