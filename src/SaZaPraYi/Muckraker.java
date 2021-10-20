package SaZaPraYi;

import battlecode.common.*;

public class Muckraker {
    private static RobotController rc;
    private static RobotUtils utils;

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
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {

                // It's a slanderer... go get them
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
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

                    return;
                }
            }
        }

        //Avoid politician
        for (RobotInfo robot: rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemy)){
            if(robot.getType()== RobotType.POLITICIAN) {
                Direction enemy_loc = rc.getLocation().directionTo(robot.location);
                if (utils.tryMove(enemy_loc.opposite())) {
                    System.out.println("Moving away from ");
                }
            }
        }

        if (utils.tryMove(utils.randomDirection()))
            System.out.println("I moved!");
    }

}
