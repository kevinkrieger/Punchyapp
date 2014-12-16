package com.kkrieger.punchy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;



public class Bluetooth extends Activity implements OnItemClickListener{
	public static void disconnect() throws IOException{
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
	}
	public static void gethandler(Handler handler) {
		mHandler = handler;
	}
	static Handler mHandler = new Handler();

	static ConnectedThread connectedThread;
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Needs to be this value...
	
	protected static final int SUCCESS_CONNECT=0;
	protected static final int MESSAGE_READ=1;
	ListView listView;
	ArrayAdapter<String> listAdapter;
	static BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	IntentFilter filter;
	BroadcastReceiver receiver;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.kkrieger.punchy.R.layout.activity_bluetooth_starter);
		init();
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No BT detected", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			if(!btAdapter.isEnabled()) {
				turnOnBT();
			}
			getPairedDevices();
			startDiscover();
			//finish();
		}
	}
	
	private void getPairedDevices() {
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}
	
	private void turnOnBT() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent,1);
	}
	
	private void startDiscover() {
		devicesArray = btAdapter.getBondedDevices();
		if(devicesArray.size() > 0) {
			for (BluetoothDevice device:devicesArray){
				pairedDevices.add(device.getName());
			}
		}
		
	}
	
	private void init(){
		listView = (ListView)findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
		listView.setAdapter(listAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = new ArrayList<String>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		devices = new ArrayList<BluetoothDevice>();
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
					for(int a = 0; a<pairedDevices.size(); a++) {
						if(device.getName().equals(pairedDevices.get(a))) {
							s = "(Paired)";
							break;
						}
					}
					listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					Toast.makeText(getApplicationContext(),"BT Discovery started!", Toast.LENGTH_SHORT).show();
					
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					Toast.makeText(getApplicationContext(),"BT Discovery finished!", Toast.LENGTH_SHORT).show();
				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					Toast.makeText(getApplicationContext(),"BT state changed!", Toast.LENGTH_SHORT).show();
					if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
						turnOnBT();
					}
				} else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
						Toast.makeText(getApplicationContext(),"BT connection state changed!", Toast.LENGTH_SHORT).show();
						//if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
						//	turnOnBT();
						//}
				}
			}
		};
		
		registerReceiver(receiver,filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver,filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver,filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver,filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		registerReceiver(receiver,filter);
	}
	
	 
	/* A class to connect to a bluetooth device. */
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	       
				Toast.makeText(getApplicationContext(),"BT Connecting", Toast.LENGTH_SHORT).show();
			
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { Toast.makeText(getApplicationContext(),"BT IOException caught", Toast.LENGTH_SHORT).show(); }
	        mmSocket = tmp;
	       
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	    	btAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	        	//Toast.makeText(getApplicationContext(),"BT connect socket",Toast.LENGTH_SHORT).show();
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	     //   Toast.makeText(getApplicationContext(),"BT connect success",Toast.LENGTH_SHORT).show();
            
	        
	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);
	        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
	    }
	 
	    /* Will cancel an in-progress connection, and close the socket */
		public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	

	/* This thread is for bluetooth connected device
	 * It knows about the socket, input stream and output streams to the device.
	 * You can write to the device using write(String income)
	 * and shutdown the connection using cancel()  */
static class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
 
    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 

	public void run() {
        byte[] buffer;  // buffer store for the stream
        int bytes; // bytes returned from read()
 
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
            	try{
            		sleep(30);
            	} catch(InterruptedException e) {
            		e.printStackTrace();
            	}
            	buffer = new byte[1024];
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }
 
    /* Call this from the main activity to send data to the remote device */
    public void write(String income) {
        try {
            mmOutStream.write(income.getBytes());
            try {
            	Thread.sleep(20);
            } catch (InterruptedException e){
            	e.printStackTrace();
            }
        } catch (IOException e) { }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() throws IOException {
        mmSocket.close();
    }
} /* End of ConnectedThread */



@Override 
protected void onPause() {
	Toast.makeText(getApplicationContext(),"Paused!", Toast.LENGTH_SHORT).show();
	super.onPause();
		unregisterReceiver(receiver);
}

protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if(resultCode == RESULT_CANCELED) {
		Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
		finish();
	}
}

