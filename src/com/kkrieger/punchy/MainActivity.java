package com.kkrieger.punchy;

import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter.LengthFilter;
import android.R.integer;
import android.R.string;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.jjoe64.graphview.GraphView;

import java.io.IOException;
import java.io.WriteAbortedException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothSocket;


import org.apache.http.impl.conn.SingleClientConnManager;

public class MainActivity extends ActionBarActivity {
	
	/* Our data structures for graph. We have a GraphView,
	 * a data series for debugging,
	 * and 8 data series for acceleration/gyro in x,y,z and magnitude
	 * also, a data series for temperature in degrees celcius 
	 * we keep track of x values of all these as well */
	private GraphView graphView;
	private GraphViewSeries debugSeries;

	private GraphViewSeries gyroSeries;
	private GraphViewSeries gyroXSeries;
	private GraphViewSeries gyroYSeries;
	private GraphViewSeries gyroZSeries;

	private GraphViewSeries accelSeries;
	private GraphViewSeries accelXSeries;
	private GraphViewSeries accelYSeries;
	private GraphViewSeries accelZSeries;
	
	private GraphViewSeries temperatureSeries;
	
	private int x_debugSeries;
	private int x_gyroSeries;
	private int x_gyroXSeries;
	private int x_gyroYSeries;
	private int x_gyroZSeries;
	private int x_accelSeries;
	private int x_accelXSeries;
	private int x_accelYSeries;
	private int x_accelZSeries;
	private int x_temperatureSeries;
	
	private int punches;
	private int punches_per_second;
	private int punch_speeds[];
	private int punch_speed_average;
	
