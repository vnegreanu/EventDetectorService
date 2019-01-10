package com.example.bluetoothclient;

import android.app.Application;


public class BluetoothApplication extends Application 
{

	    // application is already a singleton
	    private static BluetoothApplication instance;

	    public static BluetoothApplication getInstance() {
	        return instance;
	    }

	    @Override
	    public void onCreate() {
	        super.onCreate();
	        instance = this;
	        
	    }

	}


