package com.martinstephencox.photodetails;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    int RESULT_PHOTO_OK = 1;
    boolean addedMarker = false;
    ExifInterface exif;
    MarkerOptions posMarkerOptions = new MarkerOptions().position(new LatLng(0, 0)).visible(false).draggable(false);
    Marker posMarker;

    /*Image details for restoring state e.g. on rotation*/
    Uri iURI = Uri.EMPTY;
    String iEXIFPath = "";
    String iFilename = "";
    String iWidth = "";
    String iHeight = "";
    Long iSize = 0l;
    String iLat = "";
    String iLon = "";
    String iLatRef = "";
    String iLonRef = "";
    Float iLatFloat = 0.0f;
    Float iLonFloat = 0.0f;
    ExifInterface iEXIF = null;

    public enum populateMode {
        LOAD_IMAGE, REPOPULATE
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.select_photo_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Uses intent to launch a photo selector
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.photo_selector)), RESULT_PHOTO_OK);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //Successfully selected an image, load it into ImageView using Glide

            TableRow imageRow = (TableRow) findViewById(R.id.image_row);
            imageRow.setBackgroundColor(getResources().getColor(R.color.colorBlack));
            imageRow.setVisibility(View.VISIBLE);
            TableRow messageRow = (TableRow) findViewById(R.id.message_row);
            TableLayout table = (TableLayout) findViewById(R.id.layout_table);
            table.removeView(messageRow);
            try {

                iURI = data.getData();

                drawImage();

                iEXIFPath = getRealPathFromURI(this.getApplicationContext(), iURI);
                iEXIF = new ExifInterface(iEXIFPath);

                //Getting all the Exif attributes
                String[] filepathComponents = iEXIFPath.split("/");
                iFilename = filepathComponents[filepathComponents.length-1];

                File image = new File(iEXIFPath);
                iSize = image.length();
                iSize = iSize/1024;

                iWidth = iEXIF.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                iHeight = iEXIF.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                iLat = iEXIF.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                iLon = iEXIF.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                iLatRef = iEXIF.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                iLonRef = iEXIF.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                populateFields(iEXIF, populateMode.LOAD_IMAGE);

                iLatFloat = 0f;
                iLonFloat = 0f;

                addMapMarker();
                displayCoordsInDegrees();
            } catch (Exception e) {
                createErrorDialog(android.R.string.dialog_alert_title, R.string.photo_selector_error_text);
            }
        } else if (resultCode == RESULT_CANCELED){
            //User canceled operation
        } else {
            //Couldn't load the image, display an error message
            createErrorDialog(android.R.string.dialog_alert_title, R.string.photo_selector_error_text);
        }
    }

    public void drawImage() {
        ImageView imageView = (ImageView) findViewById(R.id.photo);
        imageView.setVisibility(View.VISIBLE);
        Glide.with(MainActivity.this).load(iURI).fitCenter().into(imageView);
    }

    public void populateFields(ExifInterface exif, populateMode mode) {
        TextView filename = (TextView) findViewById(R.id.image_filename);
        filename.setText(iFilename);
        TextView width = (TextView) findViewById(R.id.image_width);
        width.setText(iWidth);
        TextView height = (TextView) findViewById(R.id.image_height);
        height.setText(iHeight);
        TextView size = (TextView) findViewById(R.id.image_size_bytes);
        size.setText(iSize.toString() + getString(R.string.EXIF_size));

        if (mode.equals(populateMode.LOAD_IMAGE)) {
            TextView datetime = (TextView) findViewById(R.id.image_date_taken);
            datetime.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
            TextView camera = (TextView) findViewById(R.id.image_camera);
            camera.setText(exif.getAttribute(ExifInterface.TAG_MAKE));
            TextView lens = (TextView) findViewById(R.id.image_lens);
            lens.setText(exif.getAttribute(ExifInterface.TAG_APERTURE));
            TextView exposure = (TextView) findViewById(R.id.image_exposure);
            exposure.setText(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
            TextView flash = (TextView) findViewById(R.id.image_flash);
            flash.setText(exif.getAttribute(ExifInterface.TAG_FLASH));
            TextView focalLength = (TextView) findViewById(R.id.image_focal_length);
            focalLength.setText(exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
        }
    }

    public void addMapMarker() {
        if (iLat != null && iLon != null && iLatRef != null && iLonRef != null ) {

            if (iLatRef.equals("N")) {
                //North of equator, positive value
                iLatFloat = toDegrees(iLat);
            } else {
                //South of equator, negative value
                iLatFloat = 0 - toDegrees(iLat);
            }

            if (iLonRef.equals("E")) {
                //East of prime meridian, positive value
                iLonFloat = toDegrees(iLon);
            } else {
                //West of prime meridian, negative value
                iLonFloat = 0 - toDegrees(iLon);
            }
        }

        final MapView gMap = (MapView) findViewById(R.id.map);

        if (addedMarker == false) {
            posMarker = gMap.getMap().addMarker(posMarkerOptions);
            posMarker.setTitle(getString(R.string.map_position));
            addedMarker = true;
        }

        posMarker.setVisible(true);
        posMarker.setPosition(new LatLng(iLatFloat, iLonFloat));

        GoogleMap gMapObj = gMap.getMap();

        gMapObj.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                posMarker.setPosition(latLng);
                displayCoordsInDegrees();

                //Use text view values instead of posMarker values
                iLat = toDMS(posMarker.getPosition().latitude);
                iLon = toDMS(posMarker.getPosition().longitude);

                if (posMarker.getPosition().latitude > 0) {
                    //North of equator, positive value
                    iLatFloat = toDegrees(iLat);
                    iLatRef = "N";
                } else {
                    //South of equator, negative value
                    iLatFloat = 0 - toDegrees(iLat);
                    iLatRef = "S";
                }

                if (posMarker.getPosition().longitude > 0) {
                    //East of prime meridian, positive value
                    iLonFloat = toDegrees(iLon);
                    iLonRef = "E";
                } else {
                    //West of prime meridian, negative value
                    iLonFloat = 0 - toDegrees(iLon);
                    iLonRef = "W";
                }
            }
        });
    }

    public void createErrorDialog(int title, int message) {
        //Couldn't load the image, display an error message
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void displayCoordsInDegrees() {
        DecimalFormat formatter = new DecimalFormat("0.000");

        String latFormat = formatter.format(posMarker.getPosition().latitude);
        String lonFormat = formatter.format(posMarker.getPosition().longitude);

        TextView latitude = (TextView) findViewById(R.id.image_latitude);
        TextView longitude = (TextView) findViewById(R.id.image_longitude);

        latitude.setText(latFormat);  //Use posMarker location as field actually reflects marker position
        longitude.setText(lonFormat);  //Use posMarker location as field actually reflects marker position
    }

    public Float toDegrees(String ref) {
        //EXIF data should is in DMS format, need to convert from DMS to degrees for Google Maps
        //Credit to http://android-er.blogspot.co.uk/2010/01/convert-exif-gps-info-to-degree-format.html

        Float result = null;
        String[] DMS = ref.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    public static String getRealPathFromURI(Context context, Uri uri){
        Cursor cursor = null;
        String filePath = "";

        if (Build.VERSION.SDK_INT < 19) {
            // On Android Jelly Bean

            // Taken from https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
            try {
                String[] proj = { MediaStore.Images.Media.DATA };
                cursor = context.getContentResolver().query(uri,  proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                filePath = cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            // On Android KitKat+

            // Taken from http://hmkcode.com/android-display-selected-image-and-its-real-path/
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // Where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath;
    }

    public String toDMS(double coords) {
        //Need to convert from DD to DMS format for EXIF

        int iDegrees, iMinutes, iSeconds;
        String result, sDegrees, sMinutes, sSeconds;

        if (coords < 0) {
            coords = -coords;   //Make negative values into positive
        }

        iDegrees = ((int) coords);
        sDegrees = String.valueOf(iDegrees) + "/1,";

        iMinutes = (int) (60 * (coords % 1));
        sMinutes = String.valueOf(iMinutes) + "/1,";

        iSeconds = (int) (60000 * (iMinutes % 1));
        sSeconds = String.valueOf(iSeconds) + "/1000";

        return sDegrees + sMinutes + sSeconds;
    }

    public boolean saveImage() {
        try {
            //Save the modified image
            iEXIF.setAttribute(ExifInterface.TAG_DATETIME, ((TextView) findViewById(R.id.image_date_taken)).getText().toString());
            iEXIF.setAttribute(ExifInterface.TAG_MAKE, ((TextView) findViewById(R.id.image_camera)).getText().toString());
            iEXIF.setAttribute(ExifInterface.TAG_APERTURE, ((TextView) findViewById(R.id.image_lens)).getText().toString());
            iEXIF.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, ((TextView) findViewById(R.id.image_exposure)).getText().toString());
            iEXIF.setAttribute(ExifInterface.TAG_FLASH, ((TextView) findViewById(R.id.image_flash)).getText().toString());
            iEXIF.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, ((TextView) findViewById(R.id.image_focal_length)).getText().toString());

            if (posMarker != null) {

                TextView latitude = (TextView) findViewById(R.id.image_latitude);
                TextView longitude = (TextView) findViewById(R.id.image_longitude);

                //Use text view values instead of posMarker values
                String lat = toDMS(posMarker.getPosition().latitude);
                String lon = toDMS(posMarker.getPosition().longitude);

                iEXIF.setAttribute(ExifInterface.TAG_GPS_LATITUDE, lat);
                iEXIF.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lon);

                if (posMarker.getPosition().latitude > 0) {
                    iEXIF.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                } else {
                    iEXIF.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                }

                if (posMarker.getPosition().longitude > 0) {
                    iEXIF.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                } else {
                    iEXIF.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                }
            }
            iEXIF.saveAttributes();
            View coordLayout = findViewById(R.id.main_content);
            Snackbar successSnackbar = Snackbar.make(coordLayout, R.string.photo_save_success_text, Snackbar.LENGTH_SHORT);
            successSnackbar.show();

            ViewGroup mapGroup = (ViewGroup) findViewById(R.id.map);
            int count = mapGroup.getChildCount();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //HERE FOR ACTION BAR OPTIONS

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_save) {

            if (iEXIF == null) {
                createErrorDialog(R.string.photo_save, R.string.photo_missing_image_error_text);
            } else {
                boolean result = saveImage();
                if (result == false) {
                    createErrorDialog(R.string.photo_save, R.string.photo_save_error_text);
                }
            }

        }

        if (id == R.id.action_clear_EXIF) {
            try {

                ViewGroup detailsScroll = (ViewGroup) findViewById(R.id.layout_table);

                for (int i = 0; i < detailsScroll.getChildCount(); i++) {
                    ViewGroup tableRow = (ViewGroup) detailsScroll.getChildAt(i);
                    for (int j = 0; j < tableRow.getChildCount(); j++) {
                        View view = tableRow.getChildAt(j);
                        if (view instanceof EditText) {
                            ((EditText) view).setText(" ");
                        }
                    }
                }

                TextView latitude = (TextView) findViewById(R.id.image_latitude);
                TextView longitude = (TextView) findViewById(R.id.image_longitude);

                posMarker.setPosition(new LatLng(0,0));
                latitude.setText("0.000");
                longitude.setText("0.000");

                return true;
            } catch (Exception e) {
                createErrorDialog(R.string.action_clear_EXIF, R.string.clear_EXIF_error_text);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        String stringURI = iURI.toString();
        savedInstanceState.putString("URI", stringURI);
        savedInstanceState.putString("EXIF_PATH", iEXIFPath);
        savedInstanceState.putString("FILENAME", iFilename);
        savedInstanceState.putString("WIDTH", iWidth);
        savedInstanceState.putString("HEIGHT", iHeight);
        savedInstanceState.putLong("SIZE", iSize);
        savedInstanceState.putString("LAT", iLat);
        savedInstanceState.putString("LON", iLon);
        savedInstanceState.putString("LAT_REF", iLatRef);
        savedInstanceState.putString("LON_REF", iLonRef);
        savedInstanceState.putFloat("LAT_FLOAT", iLatFloat);
        savedInstanceState.putFloat("LON_FLOAT", iLonFloat);

        //Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        String stringURI = savedInstanceState.getString("URI");
        iURI = Uri.parse(stringURI);
        iEXIFPath = savedInstanceState.getString("EXIF_PATH");
        iFilename = savedInstanceState.getString("FILENAME");
        iWidth = savedInstanceState.getString("WIDTH");
        iHeight = savedInstanceState.getString("HEIGHT");
        iSize = savedInstanceState.getLong("SIZE");
        iLat = savedInstanceState.getString("LAT");
        iLon = savedInstanceState.getString("LON");
        iLatRef = savedInstanceState.getString("LAT_REF");
        iLonRef = savedInstanceState.getString("LON_REF");
        iLatFloat = savedInstanceState.getFloat("LAT_FLOAT");
        iLonFloat = savedInstanceState.getFloat("LON_FLOAT");

        try {
            iEXIF = new ExifInterface(iEXIFPath);
        } catch (Exception e) {

        }

        drawImage();
        populateFields(null, populateMode.REPOPULATE);
        addMapMarker();
        displayCoordsInDegrees();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return DetailsFragment.newInstance(position);
                case 1:
                    return MapsFragment.newInstance(position);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.menu_header_details);
                case 1:
                    return getString(R.string.menu_header_map);
            }
            return null;
        }
    }
}
