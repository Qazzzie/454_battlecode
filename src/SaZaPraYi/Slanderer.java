package SaZaPraYi;

import battlecode.common.*;



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
        Team enemy = rc.getTeam().opponent();
        Team friend = rc.getTeam();


        int sensorRadiusSquared = rc.getType().sensorRadiusSquared;
//        Direction to_move = utils.randomDirection();


        // If there are enemies near the sensor radius set flag and move away from them.
        // Otherwise: avoid Flagging? look for flag signal and don't go to the direction
        // of the flagged slanderer since they are sending signal of presence of an enemy

        if (rc.senseNearbyRobots(sensorRadiusSquared, enemy).length >0) {
            //System.out.println("Enemy sensed");
            rc.setFlag(RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal());
            avoidEnemy(enemy);
        } else if (rc.senseNearbyRobots(sensorRadiusSquared, friend).length >0){
            //rc.setFlag(RobotUtils.flags.NOTHING.ordinal());   //we don't want to set flag to all bots as 1 flag set takes 100bytes
            avoidFlaggedSlanderer(friend); //second priority, to be demoted by moving away from slanderers with their enemy_spotted flag up
        } else{
            utils.moveAwayFromOtherUnits();
        }
    }


    /**
     * If enemy detected, move to opposite direction of enemy; otherwise avoid bots of same team as well
     * to prevent blocking
     **/

    public boolean avoidEnemy(Team enemy) throws GameActionException {

        for (RobotInfo enemy_i : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemy)) {
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


    /**
     * Slanderer sense the own team flagged slanderer and moves away from them since it's a
     * signal of presence of enemy near by.
     * Also, moves away from each other in general not to block paths.
     **/

    public boolean avoidFlaggedSlanderer(Team friend) throws GameActionException {
        //Move away from flagged slanderer FS.
        for (RobotInfo FS : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, friend)) {
            if (rc.getFlag(FS.getID()) == RobotUtils.flags.SLANDERER_SPOTTED_ENEMY.ordinal()) {
                Direction away_from_FS = rc.getLocation().directionTo(FS.location).opposite();
                if (utils.tryMove(away_from_FS)) {
                    //System.out.println("Moving away from Flagging slanderer");
                    return true;
                } else {
                    utils.moveAwayFromOtherUnits();
                    return true;
                }
            }
            // need this else here to avoid slanderer not moving away from EC after production
            else {
                utils.moveAwayFromOtherUnits();
                return true;
            }

        }
        return false;
    }
}



