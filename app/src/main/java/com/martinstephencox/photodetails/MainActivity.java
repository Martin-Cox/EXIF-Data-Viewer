package com.martinstephencox.photodetails;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    int RESULT_PHOTO_OK = 1;
    ExifInterface exif;

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
            ImageView imageView = (ImageView) findViewById(R.id.photo);
            imageView.setVisibility(View.VISIBLE);
            TableRow imageRow = (TableRow) findViewById(R.id.image_row);
            imageRow.setBackgroundColor(getResources().getColor(R.color.colorBlack));
            imageRow.setVisibility(View.VISIBLE);
            TableRow messageRow = (TableRow) findViewById(R.id.message_row);
            TableLayout table = (TableLayout) findViewById(R.id.layout_table);
            table.removeView(messageRow);
            try {
                Glide.with(MainActivity.this).load(data.getData()).fitCenter().into(imageView);
                String exifPath = getRealPathFromURI(this.getApplicationContext(), data.getData());
                System.out.println("HERE HERE HERE: " + exifPath);
                exif = new ExifInterface(exifPath);

                //Getting all the Exif attributes
                TextView width = (TextView) findViewById(R.id.image_width);
                width.setText(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
                TextView height = (TextView) findViewById(R.id.image_height);
                height.setText(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
                TextView sizeBytes = (TextView) findViewById(R.id.image_size_bytes);
                sizeBytes.setText("Cannot");   //CAN'T GET WITH EXIFINTERFACE
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
                TextView coords = (TextView) findViewById(R.id.image_flash);
                //coords.setText(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + " " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

                double lat = 0;
                double lon = 0;

                String latString = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String lonString = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);


                if (!latString.equals(null) && !lonString.equals(null)) {


                    final MapView gMap = (MapView) findViewById(R.id.map);

                    //TODO GET LAT LONG FROM EXIF (IF IT EXISTS)

                    //TODO ADD MOBILE PHONE GEOLOCATION TO MAP

                    gMap.getMap().addMarker(new MarkerOptions().position(new LatLng(48.19, 11.56)).title(getString(R.string.map_original_position)));

                    MarkerOptions newPosMarker = new MarkerOptions().position(new LatLng(61.22, 11.56)).title(getString(R.string.map_new_position)).visible(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    gMap.getMap().addMarker(newPosMarker);
                }

            } catch (Exception e) {
                createErrorDialog(android.R.string.dialog_alert_title, R.string.photo_selector_error_text);
            }
        } else {
            //Couldn't load the image, display an error message
            createErrorDialog(android.R.string.dialog_alert_title, R.string.photo_selector_error_text);
        }
    }

    public void createErrorDialog(int title, int message) {
    //Couldn't load the image, display an error message
    new AlertDialog.Builder(MainActivity.this)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
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

    public boolean saveImage() {
        try {
            //Save the modified image
            exif.setAttribute(ExifInterface.TAG_MAKE, ((TextView) findViewById(R.id.image_camera)).getText().toString());
            exif.saveAttributes();
            View coordLayout = findViewById(R.id.main_content);
            Snackbar successSnackbar = Snackbar.make(coordLayout, R.string.photo_save_success_text, Snackbar.LENGTH_SHORT);
            successSnackbar.show();
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

            if (this.exif == null) {
                createErrorDialog(R.string.photo_save, R.string.photo_missing_image_error_text);
            } else {
                boolean result = saveImage();
                if (result == false) {
                    createErrorDialog(R.string.photo_save, R.string.photo_save_error_text);
                }
            }

        }

        if (id == R.id.action_clear_EXIF) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
