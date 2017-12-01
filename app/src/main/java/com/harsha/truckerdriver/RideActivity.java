package com.harsha.truckerdriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class RideActivity extends AppCompatActivity {

    public GPSTracker gpsTracker;
    public enum GoodType{
         ELECTRICAL_ELECTRONICS, FURNITURE, TIMBER_PLYWOOD, TEXTILE, PHARMACY, FOOD, CHEMICALS, PLASTIC
    }

     public enum PaymentMode{
        CASH, PAYTM, CREDIT_CARD
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        getRequestData();
    }

    void startGPSService()
    {
        if(Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
            gpsTracker = new GPSTracker(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gpsTracker = new GPSTracker(this);
        }
    }

    void getRequestData()
    {
        Intent intent = getIntent();
        String requestId = intent.getStringExtra("requestId");
        ParseQuery query = new ParseQuery("Request");
        query.whereEqualTo("objectId", requestId);
        query.getInBackground(requestId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e != null)
                {
                    showToast(e.getMessage());
                    return;
                }

                displayData(object);
            }

        });
    }

    void displayData(ParseObject request)
    {
       getTextView(R.id.ride_name).setText(request.getString("username"));
       getTextView(R.id.ride_source).setText(request.getString("source"));
       getTextView(R.id.ride_destination).setText(request.getString("destination"));
       getTextView(R.id.ride_good_type).setText(getGoodType(GoodType.values()[request.getInt("goodType")]));
       getTextView(R.id.ride_payment_mode).setText(getPaymentMode(PaymentMode.values()[request.getInt("paymentMode")]));
    }

    String getGoodType(GoodType goodType)
    {
        switch (goodType)
        {
            case ELECTRICAL_ELECTRONICS: return "Electrical/Electronics";
            case FURNITURE: return "Furniture";
            case TIMBER_PLYWOOD: return "Timber/Plywood";
            case TEXTILE: return "Textile/Garments";
            case PHARMACY: return "Pharmacy/Medical Supplies";
            case FOOD: return "Food Items";
            case CHEMICALS: return "Chemicals/Paints";
            case PLASTIC: return "Plastic/Rubber";
            default: return "Not Specified";
        }


    }

    String getPaymentMode(PaymentMode mode)
    {
        String value;
        switch (mode) {
            case CASH: value = "Cash";
                break;
            case PAYTM: value = "Paytm";
                break;
            case CREDIT_CARD: value = "Credit Card";
                break;
            default: value = "Not Specified";
        }

        return value;
    }
    void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }
}
