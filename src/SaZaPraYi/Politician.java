package SaZaPraYi;

import battlecode.common.*;

public class Politician {
    private static RobotController rc;
    private static RobotUtils utils;

    /**
     * The constructor for the Politician controller object.
     *
     * @param _rc    The RobotController to use.
     * @param _utils The RobotUtils object to use.
     */
    public Politician(RobotController _rc, RobotUtils _utils) {
        rc = _rc;
        utils = _utils;
    }

    /**
     * The run function for the Politician controller object.
     *
     * @throws GameActionException if anything would cause one.
     */
    public void run() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        Team friend = rc.getTeam();
        Team neutralEC = Team.NEUTRAL;

        int actionRadius = rc.getType().actionRadiusSquared;
        int senseRadius = rc.getType().sensorRadiusSquared;

        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] friendly = rc.senseNearbyRobots(actionRadius,friend);
        RobotInfo[] allrobots = rc.senseNearbyRobots(actionRadius);

        int noofnearbyrobots = attackable.length + friendly.length;

        int penaltyconviction = 10;
        int initialconviction = rc.getConviction();
        int useableconviction = 0;
        if (noofnearbyrobots >0) {
            useableconviction = (initialconviction - penaltyconviction) / noofnearbyrobots; //this usable conviction must be >0 to give speech
        }

        //empower when Neutral EC is nearby
        for (RobotInfo neutral: rc.senseNearbyRobots(actionRadius, neutralEC)){
            if (useableconviction > 0 && rc.canEmpower(actionRadius)){
                System.out.println("empowering....");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
        }


        //move towards enemy area to reduce their power when own power is less
        if (initialconviction <= penaltyconviction){
            // check/sense for enemy robots within radius and move towards their direction
            for (RobotInfo robot: rc.senseNearbyRobots(senseRadius, enemy)){
                Direction enemy_location = rc.getLocation().directionTo(robot.location);
                if (utils.tryMove(enemy_location)){
                    System.out.println("Moving towards enemy");
                }
            }
        }

        // start giving speech once round reaches to 150+ even enemy is not found after every 10 round
        // if enemy is detected try to convict them as well
        // This makes politicians able to give speech only from round 150
        if(attackable.length != 0 && rc.canEmpower(actionRadius) && useableconviction>0){
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        else if (rc.getRoundNum() >=150){
            if(rc.getRoundNum()%10 == 0 && useableconviction>0 && rc.canEmpower(actionRadius)){
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }

        }


        /*
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
        */
        if (utils.tryMove(utils.randomDirection()))
            System.out.println("I moved!");
    }

}
