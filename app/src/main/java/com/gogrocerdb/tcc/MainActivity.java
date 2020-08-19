package com.gogrocerdb.tcc;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.gogrocerdb.tcc.Fragment.My_Pending_Order;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.franmontiel.localechanger.LocaleChanger;
import com.gogrocerdb.tcc.Config.BaseURL;
import com.gogrocerdb.tcc.Fonts.CustomTypefaceSpan;
import com.gogrocerdb.tcc.Fragment.DeliveredFragments;
import com.gogrocerdb.tcc.Fragment.Home;

import com.gogrocerdb.tcc.NetworkConnectivity.ConnectionHelper;
import com.gogrocerdb.tcc.NetworkConnectivity.NoInternetConnection;
import com.gogrocerdb.tcc.util.ConnectivityReceiver;
import com.gogrocerdb.tcc.util.CustomVolleyJsonRequest;
import com.gogrocerdb.tcc.util.GPSTracker;
import com.gogrocerdb.tcc.util.LocationUpdatesService;
import com.gogrocerdb.tcc.util.Session_management;
import com.gogrocerdb.tcc.util.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements  ConnectivityReceiver.ConnectivityReceiverListener ,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView iv_profile;
    BottomNavigationView bottomNavigationView;
    private Menu nav_menu;
    private TextView tv_name;
    ImageView imageView;
    TextView mTitle;
    Toolbar toolbar;
    int padding = 0;
    private Bitmap bitmap;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Session_management sessionManagement;
    private static final int PERMISSIONS_REQUEST_Location = 888;
    public static final String MyPREFERENCES = "MyPrefs" ;
    GoogleApiClient mGoogleApiClient;
    public Handler ha;
    Boolean isShow = true;


    private String Longitude;
    private String Latitude;
    ConnectionHelper helper;
    Boolean isInternet;
    GPSTracker gps;
    android.app.AlertDialog alert;
    public static final int REQUEST_LOCATION = 1450;
    private boolean mBound = false;
    private MyReceiver myReceiver;
    private LocationUpdatesService mService = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences= getSharedPreferences("lan", Context.MODE_PRIVATE);
        gps = new GPSTracker(this);
        helper = new ConnectionHelper(this);
        isInternet = helper.isConnectingToInternet();
        myReceiver = new MyReceiver();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > 22) {
                requestPermissions(new String[]{Manifest.permission
                                .ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_Location);
            }

        } else {
            enableHandler();
            //statusCheck();
        }

        editor = sharedPreferences.edit();

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);

            if (view instanceof TextView) {
                TextView textView = (TextView) view;

                Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "Font/Bold.otf");
                textView.setTypeface(myCustomFont);
            }


        }
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



   //     NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        Menu m = navigationView.getMenu();
//        for (
//                int i = 0; i < m.size(); i++)
//
//        {
//            MenuItem mi = m.getItem(i);
//            SubMenu subMenu = mi.getSubMenu();
//            if (subMenu != null && subMenu.size() > 0) {
//                for (int j = 0; j < subMenu.size(); j++) {
//                    MenuItem subMenuItem = subMenu.getItem(j);
//                    applyFontToMenuItem(subMenuItem);
//                }
//            }
//
//            applyFontToMenuItem(mi);
//        }

        sessionManagement = new

                Session_management(MainActivity.this);
