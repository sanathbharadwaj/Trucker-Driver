package com.harsha.truckerdriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class RideFinishedActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_finished);
        setCash();

        mToolbar = (Toolbar) findViewById(R.id.nav_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setCash()
    {
        Intent intent = getIntent();
        int fare = intent.getIntExtra("fare", 0);
        TextView amount = findViewById(R.id.amount);
        String cashText = "Rs " + fare;
        amount.setText(cashText);
    }


    public void onFinish(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
