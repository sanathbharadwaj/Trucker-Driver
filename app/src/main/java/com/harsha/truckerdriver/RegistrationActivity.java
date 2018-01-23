package com.harsha.truckerdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class RegistrationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    final String USER_REGISTERED = "isRegistered";
    private byte[] byteArray;
    private ParseUser user;
    private ParseFile file;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        preferences = getSharedPreferences("com.harsha.truckerdriver", MODE_PRIVATE);

        TextView text = (TextView) findViewById(R.id.log);

        text.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Start NewActivity.class
                Intent intent = new Intent(RegistrationActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        /*if(preferences.getBoolean(USER_REGISTERED,false))
        {
            loadToMainActivity();
        }*/

        /*if(ParseUser.getCurrentUser() != null)
            loadToMainActivity();*/
        mToolbar = (Toolbar) findViewById(R.id.nav_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }



    public void registerUser()
    {
        user = new ParseUser();
        user.put("name",getEditText(R.id.name_field).getText().toString());
        user.setUsername(getEditText(R.id.username).getText().toString());
        user.setPassword(getEditText(R.id.password).getText().toString());
        user.setEmail(getEditText(R.id.email).getText().toString());
        user.put("phone", getEditText(R.id.phone_number).getText().toString());
        user.put("isDriver", true);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null)
                {
                    showToast("Registration error!");
                    return;
                }
                editor = preferences.edit();
                editor.putBoolean(USER_REGISTERED, true);
                editor.apply();
                registerDriver();

            }
        });
    }

    void loadToMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    EditText getEditText(int id)
    {
        return (EditText)findViewById(id);
    }

    void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void chooseImage(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri pickedImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(pickedImage);
                byteArray = getBytes(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showToast("File not found");
                return;
            }
            catch (IOException e)
            {
                showToast("IO Exception");
                return;
            }

         /*   String[] filePath = { MediaStore.Images.Media.DATA };
            if(pickedImage == null)
            {
                showToast("Error picking file");
                return;
            }
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
            //uploadImageFile(byteArray);


            cursor.close();  */
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void register(View view)
    {
        if(byteArray == null)
        {
            showToast("Please choose valid image");
            return;
        }
        file = new ParseFile("displayPic.png", byteArray);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                    showToast("Upload failed please try again");
                else {
                    registerUser();
                }
            }
        });
    }

    void registerDriver()
    {
        final ParseObject object = new ParseObject("Driver");
        object.put("phoneNumber", getEditText(R.id.phone_number).getText().toString());
        object.put("userId", user.getObjectId());
        object.put("username", user.getUsername());
        object.put("vehicleName", getEditText(R.id.vehicle_name).getText().toString());
        object.put("vehicleNumber", getEditText(R.id.vehicle_number).getText().toString());
        object.put("accountNumber", getEditText(R.id.account_number).getText().toString());
        object.put("profilePic", file);
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!= null)
                {
                    showToast("Registration failed please try again");
                    return;
                }
                showToast("Registration successful");
                user.put("driverId", object.getObjectId());
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        loadToMainActivity();
                    }
                });

            }
        });
    }

}
