package com.harsha.truckerdriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;

/**
 * Created by Admin on 11/11/2017.
 */

public class Utilities {

    public static String refineAddress(Address address){
        String result = "";
        if(address.getAddressLine(0) != null) {
            result += address.getAddressLine(0) + ", ";
        }
        if(address.getAddressLine(1) != null) {
            result += address.getAddressLine(1) + ", ";
        }
        if(address.getAddressLine(2) != null) {
            // result += address.getAddressLine(2);
        }

        return result;
    }

    public static void  myToast(String message)
    {
    }

    public static void loadActivity(Context context , Class myClass )
    {
        Intent intent = new Intent(context, myClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(intent);
    }

    public static void loadActivityAndFinish(Context context , Class myClass )
    {
        Intent intent = new Intent(context, myClass);
        context.startActivity(intent);
        Activity activity = (Activity) context;
        activity.finish();
    }
}
