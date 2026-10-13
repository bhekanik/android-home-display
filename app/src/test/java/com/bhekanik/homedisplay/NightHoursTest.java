package com.bhekanik.homedisplay;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NightHoursTest {
    @Test public void dimsAcrossMidnightAtTheRequestedBoundaries() {
        assertFalse(NightHours.isNight(21));
        assertTrue(NightHours.isNight(22));
        assertTrue(NightHours.isNight(23));
        assertTrue(NightHours.isNight(0));
        assertTrue(NightHours.isNight(6));
        assertFalse(NightHours.isNight(7));
    }
}
