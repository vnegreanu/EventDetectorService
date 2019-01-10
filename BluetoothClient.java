package com.example.bluetoothclient;

public class BluetoothClient 

{
	private String BtMacAddress = null;
	private int TimeValidationInterval = 1;
	private int pushCounter = 0;
	
	public BluetoothClient(String MACAddress, int Interval) 
	{
		BtMacAddress = MACAddress;
		TimeValidationInterval = Interval; 
	}
	
	public String getMACAddress() 
	{
		return BtMacAddress;
	}
	
	public int getTimeInterval() 
	{
		return TimeValidationInterval;
	}
	
	public void setpushCounter(int externalCounter)
	{
		pushCounter = externalCounter;
	}
	
	public void incrementpushCounter() 
	{
		pushCounter++;
	}
	
	public int getPushCounter() 
	{
		return pushCounter;
	}

}
