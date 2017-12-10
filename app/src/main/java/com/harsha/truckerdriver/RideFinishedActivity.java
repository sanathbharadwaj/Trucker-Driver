package com.harsha.truckerdriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RideFinishedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_finished);
    }


    public void onFinish(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
