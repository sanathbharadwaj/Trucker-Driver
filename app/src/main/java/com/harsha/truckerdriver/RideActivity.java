package com.harsha.truckerdriver;

import android.Manifest;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RideActivity extends AppCompatActivity {

    public GPSTracker gpsTracker;
    private ParseObject request;
    private ImageButton navButton;
    private ParseObject driver;
    private Button rightButton;
    private LatLng navLatLng;
    private ParseObject user;
    private ParseObject twoLocations;
    public float rideDistance = 0;
    private int totalFare;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        getRequestData();
        startGPSService();
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



    public void startGPSService()
    {
        if(Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            gpsTracker = new GPSTracker(this);
            sendMyLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gpsTracker = new GPSTracker(this);
            sendMyLocation();
        }
    }

    void sendMyLocation()
    {
        ParseQuery query = new ParseQuery("Driver");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!= null)
                {
                    sendMyLocation();
                    return;
                }
                driver = objects.get(0);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pushLocation();
                        handler.postDelayed(this, 15000);
                    }
                }, 15000);
            }

        });
    }

    void pushLocation()
    {
        if(driver == null) return;
        Location location = gpsTracker.getLocation();
        if(location == null)
            return;
        ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        driver.put("driverLocation", point);
        driver.saveInBackground();
    }

    public void arrived(View view)
    {
        showToast("Test : arrived");
        rightButton = (Button)view;
        request.put("status", "arrived");
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                {
                    showToast("Error please try again!");
                    return;
                }
                notifyCustomer(1);
                status = Status.ARRIVED;
                rightButton.setText("Start Ride");
                rightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startRide();
                    }
                });
            }
        });
    }

    void startRide()
    {
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
                notifyCustomer(2);
                storeStartData();
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
        });
    }

    void storeStartData()
    {
        Location location = gpsTracker.getLocation();
        ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        twoLocations = new ParseObject("LocationData");
        twoLocations.put("startLocation", point);
        Date date = new Date();
        twoLocations.put("startedAt", date);
        twoLocations.pinInBackground();
    }

    @Override
    public void onBackPressed()
    {
        //Do nothing
    }

    public void cancelRide(View view)
    {
        request.put("status", "cancelled");
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null)
                {
                    showToast("Error please try again");
                    return;
                }
                notifyCustomer(4);
                showToast("Ride had been cancelled");
                status = Status.CANCELED;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    void finishRide(){
        request.put("status", "finished");
        request.put("endedAt", new Date());
        showToast("Total distance: " + rideDistance);
        showToast("Total distance: " + rideDistance);
        totalFare = calculateCash();
        request.put("amount", totalFare);
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!= null)
                {
                    showToast("Error please try again");
                    return;
                }
                notifyCustomer(3);

                status = Status.FINISHED;
                storeEndData();
            }
        });
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
       final int PRICE_PER_KM = 15, PRICE_PER_MINUTE = 3;
        long rideDuration = (new Date().getTime()-
                twoLocations.getDate("startedAt").getTime())/1000;
        showToast("Ride duration: " + rideDuration);
        showToast("Ride duration: " + rideDuration);

        int totalFare = 0, baseFare = 200;
        totalFare = (int)(baseFare + PRICE_PER_KM * (rideDistance/1000) + PRICE_PER_MINUTE * (rideDuration/60));
        return totalFare;

    }

    void notifyCustomer(int id)
    {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("alertId", id);
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
        Intent intent = getIntent();
        final String requestId = intent.getStringExtra("requestId");
        ParseQuery query = new ParseQuery("Request");
        query.whereEqualTo("objectId", requestId);
        query.getInBackground(requestId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    showToast(e.getMessage());
                    return;
                }
                request = object;
                //getUserData(request.getString("username"));
                notifyCustomer(0);
                initializeButtonClickListeners();
                displayData();
            }

        });
    }

    void getUserData(String username)
    {
        ParseQuery query = new ParseQuery("User");
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
            if (addressList.size() == 0) {
                showToast("Please restart your phone");
                return new LatLng(0, 0);
            }
            Address address = addressList.get(0);
            return new LatLng(address.getLatitude(), address.getLongitude());

        }
        return null;

    }
}
