package com.martinstephencox.photodetails;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Martin on 19/03/2016.
 */
public class MainActivityTest {

    MainActivity ma = new MainActivity();

    @Test
    public void DMS_to_DD() throws Exception {
        String lat = "46/1,59/1,189183/9983";
        String lon = "3/1,29/1,13504/18473";
        assertEquals(46.989, ma.toDegrees(lat), 0.01);
        assertEquals(3.483, ma.toDegrees(lon), 0.01);
    }

    @Test
    public void DD_to_DMS() throws Exception {
        Double ref = 46.989;
        String DMS = ma.toDMS(ref);
        assertEquals("46/1,59/1,0/1000", ma.toDMS(ref));
    }
}
