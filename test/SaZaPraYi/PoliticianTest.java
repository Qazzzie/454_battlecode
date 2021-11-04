package SaZaPraYi;

import battlecode.common.RobotController;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class PoliticianTest {

    private RobotController rc;
    private RobotUtils utils;
    private Politician p;

    private void setupTests(){
        rc = Mockito.mock(RobotController.class);
        utils = new RobotUtils(rc);
        p = new Politician(rc,utils);
    }

    @Test
    public void testUsableConviction(){
        setupTests();
        int initialConviction = 100;
        //int convictionPenalty = 10;
        int noofNearByRobots = 5;
        int expectedResult = 18;
        double empowerFactor = 1;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testUsableConvictionWhenNearByRobotIsZero(){
        setupTests();
        int initialConviction = 100;
        int convictionPenalty = 10;
        int noofNearByRobots = 0;
        int expectedResult = initialConviction - convictionPenalty;
        double empowerFactor = 1;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testUsableConvictionWhenEmpowerFactorisLessThan1(){
        setupTests();
        int initialConviction = 100;
        //int convictionPenalty = 10;
        int noofNearByRobots = 5;
        int expectedResult = 18;
        double empowerFactor = 0.9;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testUsableConvictionWhenEmpowerFactorisGreaterThan1(){
        setupTests();
        int initialConviction = 100;
        //int convictionPenalty = 10;
        int noofNearByRobots = 5;
        int expectedResult = 21;
        double empowerFactor = 1.2;
        int result = p.getUsableConviction(initialConviction, noofNearByRobots, empowerFactor);
        assertEquals(expectedResult, result);
    }


}