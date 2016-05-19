package com.martinstephencox.photodetails;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Created by Martin on 19/03/2016.
 */
public class MainActivityTest {

    //public final ExpectedException exception = ExpectedException.none();
    //exception.expect(NullPointerException);

    MainActivity ma = new MainActivity();

    @Test
    public void MainActivity_Default_Values() throws Exception {
        assertEquals(1, ma.RESULT_PHOTO_OK);
        assertEquals(0l, ma.iSize, 0l);
        assertEquals(0.0f, ma.iLatFloat, 0.0f);
        assertEquals(0.0f, ma.iLonFloat, 0.0f);
        assertEquals("", ma.iEXIFPath);
        assertEquals("", ma.iFilename);
        assertEquals("", ma.iWidth);
        assertEquals("", ma.iHeight);
        assertEquals("", ma.iLat);
        assertEquals("", ma.iLon);
        assertEquals("", ma.iLatRef);
        assertEquals("", ma.iLonRef);
        assertEquals(null, ma.iEXIF);

        //Marker Options
        assertEquals(0.0, ma.posMarkerOptions.getPosition().latitude, 0.0);
        assertEquals(0.0, ma.posMarkerOptions.getPosition().longitude, 0.0);
        assertFalse(ma.posMarkerOptions.isVisible());
        assertFalse(ma.posMarkerOptions.isDraggable());
        assertFalse(ma.selectedAnImage);
    }

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

    @Test
    public void getImageSizeInKb() throws Exception {
        //Using boundary values
        assertEquals(0, ma.getImageSizeInKb(-1l), 0.0);
        assertEquals(0, ma.getImageSizeInKb(0l), 0.0);
        assertEquals(1, ma.getImageSizeInKb(1024l), 0.0);
        assertEquals(64, ma.getImageSizeInKb(65536l), 0.0);
        assertEquals(2048, ma.getImageSizeInKb(2097152l), 0.0);
        assertEquals(16384, ma.getImageSizeInKb(16777216l), 0.0);
    }

    // getRealPathFromUri
}