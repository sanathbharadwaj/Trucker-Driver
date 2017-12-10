package com.harsha.truckerdriver;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Date date;
    boolean noDataLoading;
    private PopupWindow popupWindow;
    private CountDownTimer myTimer;
    private View popupView;
    private TextView timeLeft;
    boolean popupExists = false;
    private ParseObject object;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noDataLoading = true;
        checkingForRequests();

    }

    void checkingForRequests()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
             if(noDataLoading && !popupExists)
                 getNewRequest();
             handler.postDelayed(this, 4000);
            }
        }, 4000);
    }

    void getNewRequest()
    {
        noDataLoading = false;
        ParseQuery query = new ParseQuery("Request");
        if(date != null)
        query.whereGreaterThan("updatedAt", date);
        query.whereEqualTo("status", "accepted");
        query.orderByAscending("updatedAt");
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
                object = objects.get(0);
                date = object.getUpdatedAt();
                showPopup();
            }

        });
    }

    void showPopup()
    {
        showToast("Showing Popup: " + object.getString("source"));
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.main_layout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.request_popup, null);

        // create the popup window
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
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
        myTimer.cancel();
        object.put("driverId", ParseUser.getCurrentUser().getObjectId());
        object.put("status", "assigned");
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!= null)
                {
                    showToast(e.getMessage());
                    return;
                }
                goToRideActivity();
            }
        });
    }

    void goToRideActivity()
    {
        Intent intent = new Intent(getApplicationContext(), RideActivity.class);
        intent.putExtra("requestId", object.getObjectId());
        startActivity(intent);
    }
}