@Override
public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	if (btAdapter.isDiscovering()) {
		Toast.makeText(getApplicationContext(),"Cancelling discovery...", Toast.LENGTH_SHORT).show();
		btAdapter.cancelDiscovery();
	}
	if (listAdapter.getItem(arg2).contains("(Paired)")) {
		Toast.makeText(getApplicationContext(),"Connecting to "+listAdapter.getItem(arg2)+"...", Toast.LENGTH_SHORT).show();
		BluetoothDevice selectedDevice = devices.get(arg2);
		ConnectThread connect = new ConnectThread(selectedDevice);
		connect.start();
		finish();
	} else {
		Toast.makeText(getApplicationContext(),"Device is not paired", Toast.LENGTH_SHORT).show();
	}

}
	
}

//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.UUID;
//
//import android.support.v7.app.ActionBarActivity;
//import android.R.anim;
//import android.R.integer;
//import android.app.DownloadManager.Request;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//import android.widget.Toast;
//
//public class BluetoothStarter extends ActionBarActivity implements OnItemClickListener {
//	public static void disconnect(){
//		if (connectedThread != null) {
//			connectedThread.cancel();
//			connectedThread = null;
//		}
//	}
//	public static void gethandler(Handler handler) {
//		mHandler = handler;
//	}
//	static Handler mHandler = new Handler();
//
//	/* Thread for connecting bluetooth device */
//	static ConnectedThread connectedThread;
//	protected static final int SUCCESS_CONNECT=0;
//	protected static final int MESSAGE_READ=1;
//	
//	/* Bluetooth adapter, array list for devices, paired devices set, paired devices string list and a list 
//	 * view for displaying bluetooth devices */
//	static BluetoothAdapter mBluetoothAdapter;
//	ArrayList<BluetoothDevice> btDevicesArrayList;
//	ArrayAdapter<String> listAdapter; 
//	Set<BluetoothDevice> pairedDevices ;
//	ArrayList<String> pairedDevicesArrayList;
//	public static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//	ListView btListView;
//	
//	/* Intent filter for finding bluetooth devices
//	 * Broadcast receiver for bluetooth actions (discovery started, finished,
//	 * state changed, connection state changed...) */
//	IntentFilter btFilterIntentFilter;
//	BroadcastReceiver btBroadcastReceiver;
//	
//	/* Toast object and duration */
//	int toastDuration = Toast.LENGTH_SHORT;
//	Toast toast;
//	/* Context for starting a 'toast' message to user */
//	Context context;
//	/* Strings used in 'toast' messages */
//	CharSequence noBtCharSequence = "Bluetooth unavailable!";
//	CharSequence btExistsCharSequence = "Bluetooth available!";
//	CharSequence btRequestFailCharSequence = "Bluetooth not turned on!";
//	CharSequence btRequestSuccessCharSequence = "Bluetooth on!";
//	CharSequence unknownRequestCharSequence = "Unknown request...";
//	CharSequence btDiscoveryStarted = "Bluetooth Discovery started!";
//	CharSequence btDiscoverFinished = "Bluetooth Discovery finished!";
//	CharSequence btStateChanged = "Bluetooth state changed!";
//	CharSequence btConnectionStateChanged = "Bluetooth connection state changed!";
//	
//	/* When we want to enable bluetooth we have to send the bluetooth starting activity an integer
//	 * so we can see if it succeeds */
//	private final static int REQUEST_ENABLE_BT = 1;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_bluetooth_starter);
//		/* Get the intent that started this activity. It may contain extra data. */
//		Intent intent = getIntent();
//		
//		
//		
//		btListView = (ListView)findViewById(R.id.listView);
//		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
//		btListView.setOnItemClickListener(this);
//		btListView.setAdapter(listAdapter);
//		pairedDevicesArrayList = new ArrayList<String>();
//		/* Initialize the filter and broadcast receiver for bluetooth actions */
//		btFilterIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//		btDevicesArrayList = new ArrayList<BluetoothDevice>();		
//		
//		btBroadcastReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				String action = intent.getAction();
//				if(BluetoothDevice.ACTION_FOUND.equals(action)) {
//					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//					btDevicesArrayList.add(device);
//					String s = "";
//					for(int a = 0; a<pairedDevicesArrayList.size(); a++) {
//						if(device.getName().equals(pairedDevicesArrayList.get(a))) {
//							s = "(Paired)";
//							break;
//						}
//					}
//					listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
//				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//					toast = Toast.makeText(getApplicationContext(),btDiscoveryStarted, toastDuration);
//					toast.show();
//				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//					toast = Toast.makeText(getApplicationContext(),btDiscoverFinished, toastDuration);
//					toast.show();
//				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//					toast = Toast.makeText(getApplicationContext(), btStateChanged, toastDuration);
//					toast.show();
//					if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
//						turnOnBT();
//					}
//				} else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
//						toast = Toast.makeText(context, btConnectionStateChanged, toastDuration);
//						toast.show();
//						//if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
//						//	turnOnBT();
//						//}
//				}
//			}
//			
//			
//			
//		};
//		
//		registerReceiver(btBroadcastReceiver,btFilterIntentFilter);
//		btFilterIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//		registerReceiver(btBroadcastReceiver,btFilterIntentFilter);
//		btFilterIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//		registerReceiver(btBroadcastReceiver,btFilterIntentFilter);
//		btFilterIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//		registerReceiver(btBroadcastReceiver,btFilterIntentFilter);
//		btFilterIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
//		registerReceiver(btBroadcastReceiver,btFilterIntentFilter);
//		
//		
//		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//		turnOnBT();
//		mBluetoothAdapter.cancelDiscovery();
//		mBluetoothAdapter.startDiscovery();
//		queryPairedDevices();
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.bluetooth_starter, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//	
//	
//	
//	/* Find out which devices are paired */
//	private void queryPairedDevices() {
//
//		pairedDevices = mBluetoothAdapter.getBondedDevices();
//		if(pairedDevices.size() > 0) {
//			//pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.lv_pairedDevices);
//			for (BluetoothDevice device : pairedDevices) {
//				pairedDevicesArrayList.add(device.getName() + "\n" + device.getAddress());
//			}
//			//ListView newDevicesListView = (ListView)findViewById(R.id.lv_pairedDevices);
//			//newDevicesListView.setAdapter(pairedDevicesArrayAdapter);
//		}
//	}
//	
//	private void discoverDevices() {
//		
//	}
//	
//
//	private void turnOnBT() {
//		/* Find out if bluetooth is on, and turn on if not */
//		
//		if(mBluetoothAdapter == null) {
//			/* No bluetooth... so let them know */
//			//context = getApplicationContext();
//			toast = Toast.makeText(getApplicationContext(), noBtCharSequence, toastDuration);
//			toast.show();
//			finish();
//		} 
//		//context = getApplicationContext();
//		toast = Toast.makeText(getApplicationContext(), btExistsCharSequence, toastDuration);
//		toast.show();
//		if(!mBluetoothAdapter.isEnabled()) {
//			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//		}
//	}
//	
//
//	private class ConnectThread extends Thread {
//	    private final BluetoothSocket mmSocket;
//	    private final BluetoothDevice mmDevice;
//	 
//	    public ConnectThread(BluetoothDevice device) {
//	        // Use a temporary object that is later assigned to mmSocket,
//	        // because mmSocket is final
//	        BluetoothSocket tmp = null;
//	        mmDevice = device;
//	       
//				Toast.makeText(getApplicationContext(),"BT Connecting", Toast.LENGTH_SHORT).show();
//			
//	        // Get a BluetoothSocket to connect with the given BluetoothDevice
//	        try {
//	            // MY_UUID is the app's UUID string, also used by the server code
//	            tmp = device.createRfcommSocketToServiceRecord(BT_UUID);
//	        } catch (IOException e) { Toast.makeText(getApplicationContext(),"BT IOException caught", Toast.LENGTH_SHORT).show(); }
//	        mmSocket = tmp;
//	       
//	    }
//	 
//	    public void run() {
//	        // Cancel discovery because it will slow down the connection
//	    	mBluetoothAdapter.cancelDiscovery();
//	 
//	        try {
//	            // Connect the device through the socket. This will block
//	            // until it succeeds or throws an exception
//	        	//Toast.makeText(getApplicationContext(),"BT connect socket",Toast.LENGTH_SHORT).show();
//	            mmSocket.connect();
//	        } catch (IOException connectException) {
//	            // Unable to connect; close the socket and get out
//	            try {
//	                mmSocket.close();
//	            } catch (IOException closeException) { }
//	            return;
//	        }
//	     //   Toast.makeText(getApplicationContext(),"BT connect success",Toast.LENGTH_SHORT).show();
//            
//	        
//	        // Do work to manage the connection (in a separate thread)
//	        //manageConnectedSocket(mmSocket);
//	        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
//	    }
//	 
//	    /** Will cancel an in-progress connection, and close the socket */
//	    public void cancel() {
//	        try {
//	            mmSocket.close();
//	        } catch (IOException e) { }
//	    }
//	}
//	
//
//static class ConnectedThread extends Thread {
//    private final BluetoothSocket mmSocket;
//    private final InputStream mmInStream;
//    private final OutputStream mmOutStream;
// 
//    public ConnectedThread(BluetoothSocket socket) {
//        mmSocket = socket;
//        InputStream tmpIn = null;
//        OutputStream tmpOut = null;
// 
//        // Get the input and output streams, using temp objects because
//        // member streams are final
//        try {
//            tmpIn = socket.getInputStream();
//            tmpOut = socket.getOutputStream();
//        } catch (IOException e) { }
// 
//        mmInStream = tmpIn;
//        mmOutStream = tmpOut;
//    }
// 
//    public void run() {
//        byte[] buffer;  // buffer store for the stream
//        int bytes; // bytes returned from read()
// 
//        // Keep listening to the InputStream until an exception occurs
//        while (true) {
//            try {
//            	try{
//            		sleep(30);
//            	} catch(InterruptedException e) {
//            		e.printStackTrace();
//            	}
//            	buffer = new byte[1024];
//                // Read from the InputStream
//                bytes = mmInStream.read(buffer);
//                // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//            } catch (IOException e) {
//                break;
//            }
//        }
//    }
// 
//    /* Call this from the main activity to send data to the remote device */
//    public void write(String income) {
//        try {
//            mmOutStream.write(income.getBytes());
//            try {
//            	Thread.sleep(20);
//            } catch (InterruptedException e){
//            	e.printStackTrace();
//            }
//        } catch (IOException e) { }
//    }
// 
//    /* Call this from the main activity to shutdown the connection */
//    public void cancel() {
//        try {
//            mmSocket.close();
//        } catch (IOException e) { }
//    }
//}
//
//@Override 
//protected void onPause() {
//	Toast.makeText(getApplicationContext(),"Paused!", Toast.LENGTH_SHORT).show();
//	super.onPause();
//	unregisterReceiver(btBroadcastReceiver);
//	
//	
//}
//
//protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	super.onActivityResult(requestCode, resultCode, data);
//	if(requestCode == REQUEST_ENABLE_BT) {
//		if(resultCode != RESULT_OK) {
//			/* Request to turn on bluetooth failed... */
//			//context = getApplicationContext();
//			toast = Toast.makeText(getApplicationContext(), btRequestFailCharSequence, toastDuration);
//			toast.show();
//			finish();
//		} 
//		/* Request to turn on bluetooth succeeded... */
//		//context = getApplicationContext();
//		toast = Toast.makeText(getApplicationContext(), btRequestSuccessCharSequence, toastDuration);
//		toast.show();
//		/* Now query paired devices, and give option to discover devices */
//		queryPairedDevices();
//		discoverDevices();
//		finish();
//	} else {
//		/* Unknown request... */
//	//	context = getApplicationContext();
//		toast = Toast.makeText(getApplicationContext(), unknownRequestCharSequence, toastDuration);
//		toast.show();
//		finish();
//	}
//	
////	if(resultCode == RESULT_CANCELED) {
//	//	Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
////		finish();
////	}
//}
//
//@Override
//public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//	if (mBluetoothAdapter.isDiscovering()) {
//		Toast.makeText(getApplicationContext(),"Cancelling discovery...", Toast.LENGTH_SHORT).show();
//		mBluetoothAdapter.cancelDiscovery();
//	}
//	if (listAdapter.getItem(arg2).contains("(Paired)")) {
//		Toast.makeText(getApplicationContext(),"Connecting to "+listAdapter.getItem(arg2)+"...", Toast.LENGTH_SHORT).show();
//		BluetoothDevice selectedDevice = btDevicesArrayList.get(arg2);
//		ConnectThread connect = new ConnectThread(selectedDevice);
//		connect.start();
//	} else {
//		Toast.makeText(getApplicationContext(),"Device is not paired", Toast.LENGTH_SHORT).show();
//	}
//
//}
//	
//}
//
//
