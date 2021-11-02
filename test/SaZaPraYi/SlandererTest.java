package SaZaPraYi;
import static org.junit.Assert.*;

import battlecode.common.GameActionException;
import org.junit.Test;
public class SlandererTest {
    Slanderer S;

    @Test
    public void testAvoid()throws GameActionException {
        assertEquals(S.avoid(), true);
    }
}

