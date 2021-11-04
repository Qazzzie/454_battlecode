package SaZaPraYi;

import battlecode.common.*;

//import javax.lang.model.type.NullType;
//import java.util.ArrayList;


public class Slanderer {
    private static RobotController rc;
    private static RobotUtils utils;
    boolean enemy_spotted = false;

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
//        Direction to_move = utils.randomDirection();
        // Move away from sensed enemies.
        if (rc.senseNearbyRobots(sensorRadiusSquared, rc.getTeam().opponent()).length != 0) {
            //System.out.println("Enemy sensed");
            rc.setFlag(SaZaPraYiZDev.RobotUtils.flags.NOTHING.ordinal());
            avoidEnemy();
        } else {
            rc.setFlag(SaZaPraYiZDev.RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal());
            avoidSlandererFlagging(); //second priority, to be demoted by moving away from slanderers with their enemy_spotted flag up
        }
    }

    public boolean avoidEnemy() throws GameActionException {
//        int currentFlag = 0;
//        rc.setFlag(RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal());
//        int currentFlag = rc.getFlag(rc.getID());

        for (RobotInfo enemy_i : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent())) {
            Direction away_from_enemy_i = rc.getLocation().directionTo(enemy_i.location).opposite();
            if (utils.tryMove(away_from_enemy_i)) {
//                System.out.println("Moving away from enemy");
                return true;
            } else {
                utils.moveAwayFromOtherUnits();
                return true;
            }
        }
        return false;
    }

    public boolean avoidSlandererFlagging() throws GameActionException {
        //Move away from flagging slanderer FS.
        for (RobotInfo FS : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam())) {
            if(rc.getFlag(FS.getID()) == RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal()){
                Direction away_from_FS = rc.getLocation().directionTo(FS.location).opposite();
                if (utils.tryMove(away_from_FS)) {
                    System.out.println("Moving away from Flagging slanderer");
                    return true;
                }
            }
        }
        utils.moveAwayFromOtherUnits();
        return false;
    }
//    //avoid walls and otherwise move randomly.
//    public void normalMove()throws GameActionException{
//        Direction to_move = utils.randomDirection();
//        // if its not on the map move opposite
//        if (!rc.onTheMap(rc.adjacentLocation(to_move))) {
//            to_move = to_move.opposite();
//            utils.tryMove(to_move);
//        } else {utils.tryMove(to_move);}
//    }
}



