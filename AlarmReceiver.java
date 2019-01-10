package com.example.bluetoothclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	 
    private static final String DEBUG_TAG = "AlarmReceiver";
	private final static String TEL_NUMBER = "0000"; //enter a valid number here in international format +<country_code><number>
 
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        Log.d(DEBUG_TAG, "Recurring alarm; requesting download service.");
        
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(TEL_NUMBER, null, "Alarm test!", null, null);
        
    }
 
}
