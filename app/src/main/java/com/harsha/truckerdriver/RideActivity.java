package com.harsha.truckerdriver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RideActivity extends AppCompatActivity {

    public GPSTracker gpsTracker;
    public ParseObject request;
    private ImageButton navButton;
    public ParseObject driver;
    private Button rightButton;
    private LatLng navLatLng;
    private ParseObject user;
    private ParseObject twoLocations;
    public float rideDistance = 0;
    private int totalFare;
    public List<LatLng> driverPath;
    private boolean isOldTrip = false;
    public SharedPreferences.Editor editor;
    SharedPreferences prefs;
    private ProgressBar imgLoad;
    private ImageView imgBack1;
    private Toolbar mToolbar;
    private String requestId;
    TextView rideStatusText;


    public enum GoodType {
        ELECTRICAL_ELECTRONICS, FURNITURE, TIMBER_PLYWOOD, TEXTILE, PHARMACY, FOOD, CHEMICALS, PLASTIC
    }

    public enum PaymentMode {
        CASH, PAYTM, CREDIT_CARD
     }

    public enum Status{
        ACCEPTED, ASSIGNED, ARRIVED, STARTED, FINISHED, CANCELED
    }

    public Status status = Status.ASSIGNED;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        driverPath = new ArrayList<>();
        editor = getSharedPreferences(getString(R.string.package_name), MODE_PRIVATE).edit();
        prefs = getSharedPreferences(getString(R.string.package_name), MODE_PRIVATE);
        rightButton = findViewById(R.id.start_ride);
        imgLoad = (ProgressBar)findViewById(R.id.loader);
        imgBack1 = (ImageView)findViewById(R.id.img_back1);
        rideStatusText = findViewById(R.id.ride_status_text);
        checkForCurrentTrip();
        getDriver();
        startGPSService();
        registerReceiver();

        mToolbar = (Toolbar) findViewById(R.id.nav_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    void getDriver()
    {
        ParseQuery<ParseObject> query = new ParseQuery<>("Driver");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                driver = objects.get(0);
                driver.put("isAvailable", false);
                driver.saveEventually();

            }
        });
    }


    void initializeButtonClickListeners()
    {
        ParseGeoPoint point = request.getParseGeoPoint("location");
        navLatLng = new LatLng(point.getLatitude(), point.getLongitude());

        ImageButton btn = (ImageButton) findViewById(R.id.ride_call);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(request == null) return;
                String phone = request.getString("phoneNumber");
                String phoneNumber = "tel:" + phone;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(phoneNumber));
                startActivity(intent);
            }
        });
        navButton = (ImageButton) findViewById(R.id.ride_nav);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ParseGeoPoint x = request.getParseGeoPoint("location");
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + navLatLng.latitude + "," + navLatLng.longitude));
                startActivity(intent);
            }
        });
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;
            if(action.equals("RIDE_CANCELLED"))
            {
                showToast("Ride has been cancelled by the customer");
                saveNoRunningRide();
                finish();
            }
        }
    };

    void registerReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RIDE_CANCELLED");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    void checkForCurrentTrip()
    {
        Intent intent = getIntent();
        if(prefs.getBoolean(getString(R.string.is_running), false))
        {
            isOldTrip = true;
            rideDistance += prefs.getFloat("rideDistance", 0);
            requestId = prefs.getString("requestId", "NULL");
            getRequestData();
        }
        else
        {
            requestId = intent.getStringExtra("requestId");
            editor.putString("requestId", requestId);
            editor.apply();
            getRequestData();
            saveRunningState(true);
        }

    }





    public void startGPSService()
    {
        if(Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            gpsTracker = new GPSTracker(this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gpsTracker = new GPSTracker(this);
        }
    }

    void sendMyLocation()
    {
        final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pushLocation();
                        handler.postDelayed(this, 10000);
                    }
                }, 1000);
    }

    void pushLocation()
    {
        if(driver == null) return;
        if(driverPath.size() == 0) return;
        Location location = gpsTracker.getLocation();
        if(location == null)
            return;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<LatLng>>() {}.getType();
        String pathJSON = gson.toJson(driverPath, listType);
        byte[] data = pathJSON.getBytes();
        final ParseFile file = new ParseFile("path.txt", data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                request.put("driverPath", file);
                request.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e!=null)
                            showToast(e.getMessage());
                    }
                });
            }
        });


    }

    public void arrived(View view)
    {
        imgBack1.setVisibility(View.VISIBLE);
        imgLoad.setVisibility(View.VISIBLE);
        request.put("status", "arrived");
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                {
                    showToast("Error please try again!");
                    return;
                }
                rideStatusText.setText(R.string.arrived_status_text);
                notifyCustomer(1, "NULL");
                imgBack1.setVisibility(View.GONE);
                imgLoad.setVisibility(View.GONE);
                setArrivedStatus();
            }
        });
    }

    void setArrivedStatus()
    {
        status = Status.ARRIVED;
        rightButton.setText("Start Ride");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRide();
            }
        });

    }


    void startRide()
    {
        imgBack1.setVisibility(View.VISIBLE);
        imgLoad.setVisibility(View.VISIBLE);
        request.put("status", "started");
        request.put("startedAt", new Date());
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    showToast("Error please try again");
                    return;
                }

                //notifyCustomer(2);
                rideStatusText.setText("Ride in Progress");
                notifyCustomer(2, driver.getString("username"));
                imgBack1.setVisibility(View.GONE);
                imgLoad.setVisibility(View.GONE);
                storeStartData();
                setStartStatus();
            }
        });
    }

    private void setStartStatus() {
        navLatLng = getDestination(request.getString("destination"));
        rightButton.setText("End Ride");
        status = Status.STARTED;
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishRide();
            }
        });

    }

    void storeStartData()
    {
        Location location = gpsTracker.getLocation();
        ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        twoLocations = new ParseObject("LocationData");
        twoLocations.put("startLocation", point);
        twoLocations.put("id", request.getObjectId());
        Date date = new Date();
        editor.putLong("startedAt", date.getTime());
        editor.apply();
        twoLocations.put("startedAt", date);
        twoLocations.pinInBackground();
    }

    @Override
    public void onBackPressed()
    {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    void saveRunningState(boolean state)
    {
        editor.putBoolean(getString(R.string.is_running), state);
        editor.apply();
    }

    public void cancelRide(View view)
    {
        saveRunningState(false);
        request.put("status", "cancelled");
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null)
                {
                    showToast("Error please try again");
                    return;
                }
                notifyCustomer(4, "NULL");
                showToast("Ride had been cancelled");
                status = Status.CANCELED;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                driver.put("isAvailable", true);
                driver.saveEventually();
                finish();
            }
        });
    }


    void finishRide(){
        imgBack1.setVisibility(View.VISIBLE);
        imgLoad.setVisibility(View.VISIBLE);
        request.put("status", "finished");
        request.put("endedAt", new Date());
        showToast("Total distance: " + rideDistance);
        showToast("Total distance: " + rideDistance);
        totalFare = calculateCash();
        driver.put("isAvailable", true);
        driver.saveEventually();
        request.put("amount", totalFare);
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!= null)
                {
                    showToast("Error please try again");
                    return;
                }
                notifyCustomer(3, "Rs. "+ totalFare);

                //notifyCustomer(3);

                status = Status.FINISHED;
                imgBack1.setVisibility(View.GONE);
                imgLoad.setVisibility(View.GONE);
                storeEndData();
            }
        });
        saveNoRunningRide();

    }

    void storeEndData()
    {
        Location location = gpsTracker.getLocation();
        ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        twoLocations = new ParseObject("LocationData");
        twoLocations.put("endLocation", point);
        Date date = new Date();
        twoLocations.put("endedAt", date);
        twoLocations.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null){showToast("Error try again");return;}
                Intent intent = new Intent(getApplicationContext(), RideFinishedActivity.class);
                intent.putExtra("fare", totalFare);
                startActivity(intent);
                gpsTracker.stopUsingGPS();
                finish();
            }
        });
    }

    int calculateCash()
    {
        long startTime = prefs.getLong("startedAt", 0);
       final int PRICE_PER_KM = 15, PRICE_PER_MINUTE = 3;
        long rideDuration = (new Date().getTime()-
                startTime)/1000;
        showToast("Ride duration: " + rideDuration);
        showToast("Ride duration: " + rideDuration);

        int totalFare = 0, baseFare = 200;
        totalFare = (int)(baseFare + PRICE_PER_KM * (rideDistance/1000) + PRICE_PER_MINUTE * (rideDuration/60));
        return totalFare;

    }


    void notifyCustomer(int id, String extra)
    {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("alertId", id);
        params.put("extra", extra);
        params.put("installation", request.getString("userInsId"));
        ParseCloud.callFunctionInBackground("sendNotification", params, new FunctionCallback<Integer>() {
            public void done(Integer res, ParseException e) {
                if (e == null) {
                    Log.i("Notification", "Successfully sent");
                }
            }
        });
    }



    void getRequestData() {
        ParseQuery<ParseObject> query = new ParseQuery<>("Request");
        query.whereEqualTo("objectId", requestId);
        query.getInBackground(requestId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }
                request = object;
                sendMyLocation();
                //getUserData(request.getString("username"));
                if(!isOldTrip)
                notifyCustomer(0, ParseUser.getCurrentUser().getUsername());
                initializeButtonClickListeners();
                displayData();
                if(isOldTrip)
                updateRideState();
            }

        });
    }

    private void updateRideState() {
        String statusLocal = request.getString("status");
        switch (statusLocal)
        {
            case "assigned":  status = Status.ASSIGNED;
            notifyCustomer(0, ParseUser.getCurrentUser().getUsername());break;
            case "arrived": setArrivedStatus();break;
            case "started": setStartStatus();break;
            case "finished": saveNoRunningRide();
                finish(); break;
            case "cancelled": saveNoRunningRide(); showToast("Ride has been cancelled by the customer");
                                finish();
        }
    }

    void saveNoRunningRide()
    {
        SharedPreferences.Editor prefs = getSharedPreferences(getString(R.string.package_name), MODE_PRIVATE).edit();
        prefs.putBoolean(getString(R.string.is_running), false);
        prefs.apply();
        editor.clear();
        editor.apply();
    }

    void getUserData(String username)
    {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("User");
        query.whereEqualTo("username", username);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    showToast("Error!!");
                    return;
                }
                if(objects.size() == 0)
                {
                    showToast("Error 0 objects");
                    return;
                }
                user = objects.get(0);
            }

        });
    }

    void displayData() {
        getTextView(R.id.ride_name).setText(request.getString("username"));
        getTextView(R.id.ride_source).setText(request.getString("source"));
        getTextView(R.id.ride_destination).setText(request.getString("destination"));
        getTextView(R.id.ride_good_type).setText(getGoodType(GoodType.values()[request.getInt("goodType")]));
        getTextView(R.id.ride_payment_mode).setText(getPaymentMode(PaymentMode.values()[request.getInt("paymentMode")]));
    }

    String getGoodType(GoodType goodType) {
        switch (goodType) {
            case ELECTRICAL_ELECTRONICS:
                return "Electrical/Electronics";
            case FURNITURE:
                return "Furniture";
            case TIMBER_PLYWOOD:
                return "Timber/Plywood";
            case TEXTILE:
                return "Textile/Garments";
            case PHARMACY:
                return "Pharmacy/Medical Supplies";
            case FOOD:
                return "Food Items";
            case CHEMICALS:
                return "Chemicals/Paints";
            case PLASTIC:
                return "Plastic/Rubber";
            default:
                return "Not Specified";
        }


    }

    String getPaymentMode(PaymentMode mode) {
        String value;
        switch (mode) {
            case CASH:
                value = "Cash";
                break;
            case PAYTM:
                value = "Paytm";
                break;
            case CREDIT_CARD:
                value = "Credit Card";
                break;
            default:
                value = "Not Specified";
        }

        return value;
    }

    void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    TextView getTextView(int id) {
        return (TextView) findViewById(id);
    }

    LatLng getDestination(String addressText) {
        List<Address> addressList = null;

        if (!addressText.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(addressText, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO:Handle this error.
            if (addressList == null || addressList.size() == 0) {
                showToast("Please restart your phone");
                return new LatLng(0, 0);
            }
            Address address = addressList.get(0);
            return new LatLng(address.getLatitude(), address.getLongitude());
        }
        return null;

    }
}
