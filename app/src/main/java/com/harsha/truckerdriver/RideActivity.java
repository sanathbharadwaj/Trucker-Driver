package com.harsha.truckerdriver;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.List;

public class RideActivity extends AppCompatActivity {

    private ParseObject request;

    public enum GoodType {
        ELECTRICAL_ELECTRONICS, FURNITURE, TIMBER_PLYWOOD, TEXTILE, PHARMACY, FOOD, CHEMICALS, PLASTIC
    }

    public enum PaymentMode {
        CASH, PAYTM, CREDIT_CARD
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String phno = "tel:07204326536";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        getRequestData();

        ImageButton btn = (ImageButton) findViewById(R.id.ride_call);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(phno));
                startActivity(intent);
            }
        });
        ImageButton btn1 = (ImageButton) findViewById(R.id.ride_nav);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double latitude = 13.018912;
                double longitude = 76.104395;
                ParseGeoPoint x = request.getParseGeoPoint("location");
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + x.getLatitude() + "," + x.getLongitude()));
                startActivity(intent);
            }
        });

    }

    void getRequestData() {
        Intent intent = getIntent();
        String requestId = intent.getStringExtra("requestId");
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
                displayData();
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
