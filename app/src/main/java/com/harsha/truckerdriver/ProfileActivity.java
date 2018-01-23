package com.harsha.truckerdriver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    Toolbar mToolbar;
    TextView m_toolbar_title;
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.nav_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        /*Button ed = (Button) findViewById(R.id.edit_prof);

        // Capture button clicks
        ed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                // Start NewActivity.class
                Intent intent = new Intent(ProfileActivity.this,
                        EditProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });*/





        loadUserData();

    }

    void loadUserData() {

        ParseQuery<ParseObject> query = new ParseQuery<>("User");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    showToast("Failed to load user data");
                    return;
                }
                updateUserUI();
            }
        });
    }

    void updateUserUI() {
        getTextView(R.id.name).setText(ParseUser.getCurrentUser().getString("name"));
        getTextView(R.id.phone_no).setText(ParseUser.getCurrentUser().getString("phone"));
        getTextView(R.id.email_add).setText(ParseUser.getCurrentUser().getString("email"));

    }
    TextView getTextView(int id)
    {
        return (TextView)findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
