package com.example.bluetoothclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {

	TextView out;
	private final int BUFFER_SIZE = 8;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;
	private int bytes = 0;
	private int curLength = 0;
	private byte[] inputBuffer = new byte[BUFFER_SIZE];
	private int commandWord = 0;
	private final byte GET_DATA_RECORD = 0x31;
	private final byte COMMAND_DELETE_RECORDS = 0x32;
	private final int COMMAND_WORD_STOP = 0x0008;
	private final int PUSH_TIME_MASK = 0x7FFF;
	private final int PUSH_TIME_MULTIPLIER = 0x08;
	private int pushTime = 0;
	private int validationTime = 1; //ms
	private final String CLIENT_TAG = "BluetoothClient";

	 
	private static final int REQUEST_ENABLE_BT = 1;


	// Well known SPP UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// server's MAC address
	private static final BT_1_MAC_ADDRESS = "00:12:6F:38:4C:59";
	private static final BT_2_MAC_ADDRESS = "00:12:6F:25:5E:0A";
	
	private final static long TIMEOUT = 30000;
	private final static long TICK = 1000;
	
	private CountDownTimer timer;
	private BluetoothClient bt1;
	private BluetoothClient bt2;
	
	//private PreferencesManager mPreferencesManager;
	 
	String utcTime;
	static final String DATEFORMAT = "dd-MMMM-yyyy HH:mm:ss";
	
 	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// also set alarm
        
        setRecurringAlarm();
		
		//start the service in background
		//startService(new Intent(this, BT2SMSService.class));
		
		out  = (TextView) findViewById(R.id.out);
		out.append("\n...In onCreate()...");
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		
		CheckBTState();
		
		//creating BluetoothClient objects
		bt1 = new BluetoothClient(BT_1_MAC_ADDRESS,1);
		bt2 = new BluetoothClient(BT_2_MAC_ADDRESS,20000);
		
		//get from shared prefs
		
		
		//declare timer 
		timer = new CountDownTimer(TIMEOUT, TICK) {
			@Override
			public void onTick(long l) {
			}

			@Override
			public void onFinish() {
				
				InitBTConnection(bt1);
				BTSendReceive(bt1);
				InitBTConnection(bt2);
				BTSendReceive(bt2);
								
				
				
				if((bt1.getPushCounter()!=0) || (bt2.getPushCounter()!=0)) 
				{
					sendSMSMessage(bt1.getPushCounter(), bt2.getPushCounter());

					out.append("\n SW1:" + Integer.toHexString(bt1.getPushCounter()));
					out.append("\n SW2:" + Integer.toHexString(bt2.getPushCounter()));
					
					//add persistancy here....
					
					//reset counters for both objects
					bt1.setpushCounter(0);
					bt2.setpushCounter(0);
					
					 
				}
				else 
				{
					out.append("\nNo record to delete!");
				}
				
				timer.start();
			}
		};
		timer.start();
	}
		
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		
		      
	} 
	
    @Override
    public void onStop() {
      super.onStop();
      
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
     
    }
	
    
  //user defined functions
  	 private void CheckBTState() {
  	        // Check for Bluetooth support and then check to make sure it is turned on

  	        if(btAdapter==null) { 
  	        //  AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
  	        } else {
  	          if (btAdapter.isEnabled()) {
  	            out.append("\n...Bluetooth is enabled...");
  	          } else {
  	            //Prompt user to turn on Bluetooth
  	            Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
  	            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
  	            //btAdapter.enable();
  	        	
  	          }
  	        }
  	      }
  	    
  	    public static String byteArrayToHex(byte[] a) {
  	    	   StringBuilder sb = new StringBuilder(a.length * 2);
  	    	   for(byte b: a)
  	    	      sb.append(String.format("%02x", b & 0xff));
  	    	   return sb.toString();
  	    	}
  		
  	    private void resetBTConnection() {
  	        if (inStream != null) {
  	                try {inStream.close();} catch (Exception e) {}
  	                inStream = null;
  	        }

  	        if (outStream != null) {
  	                try {outStream.close();} catch (Exception e) {}
  	                outStream = null;
  	        }

  	        if (btSocket != null) {
  	                try {btSocket.close();} catch (Exception e) {}
  	                btSocket = null;
  	        }

  	}
  	    private void sendSMSMessage(int noPush1,int noPush2) {
  	        Log.i("Send SMS", "");
  	        SimpleDateFormat dateFormatLocal = new SimpleDateFormat(DATEFORMAT);
  	        dateFormatLocal.setTimeZone(TimeZone.getDefault());
  	        utcTime = dateFormatLocal.format(new Date());

  	        try {
  	           SmsManager smsManager = SmsManager.getDefault();
  	           smsManager.sendTextMessage("0000", null, "SW1: " + noPush1 + " " + "SW2: " + noPush2 + " " + " at " + utcTime, null, null);
  	           out.append("\n SMS sent!");
  	           Toast.makeText(getApplicationContext(), "SMS sent.",
  	           Toast.LENGTH_LONG).show();
  	        } catch (Exception e) {
  	           Toast.makeText(getApplicationContext(),
  	           "SMS failed, please try again.",
  	           Toast.LENGTH_LONG).show();
  	           e.printStackTrace();
  	        }
  	     }
  	    
  	    private void InitBTConnection(BluetoothClient obj) 
  	    {	out.setText(null);
  	    	out.append("\n...In InitBTConnection...\n...Attempting client connect...");
  			String msg = "\n...In InitBTConnection...\n...Attempting client connect...";
  			Log.d(CLIENT_TAG,msg);
  			Toast.makeText(getApplicationContext(),
  	       	         msg,
  	       	         Toast.LENGTH_LONG).show();

  			 // Set up a pointer to the remote node using it's address.
  		    //BluetoothDevice device = btAdapter.getRemoteDevice(address);
  			
  			BluetoothDevice device = btAdapter.getRemoteDevice(obj.getMACAddress());
  		    
  		    try {
  		        btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
  		      } catch (IOException e) {
  		    	 out.append("\n...In InitBTConnection() and unable to create BT socket!");
  		    	 String msg2 = "\n...In InitBTConnection() and unable to create socket!" + e.getMessage();
  		    	Toast.makeText(getApplicationContext(),
		        	         msg2,
		        	         Toast.LENGTH_LONG).show();
  		      }
  		   
  		      // Discovery is resource intensive.  Make sure it isn't going on
  		      // when you attempt to connect and pass your message.
  		      btAdapter.cancelDiscovery();
  		      
  		      // Establish the connection.  This will block until it connects.
  		      try {
  		    	  btSocket.connect();
  		    	  out.append("\n...Connection established and data link opened...");
  		    	  String msg1 = "\n...Connection established and data link opened...";
  		    	  Log.d(CLIENT_TAG,msg1);
  		    	  Toast.makeText(getApplicationContext(),
  		        	         msg1,
  		        	         Toast.LENGTH_LONG).show();
  		    	  
  		      } catch (IOException e) {
  		    	  try {
  		    		  btSocket.close();
  		    		out.append("\n...In InitBTConnection() close socket during connection failure");
 		    		String msg21 = "\n...In InitBTConnection() close socket during connection failure" ;
 		 	    	Toast.makeText(getApplicationContext(),
 		 	       	         msg21,
 		 	       	         Toast.LENGTH_LONG).show();
  		    	  } catch (IOException e2) {
  		    		out.append("\n...In InitBTConnection() and unable to close socket during connection failure");
  		    		 String msg2 = "\n...In InitBTConnection() and unable to close socket during connection failure" + e2.getMessage();
  		 	    	Toast.makeText(getApplicationContext(),
  		 	       	         msg2,
  		 	       	         Toast.LENGTH_LONG).show();
  		    		 
  		    	  }
  		      }

  	    }
  	    
  	    private void BTSendReceive(BluetoothClient obj) 
  	    {
  	    	try {
  		          outStream = btSocket.getOutputStream();
  		          String msg4 = "In BTSendReceive() and output stream creation success:";
		          Log.d(CLIENT_TAG,msg4);
		          Toast.makeText(getApplicationContext(),
		        	         msg4,
		        	         Toast.LENGTH_LONG).show();
  		        } catch (IOException e) {
  		          String msg4 = "In BTSendReceive() and output stream creation failed:" + e.getMessage();
  		          Log.d(CLIENT_TAG,msg4);
  		          Toast.makeText(getApplicationContext(),
  		        	         msg4,
  		        	         Toast.LENGTH_LONG).show();
  		        }
  		      
  		      //fill the control byte to the server
  		       byte[] controlMessageBuffer = {GET_DATA_RECORD};
  		       	  
  		      try {
  		    	  inStream = btSocket.getInputStream();
  		      } catch (IOException e) {
  		    	  String msg7 = "In BTSendReceive() and input stream creation failed:" + e.getMessage();
  		    	  Log.d(CLIENT_TAG,msg7);
  		    	  Toast.makeText(getApplicationContext(),
  		    			  msg7,
  		    			  Toast.LENGTH_LONG).show();
  		      }
  		    	  
  		  
  		      //read section
  		      try {
  		    	  do{ 
  		    	      //write section
  		    	      try {
  		    	    	  //send control byte to server
  		    	          outStream.write(controlMessageBuffer);
  		    	      //    String controlbufstr = "" + controlMessageBuffer[0] + "";
  		    	          //send what is already in buffer
  		    	          outStream.flush();
  		    	          out.append("\nSent command word :" + "GET_EVENT_RECORDS");
  		    	          
  		    	          
  		    	        } catch (IOException e) {
  		    	          String msg5 = "In BTSendReceive() and an exception occurred during write : " + e.getMessage();
  		    	        	                
  		    	        }
  		    	      
  		    	      	curLength = 0;
  		    	      
  		    	  		do 
  		    	  		{
  		    	  			// Read from the InputStream 
  		    	  			bytes = inStream.read(inputBuffer, curLength, inputBuffer.length - curLength);

  		    	  			// still reading update current length 
  		    	  			curLength += bytes;

  		    	  			

  		    	  		}while(bytes >= 0 && curLength < 8);
  		    	  		 
  		    	  		
  		    	  		commandWord = inputBuffer[1] << 8 | inputBuffer[0];
  		  	    	  
  			    	  out.append("\n Received buffer is :" + byteArrayToHex(inputBuffer));
  			    	  out.append("\n Received command word is :" + Integer.toHexString(commandWord));

  			    	  //state of art in "programming" by HP
  			    	  if (COMMAND_WORD_STOP != commandWord) 
  			    	  {
  			    		  pushTime = inputBuffer[3] << 8 | inputBuffer[2];
  			    		  
  			    		  pushTime &= PUSH_TIME_MASK;
  			    		  pushTime *= PUSH_TIME_MULTIPLIER;
  			    		  
  			    		 // if(pushTime > validationTime) 
  			    		  if(pushTime > obj.getTimeInterval())
  			    		  {
  			    			obj.incrementpushCounter();
  			    		  }
  			    		  
  			    		 
  			    	  }
  			    	  else 
  			    	  {
  			    		  break;
  			    	  }
  			    	  
  		    		  }while(true);
  		    		  
  		    	 

  		    	  
  		    	  //erase section
  		    	 
  	  	      try {
  	  	    	  byte [] deleteMessageBuffer ={COMMAND_DELETE_RECORDS};
  	  	    	  //send control byte to server
  	  	          outStream.write(deleteMessageBuffer);
  	  	          //send what is already in buffer
  	  	          outStream.flush();
  	  	          
  	  	          //delay 200 ms
  	  	          SystemClock.sleep(200);
  	  	          
  	  	          out.append("\nSent delete command :" + "DELETE_RECORDS");
  	  	          out.append("\nDeletion of push number takes place!");
  	  	          
  	  	            
  	  	        } catch (IOException e) {
  	  	          String msg20 = "In onSendReceive() and an exception occurred during write: " + e.getMessage();   	                
  	  	        }
  		    	

  		      } catch (IOException e) {

  		    	  String msg8 = "In onResume() and an exception occurred during read: " + e.getMessage();
  		    	  Toast.makeText(getApplicationContext(),
  		    			  msg8,
  		    			  Toast.LENGTH_LONG).show();
  		      }
  		
  		      resetBTConnection();  
  	    }
  	
  //function to set the SMS sending alarm	    
  	  private void setRecurringAlarm() {
  		 

  	    Calendar updateTime = Calendar.getInstance();
  	    updateTime.setTimeZone(TimeZone.getDefault());
  	    updateTime.set(Calendar.HOUR_OF_DAY, 13);
  	    updateTime.set(Calendar.MINUTE, 35);
  	     
  	    Intent activate = new Intent("com.example.bluetoothclient.START_ALARM");
  	    PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, activate, 0);
  	    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
  	    alarm.setRepeating(AlarmManager.RTC_WAKEUP,updateTime.getTimeInMillis(),AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
  	}
   
    
}
