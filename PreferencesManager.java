package com.example.bluetoothclient;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    private final static String SHARED_PREFS_NAME = "com.example.bluetoothclient.prefs";

    private final static String BT1_COUNTER = "bt1_counter";
    private final static String BT2_COUNTER = "bt2_counter";



    private static PreferencesManager instance = null;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    private PreferencesManager() {
        sharedPreferences = BluetoothApplication.getInstance().getApplicationContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
    }

    /**
     * returns the instance of the PreferencesManager
     * 
     * @param context
     *            - the context
     * @return - the instance of the PreferencesManager singleton
     */
    public static synchronized PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }
    
    public int getBt1Counter() {
        return sharedPreferences.getInt(BT1_COUNTER,-1);
    }

    public void setBt1Counter(int bt1_counter) {
        preferencesEditor.putInt(BT1_COUNTER, bt1_counter);
        preferencesEditor.commit();
    }
    
    public int getBt2Counter() {
        return sharedPreferences.getInt(BT2_COUNTER,-1);
    }

    public void setBt2Counter(int bt2_counter) {
        preferencesEditor.putInt(BT2_COUNTER, bt2_counter);
        preferencesEditor.commit();
    }

    public void clearCredeantials() 
    {
        preferencesEditor.remove(BT1_COUNTER);
        preferencesEditor.remove(BT2_COUNTER);
        preferencesEditor.commit();
    }



}

