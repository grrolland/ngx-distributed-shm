package io.github.grrolland.hcshm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * ShmValue test case
 */
public class ShmValueTestCase {

    @Test
    public void getValue() {
        ShmValue shmValue = new ShmValue("X", 0);
        assertEquals("X", shmValue.getValue());
    }

    @Test
    public void getLastingTime() {
        ShmValue shmValue = new ShmValue("X", 0);
        assertEquals(0, shmValue.getLastingTime());
    }

    @Test
    public void getLastingTimeExpired() {
        ShmValue shmValue = new ShmValue("X", 99);
        assertEquals(-1, shmValue.getLastingTime());
    }

    @Test
    public void getLastingTimeNotExpired() {
        ShmValue shmValue = new ShmValue("X", 150);
        assertTrue(shmValue.getLastingTime() > 100);
    }

    @Test
    public void expire() {
        ShmValue shmValue = new ShmValue("X", 0);
        assertEquals(0, shmValue.getLastingTime());
        shmValue.expire(150);
        assertTrue(shmValue.getLastingTime() > 100);
        shmValue.expire(99);
        assertEquals(-1, shmValue.getLastingTime());
    }
}