//        View headerView = navigationView.getHeaderView(0);
//        //    navigationView.getBackground().setColorFilter(0x80000000, PorterDuff.Mode.MULTIPLY);
//        navigationView.setNavigationItemSelectedListener(this);
//        nav_menu = navigationView.getMenu();
//        View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
//        iv_profile = (ImageView) header.findViewById(R.id.iv_header_img);
//
//        tv_name = (TextView) header.findViewById(R.id.tv_header_name);
   //     updateHeader();
 //       sideMenu();


        if (savedInstanceState == null)
        {
            Fragment fm = new Home();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.contentPanel, fm, "Home_fragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }

        getFragmentManager().
                addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        try {

                            InputMethodManager inputMethodManager = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            Fragment fr = getFragmentManager().findFragmentById(R.id.contentPanel);
                            final String fm_name = fr.getClass().getSimpleName();
                            Log.e("backstack: ", ": " + fm_name);
                            if (fm_name.contentEquals("Home_fragment")) {
                                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                toggle.setDrawerIndicatorEnabled(true);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                toggle.syncState();

                            } else if (fm_name.contentEquals("My_order_fragment") ||
                                    fm_name.contentEquals("Thanks_fragment")) {
                                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                                toggle.setDrawerIndicatorEnabled(false);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                toggle.syncState();
                                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Fragment fm = new Home();
                                        FragmentManager fragmentManager = getFragmentManager();
                                        fragmentManager.beginTransaction().replace(R.id.contentPanel, fm)
                                                .addToBackStack(null).commit();
                                    }
                                });
                            } else {
                                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                toggle.setDrawerIndicatorEnabled(false);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                toggle.syncState();
                                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        onBackPressed();
                                    }
                                });
                            }

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(0);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_order:
                        Fragment fma = new DeliveredFragments();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.contentPanel, fma, "Delivered_Fragments")
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                        break;

                        case R.id.nav_profile:
                            Fragment fma2 = new Home();
                            FragmentManager fragmentManager2 = getFragmentManager();
                            fragmentManager2.beginTransaction()
                                    .replace(R.id.contentPanel, fma2, "Delivered_Fragments")
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .commit();
                        break;

                    case R.id.nav_log_out:
                        sessionManagement.logoutSession();
                        finish();
                        break;

                    case R.id.nav_pending_orders:
                        Fragment fma1 = new My_Pending_Order();
                        FragmentManager fragmentManager1 = getFragmentManager();
                        fragmentManager1.beginTransaction()
                                // .replace(R.id.contentPanel, fma, "Pending_fragment")
                                .replace(R.id.contentPanel, fma1, "Home_fragment")
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                        break;

                }
                return true;
            }
        });

    }

    public void updateHeader() {
        if (sessionManagement.isLoggedIn()) {
            String getname = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
            tv_name.setText(getname);
        }
    }


    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "Font/CircularStd-Book.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }


    public void sideMenu()
    {
        if (sessionManagement.isLoggedIn()) {
            nav_menu.findItem(R.id.nav_log_out).setVisible(true);
        } else {
//            tv_name.setText(getResources().getString(R.string.btn_login));
//            tv_name.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent i = new Intent(MainActivity.this, LogInActivity.class);
//                    startActivity(i);
//                }
//            });
            nav_menu.findItem(R.id.nav_log_out).setVisible(false);

        }
    }


    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {

        if (!isConnected) {
            Intent intent = new Intent(MainActivity.this, NoInternetConnection.class);
            startActivity(intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.language, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_language) {
//            openLanguageDialog();
//        }


        return super.onOptionsItemSelected(item);
    }


    private void openLanguageDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_language, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        TextView lEnglish = v.findViewById(R.id.l_english);
        TextView lSpanish = v.findViewById(R.id.l_arabic);
        final AlertDialog dialog = builder.create();

        lEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocaleChanger.setLocale(Locale.ENGLISH);
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                editor.putString("language", "english");
                editor.apply();

                recreate();
                dialog.dismiss();
            }
        });
        lSpanish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocaleChanger.setLocale(new Locale("ar", "ARABIC"));
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                editor.putString("language", "spanish");
                editor.apply();

                recreate();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_LOCATION){
            if(resultCode == Activity.RESULT_OK){

                enableHandler();
            }else{
                statusCheck();
            }
        }
    }

    private void enableLoc() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);

                        }catch (NullPointerException | IntentSender.SendIntentException e){
                            e.printStackTrace();
                            try{
                                status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                            }catch (Exception ee){
                                ee.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });

    }

    public void enableHandler(){
        ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                statusCheck();
                ha.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void showDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Connect to network or quit")
                .setCancelable(false)
                .setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        if (alert == null) {
            alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_Location) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                statusCheck();

            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    private void updateLiveLocation( String lat, String longi) {

        SharedPreferences userDetails = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        final String userId = userDetails.getString("user_id", "");
        String tag_json_obj = "json_product_req";
        Map<String, String> params = new HashMap<String, String>();


        params.put("latitude", lat);
        params.put("longitude", longi);
        params.put("emp_id", userId);


        CustomVolleyJsonRequest jsonObjReq = new CustomVolleyJsonRequest(Request.Method.POST,
                BaseURL.updateLocatio, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                Log.v("loc_called", Longitude + " "+ Latitude);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if(isShow){
                enableLoc();
                isShow = false;
            }

        }else{
            isShow = true;
            gps = new GPSTracker(this);
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                if (latitude == 0.0 && longitude == 0.0) {
                    //Toast.makeText(getApplicationContext()," Network Error Sorry We Cannot Find Your Location Plz Check Your Settings Or contact your System Adminitrator  " ,Toast.LENGTH_LONG).show();
                    statusCheck();
                    //gps.showSettingsAlert();
                } else {
                    Longitude=Double.toString(longitude);;
                    Latitude=Double.toString(latitude);
                    //	Toast.makeText(getApplicationContext(),"Your Location is - \nLat: " + latitude+ "\nLong: " + longitude,Toast.LENGTH_LONG).show();

                    if (helper.isConnectingToInternet()) {
                        if (mService != null){
                            mService.requestLocationUpdates();
                        }

                        updateLiveLocation(Latitude,Longitude);
                        if (alert != null && alert.isShowing()) {
                            alert.dismiss();
                            alert = null;
                        }
                    } else {
                        showDialog();
                    }

                }
            }

        }
    }

    @Override
    protected void onStart()  {
        super.onStart();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
          /*  setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));*/
        }
    }


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {

                // userStartedLocationSharing();

                // sendCoordinates(Utils.getLocationLatLng(location));
                //   Toast.makeText(MainActivity.this, Utils.getLocationText(location), Toast.LENGTH_SHORT).show();

                updateLiveLocation(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
               // upDateCoordinates(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
            }
        }
    }


    void showDialogeProfile()
    {
        final Dialog dialog;
        ImageView img_close;
        ImageView iv_header_img;
        TextView tv_header_name;
        // Dialoge box
        dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialoguebox_profile);
        dialog.setCancelable(false);


        //top menu
        img_close = dialog.findViewById(R.id.img_close);
        iv_header_img = dialog.findViewById(R.id.iv_header_img);
        tv_header_name = dialog.findViewById(R.id.tv_header_name);

        if (sessionManagement.isLoggedIn()) {
            String getname = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
            tv_header_name.setText(getname);
        }

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}