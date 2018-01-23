package com.harsha.truckerdriver;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ContactUsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);


      //  call();
        calls();
        mToolbar = (Toolbar) findViewById(R.id.nav_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



    }

    //void call()
    //{
    //  ImageButton btn2 = (ImageButton) findViewById(R.id.contact_button);
      //btn2.setOnClickListener(new View.OnClickListener() {
        //  @Override
          //public void onClick(View view) {

            //  Intent intent = new Intent(Intent.ACTION_DIAL);
              //intent.setData(Uri.parse("tel:8792111296"));
                //artActivity(intent);
         //   }
       // });
   // }
    void calls()
    {
        TextView btn12 = (TextView) findViewById(R.id.phone_us);
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:8792111296"));
                startActivity(intent);
            }
        });
    }
   public void email(View view)
    {

        try{
            Intent intent1 = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + "ssmelmuri@gmail.com"));
            startActivity(intent1);
        }catch(ActivityNotFoundException e){
            //TODO write here
        }
    }

}
