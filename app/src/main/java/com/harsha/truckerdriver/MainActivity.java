package com.harsha.truckerdriver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import static com.harsha.truckerdriver.Utilities.*;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Date date;
    boolean noDataLoading;
    private PopupWindow popupWindow;
    private CountDownTimer myTimer;
    private View popupView;
    private TextView timeLeft;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    NavigationView navigationView;
    boolean popupExists = false;
    ParseObject driver;
    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;
    public Location lastLocation;
    ParseObject request;
    private int checkingCount;
    private NotificationManager notificationManager;

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            //this.finish();
        }
    }

    public void Logout(){
        ParseUser.logOut();
        showToast("Logged Out Successfully");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForCurrentTrip();
        if(MainActivity.this.isFinishing()) return;
        noDataLoading = true;
        loadDriverData();
        initializeLocationService();
        registerReceiver();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        checkIfFromNotification();
        checkForNewVersion();
        initializeNavigationDrawer();



    }


    void initializeNavigationDrawer()
    {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.nav_action_bar);
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setDisplayShowHomeEnabled(true);


        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.profile:
                        Intent h1 = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(h1);
                        mDrawerLayout.closeDrawers();
                        break;


                    case R.id.past_trips:
                        if(driver==null){
                            showToast("error loading data");
                        }else {
                            Intent h3 = new Intent(MainActivity.this, PastTripsActivity.class);
                            h3.putExtra("driverId", driver.getObjectId());
                            startActivity(h3);
                            mDrawerLayout.closeDrawers();
                        }
                        break;


                    case R.id.our_rates:
                        Intent h4 = new Intent(MainActivity.this, OurRatesActivity.class);
                        startActivity(h4);
                        mDrawerLayout.closeDrawers();
                        break;


                    case R.id.about_us:
                        Intent h6 = new Intent(MainActivity.this, AboutUsActivity.class);
                        startActivity(h6);
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.contact_us:
                        Intent h7 = new Intent(MainActivity.this, ContactUsActivity.class);
                        startActivity(h7);
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.support:
                        Intent h8 = new Intent(MainActivity.this, SupportActivity.class);
                        startActivity(h8);
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.logout:
                        Logout();
                        Intent h9 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(h9);
                        mDrawerLayout.closeDrawers();
                        finish();
                        break;


                }

                return false;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)){
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIfFromNotification() {
        Intent intent = getIntent();
        String extra = intent.getStringExtra("extra");
        if(extra != null && !extra.equals("cancelled")) {
            notificationManager.cancel(intent.getIntExtra("id", 0));
            getRequest(extra);
        }
    }

    private void checkForNewVersion() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Update");
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null || objects.size() == 0) return;
                if(objects.get(0).getInt("version") > BuildConfig.VERSION_CODE)
                {
                    showAppUpdateAlert();
                }
            }
        });
    }

    private void showAppUpdateAlert() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("New Version Available")
                .setMessage("Please update to the new version in order to continue using TruckerDriver")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Add app url
                        String url = "https://www.google.com";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    void checkForCurrentTrip() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.package_name), MODE_PRIVATE);
        if (prefs.getBoolean(getString(R.string.is_running), false)) {
            loadActivityAndFinish(this, RideActivity.class);
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;
            if(action.equals("NEW_REQUEST"))
            {
                getRequest(intent.getStringExtra("requestId"));
            }
            if(action.equals("RIDE_CANCELLED"))
            {
                showToast("Ride has been cancelled by the customer");
            }

        }
    };

    void getRequest(String objectId){
        final ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e!=null)
                {
                    showToast("Failed to load request");
                    showToast(e.getMessage());
                    return;
                }
                request = object;
                showPopup(request);

            }
        });

    }

    void registerReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NEW_REQUEST");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    void initializeLocationService()
    {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {


            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                showSettingsAlert();

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startLocation();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                startLocation();
            }
        }
    }

    void startLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 20, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startLocation();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                finish();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }



    void loadDriverData()
    {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Driver");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!= null){
                    showToast("Failed to load driver data");
                    return;
                }
                driver = objects.get(0);
                driver.put("isAvailable", true);
                driver.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                    }
                });
                saveInstallation();
                updateDriverUI();

                //sendDriverLocation();

            }
        });
    }

    private void saveInstallation() {
        ParseInstallation current = ParseInstallation.getCurrentInstallation();
        current.put("driver", driver);
        current.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null)
                    showToast(e.getMessage());
                else
                {
                    showToast("Instalz: " + ParseInstallation.getCurrentInstallation().getInstallationId());
                }
            }
        });
    }

    public void onGoOffline(View view)
    {
        TextView onlineStatusText = findViewById(R.id.online_textview);
        if(driver==null) return;
        Button button = findViewById(view.getId());
        if(driver.getBoolean("isAvailable"))
        {
            driver.put("isAvailable", false);
            button.setText(R.string.go_online);
            button.setBackgroundColor(getResources().getColor(R.color.green));
            onlineStatusText.setTextColor(getResources().getColor(R.color.red));
            onlineStatusText.setText(R.string.offline);
        }
        else
        {
            driver.put("isAvailable", true);
            button.setText(R.string.go_offline);
            button.setBackgroundColor(getResources().getColor(R.color.red));
            onlineStatusText.setTextColor(getResources().getColor(R.color.online_status));
            onlineStatusText.setText(R.string.online);
        }
        driver.saveEventually();
    }

    private void sendDriverLocation() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ParseGeoPoint point = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                driver.put("driverLocation", point);
                driver.saveInBackground();
                handler.postDelayed(this, 60000);
            }
        }, 1000);
    }

    void updateDriverUI()
    {
        getTextView(R.id.main_name).setText(driver.getString("username"));
        getTextView(R.id.car_details).setText(driver.getString("vehicleName") + "|" + driver.getString("vehicleNumber"));
        getTextView(R.id.profile_text).setText(ParseUser.getCurrentUser().getString("name"));
        getTextView(R.id.phone_text).setText(ParseUser.getCurrentUser().getString("phone"));
        Double rating = (Double) driver.getNumber("rating");
        if(rating== null) rating = 0.0;
        double valueRounded = Math.round(rating * 10D) / 10D;
        getTextView(R.id.rating_value).setText(Double.toString(valueRounded));


        getDriverImage();
    }

    void getDriverImage()
    {
        ParseFile file = (ParseFile)driver.get("profilePic");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if(e != null)
                {
                    showToast("Error retrieving driver image");
                    return;
                }
                setDriverImageView(data);
            }
        });
    }

    void setDriverImageView(byte[] data)
    {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        ImageView imageView = (ImageView) findViewById(R.id.circleImageView2);
        imageView.setImageBitmap(bitmap);
    }

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }

    public void checkForRequests(View view)
    {
        checkingCount = 0;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
             if(noDataLoading && !popupExists)
                 getNewRequest();
             checkingCount++;
             if(checkingCount!=5)
             handler.postDelayed(this, 4000);
            }
        }, 4000);
    }

    void getNewRequest()
    {
        noDataLoading = false;
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        if(date != null)
        query.whereGreaterThan("updatedAt", date);
        query.whereEqualTo("status", "accepted");
        query.orderByDescending("updatedAt");
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                noDataLoading = true;
                if(e!=null) {
                    showToast("Network Error");
                    return;
                }
                if(objects.size() == 0)
                    return;
                request = objects.get(0);
                date = request.getUpdatedAt();
                showPopup(request);
            }

        });
    }

    void showPopup(ParseObject object)
    {
        if(popupExists) return;
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.main_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        popupView = inflater.inflate(R.layout.request_popup, null);

        // create the popup window
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT + 1000;
        //int height = 350;
        popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.BOTTOM, 0, 0);
        popupExists = true;

        String sourceAddress = object.getString("source");
        String destinationAddress = object.getString("destination");
        String customer = object.getString("username");

        TextView customerName = (TextView) popupView.findViewById(R.id.ride_name),
                source = (TextView) popupView.findViewById(R.id.source), destination = (TextView) popupView.findViewById(R.id.destination);

        customerName.setText(customer);
        source.setText(sourceAddress);
        destination.setText(destinationAddress);
        startTimer();

    }

    void startTimer()
    {
        timeLeft = popupView.findViewById(R.id.accept_text);
        myTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                String text = "You have " + l/1000 + " seconds to accept";
                timeLeft.setText(text);
            }

            @Override
            public void onFinish() {
                popupWindow.dismiss();
                popupExists = false;
            }
        };

        myTimer.start();
    }

    void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void onAccept(View view)
    {
        assert notificationManager != null;
        popupWindow.dismiss();
        myTimer.cancel();
        acceptRequest();
       /* request.put("driverId", ParseUser.getCurrentUser().getString("driverId"));
        request.put("status", "assigned");
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!= null)
                {
                    showToast(e.getMessage());
                    return;
                }
                goToRideActivity();
            }
        });*/
    }

    void acceptRequest()
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("requestId", request.getObjectId());
        params.put("driverId", ParseUser.getCurrentUser().getString("driverId"));
        ParseCloud.callFunctionInBackground("acceptRequest", params, new FunctionCallback<Integer>() {
           public void done(Integer res, ParseException e)
           {
                if(e!=null)
                {
                    showToast("Failed to connect to server");
                    return;
                }
                if(res == 1) {
                    notificationManager.cancelAll();
                    goToRideActivity();
                }
                else
                {
                    showToast("The request is either cancelled or already accepted");
                }
                popupExists = false;
           }
        });
    }

    void goToRideActivity()
    {
        Intent intent = new Intent(getApplicationContext(), RideActivity.class);
        intent.putExtra("requestId", request.getObjectId());
        startActivity(intent);
    }
}