	Handler mHandler = new Handler() {
		String totalstring = "";
		List<Integer> accelx = new ArrayList<Integer>();
		@Override 
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case Bluetooth.SUCCESS_CONNECT:
				Bluetooth.connectedThread = new Bluetooth.ConnectedThread((BluetoothSocket)msg.obj);
				Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
				String s = "Successfully connected";
				Bluetooth.connectedThread.start();
				break;
			case Bluetooth.MESSAGE_READ:
				Toast.makeText(getApplicationContext(), "DATA!", Toast.LENGTH_SHORT).show();
				byte[] readBuf = (byte[]) msg.obj;
				String strIncomeString = new String(readBuf,0,msg.arg1);
				//Toast.makeText(getApplicationContext(),strIncomeString , Toast.LENGTH_SHORT).show();
				if(strIncomeString.contains("E")) {
					totalstring = totalstring.concat(strIncomeString);
					//append to our array of values until we have enough to graph
					// accelx.add(strIncomeString.) = accels.concat(strIncomeString);
				}
				if(totalstring.length() > 256) {
					// get values from it and graph
					String[] accelstrings = strIncomeString.split("E");
					for (int index = 0; index < accelstrings.length ; index++) {
						String[] xyzStrings = accelstrings[index].split(",");
						if(xyzStrings.length != 3) {
							
						} else {
							if(isIntNumber(xyzStrings[0])) //accelx.add(Integer.parseInt(xyzStrings[0]));
							debugSeries.appendData(new GraphView.GraphViewData(x_debugSeries++,Integer.parseInt(xyzStrings[0])), true,400);
						}
					}
					
				}
			/*	byte[] readBuf = (byte[]) msg.obj;
				String strIncome = new String(readBuf, 0, 25);
				
				// Check the data from the bluetooth device
				if(strIncome.indexOf('E') == 0 ){// && strIncome.indexOf('.')==2){
					strIncome = strIncome.replace("E","");
					String[] accelerations = strIncome.split(",");
					int accelints[] = new int[accelerations.length];
					for(int index = 0; index < accelerations.length; index++) {
						if(isIntNumber(accelerations[index])) {
							accelints[index] = Integer.parseInt(accelerations[index]);
						}
					}
					//Series.appendData( new GraphViewData(graph2LastXValue,Math.sqrt((float)(accelints[0]*accelints[0]+accelints[1]*accelints[1]+accelints[2]*accelints[2]))/2048.0),AutoScrollX);
					Series.appendData( new GraphViewData(graph2LastXValue,accelints[0]/2048.0),AutoScrollX);
					
					if (graph2LastXValue >= Xview && Lock == true) {
						Series.resetData(new GraphViewData[] {});
						graph2LastXValue = 0;
					} else {
						graph2LastXValue += 0.1;
					}
					if (Lock == true) {
						graphView.setViewPort(0, Xview);
					} else {
						graphView.setViewPort(graph2LastXValue-Xview, Xview);
					}
					// Refresh
					GraphView.removeView(graphView);
					GraphView.addView(graphView);
				}*/
				break;
			default:
				Toast.makeText(getApplicationContext(),"Unknown message...", Toast.LENGTH_SHORT).show();
			}
		}
		public boolean isIntNumber(String num){ 
			try{
				Integer.parseInt(num);
			}catch (NumberFormatException nfe) {
				return false;
			}
			return true;
		}
		// See if a string is a floating point number
		public boolean isFloatNumber(String num) {
			try{
				Double.parseDouble(num);
			} catch(NumberFormatException nfe) {
				return false;
			}
			return true;
		}
	};
	/* Now we have a debug timer for updating the graph periodically. And a handler for it */
	final int debugTimerFPS = 60;
	final Handler debugTimerHandler = new Handler();
	final Runnable debugTimerRunnable = new Runnable() {
		
		@Override
		public void run() {
			//debugSeries.appendData(new GraphView.GraphViewData(x_debugSeries++,Math.sin(x_debugSeries)), true,400);
			
			//debugTimerHandler.postDelayed(this, 1000/debugTimerFPS);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		/* Here we generate some an example series of data */
		debugSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
			new GraphView.GraphViewData(1, 2.0d),
			new GraphView.GraphViewData(2, 3.0d),
			new GraphView.GraphViewData(3, 2.0d),
			new GraphView.GraphViewData(4, 3.0d),
			new GraphView.GraphViewData(5, 4.0d),
			new GraphView.GraphViewData(6, -6.0d)
		});
		x_debugSeries = 6;
		
		/* Here we make a new graph view called "Punchy Graph" */
		graphView = new LineGraphView(this, "Punchy Graph");
			
		/* Here we add the data series created above to our graph view and configure graph view */
		graphView.addSeries(debugSeries);
		graphView.setScrollable(true);
		graphView.setScalable(true);
		
		/* Now we have to place the graph in our activity layout! */
		LinearLayout layout = (LinearLayout) findViewById(R.id.l_master);
		layout.addView(graphView);
		
		debugTimerHandler.postDelayed(debugTimerRunnable, 1000);
		/* Create the debug timer and task and configure them */
		//debugTimerTask = new TimerTask() {
			
		//	@Override
		//	public void run() {
		//		debugSeries.appendData(new GraphView.GraphViewData(x_debugSeries++,Math.sin(x_debugSeries)), true,400);
		//	}
		//};
		//debugTimer = new Timer();
		//debugTimer.scheduleAtFixedRate(debugTimerTask, 1000, 1000);
		
		/* Bluetooth handler */
		Bluetooth.gethandler(mHandler);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		/* We remove the debug timer handler runnable before pausing (going to background)
		 * We can simply add it back on resume with the postDelayed function call */
		debugTimerHandler.removeCallbacks(debugTimerRunnable);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		/* We resume the debug timer handler runnable in 1 second */
		debugTimerHandler.postDelayed(debugTimerRunnable, 1000);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void connectBluetooth(View view) {
		/* Create our intention to create the BluetoothStarter activity.
		 * Then, start the activity. By creating the intent, we could pass
		 * extra data to the BluetoothStarter activity if we wanted to */
		Intent intent = new Intent(this,Bluetooth.class);
		startActivity(intent);
	}
	
	public void disconnectBluetooth(View view) {
		boolean success = true;
		try {
			Bluetooth.disconnect();
		} catch (IOException e) {
			success = false;
	      	Toast.makeText(getApplicationContext(), "Failed to close device", Toast.LENGTH_SHORT).show();
		}
		if(success){
	      	Toast.makeText(getApplicationContext(), "Device closed", Toast.LENGTH_SHORT).show();
		}
	}
	
	/* Reset the total punches and the average punches per second counters. 
	 * Reset the timer for the average punches per second as well.
	 */
	public void resetPunches(View view) {
	
	}
	
	/* Reset the max estimated punch speed and the average speed 
	 * Reset the counter for the average speed value as well.
	 */
	public void resetSpeed(View view) {
	
	}
	
	/* If the graph button is toggled, we should either start or stop the graphing */
	public void toggleGraphClicked(View view) {
		// Is the toggle on?
	    boolean on = ((ToggleButton) view).isChecked();
	    if(on) {
	    	/* Keep screen on when graph is running */
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			try{
				Bluetooth.connectedThread.write("d");
				Thread.sleep(1000);
				Bluetooth.connectedThread.write("m");
			} catch (Exception e){
				
			}
	    	
	    } else {
	    	/* Don't keep screen on when graph isn't running */
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    }
	}
	
	/* Toggle button for what type of data is graphed was clicked. */
	public void toggleDataClicked(View view) {
		// Is the toggle on?
	    boolean on = ((ToggleButton) view).isChecked();
	    if(on) {
	    	this.debugSeries.appendData(new GraphView.GraphViewData(this.x_debugSeries++,0.5), true,400);
	    } else {
	    	this.debugSeries.appendData(new GraphView.GraphViewData(this.x_debugSeries++,1.5), true,400);
	    }
	}
}
