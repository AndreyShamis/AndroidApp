package com.example.com.andrey.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.R.bool;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity
{
    Button button , btnP2pConnect,btnP2pDiscover , btnProcessList ,btnClear;
    Switch goIndicator;
    public TextView text;
    Coordinates coor ;
    WifiP2pManager mManager;
    Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    Boolean enableOrientationPrint = false;
    PeerListListener myPeerListListener;
    
    private void setGuiObjects()
    {
		coor 			= new Coordinates();
	    button 			= (Button) findViewById(R.id.btnCpuInfo);
	    btnProcessList 	= (Button) findViewById(R.id.btnShowProcessList);
	    text 			= (TextView) findViewById(R.id.textView1);
	    btnP2pConnect	= (Button)  findViewById(R.id.P2PConnect);
	    btnP2pDiscover  = (Button)  findViewById(R.id.P2PDiscover);
	    btnClear  		= (Button)  findViewById(R.id.btnClear);
	    goIndicator 	= (Switch)	findViewById(R.id.goIndicator);
    }
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
	    
		try
		{
			mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		    mChannel = mManager.initialize(this, getMainLooper(), null);
		    mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		    mReceiver.SetDeviceMacAddress("CE:3A:61:B7:D7:B2".toLowerCase());
		    
		    mIntentFilter = new IntentFilter();
		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		}
		catch(Exception ex)
		{
			AppendToText("Error:" + ex.getMessage());
		}


		setContentView(R.layout.activity_main);
		this.setGuiObjects();
	    
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	text.setText(ReadCPUinfo());
            }
        });
        btnProcessList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	PrintProcessList();
            }
        });  
        btnClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	text.setText("");
            }
        });  
        btnP2pConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	try
            	{
            		
            		WifiP2pDevice dev =mReceiver.getDeviceByMac("CE:3A:61:B7:D7:B2");
                	mReceiver.config.deviceAddress = dev.deviceAddress;
                	mReceiver.config.wps.setup = WpsInfo.PBC;
                	AppendToText("Connecting to " + dev.deviceAddress);
                	mManager.connect(mChannel, mReceiver.config, new ActionListener() {
                	    @Override
                	    public void onSuccess() {
                	    	AppendToText("success logic btnP2pConnect");
                	    }

                	    @Override
                	    public void onFailure(int reason) {
                	    	AppendToText("failure logic. Reason int = " + reason);
                	    }
                	});
                	
            	}
            	catch(NullPointerException nex)
            	{
            		AppendToText("Error: " + nex.getMessage());
            	}
            	catch(Exception ex)
            	{
            		AppendToText("Error: " + ex.getMessage());
            	}
            	

            }
        });
        
        btnP2pDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    	AppendToText("success logic btnP2pDiscover");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                    	AppendToText("success logic btnP2pDiscover");
                    }
                });
            }
        });
        
        


	}

    private void PrintToText(String string)
    {
    	text.setText(string);
    }
    
    private void AppendToText(String string)
    {
    	if(mReceiver.isGO)
    	{
    		goIndicator.setChecked(true);
    	}
    	text.append("\n" + string);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void PrintProcessList()
	{
		try
		{
			text.setText("");
	        ActivityManager servMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	        List<ActivityManager.RunningAppProcessInfo> list = servMng.getRunningAppProcesses();
	        if(list != null)
	        {
	        	for(int i=0;i<list.size();++i)
	        	{
	        		text.append(list.get(i).processName + " " + list.get(i).pid + "\n");
	        	}
	        }
		}
		catch(Exception ex)
		{
			text.setText("Error:" + ex.getMessage());
		}
	}
	
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        coor.Start();
        registerReceiver(mReceiver, mIntentFilter);


    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        coor.Stop();
        unregisterReceiver(mReceiver);

    }
    
	/**
	 * ReadCPUinfo
	 * @return
	 */
	private String ReadCPUinfo()
	{
	  ProcessBuilder cmd;
	  String result="";
	
	  try{
	   String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
	   cmd = new ProcessBuilder(args);
	
	   Process process = cmd.start();
	   InputStream in = process.getInputStream();
	   byte[] re = new byte[1024];
	   while(in.read(re) != -1){
	    System.out.println(new String(re));
	    result = result + new String(re);
	   }
	   in.close();
	  } catch(IOException ex){
	   ex.printStackTrace();
	  }
	  return result;
	}
	

	class Coordinates implements SensorEventListener
	{
	    private SensorManager mSensorManager;
	    // Create a constant to convert nanoseconds to seconds.
	    private static final float NS2S = 1.0f / 1000000000.0f;
	    private final float[] deltaRotationVector = new float[4];
	    private float timestamp;
	    private Sensor mRotationVectorSensor;
	    
	    public void Stop()
	    {
	    	mSensorManager.unregisterListener(this);
	    }
	    public void Start()
	    {
	    	mSensorManager.registerListener(this, mRotationVectorSensor,SensorManager.SENSOR_ORIENTATION);
	    }
	    public Coordinates()
	    {
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			mSensorManager.registerListener(this, mRotationVectorSensor,SensorManager.SENSOR_ORIENTATION);
	    }
	    
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			 float axisX = 0 ,axisY=0,axisZ=0;
			 axisX = event.values[0];
			 axisY = event.values[1];
			 axisZ = event.values[2];
			 if(enableOrientationPrint)
			 {
				 text.setText(
						 "\nAxis X: " + (double)Math.round(axisX*100)/100 +
						 "\nAxis Y: " + (double)Math.round(axisY*100)/100 +
						 "\nAxis Z: " + (double)Math.round(axisZ*100)/100
						 );
			 }
			 return;
			 /*
			 // This timestep's delta rotation to be multiplied by the current rotation
			 // after computing it from the gyro sample data.
			 if (timestamp != 0)
			 {
				 final float dT = (event.timestamp - timestamp) * NS2S;
				 // Axis of the rotation sample, not normalized yet.
				 axisX = event.values[0];
				 axisY = event.values[1];
				 axisZ = event.values[2];

				 // Calculate the angular speed of the sample
				 float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

				 // Normalize the rotation vector if it's big enough to get the axis
				 // (that is, EPSILON should represent your maximum allowable margin of error)
				 if (omegaMagnitude > 0.000000000001)
				 {
					 axisX /= omegaMagnitude;
					 axisY /= omegaMagnitude;
					 axisZ /= omegaMagnitude;
				 }

				// Integrate around this axis with the angular speed by the timestep
				// in order to get a delta rotation from this sample over the timestep
				// We will convert this axis-angle representation of the delta rotation
				// into a quaternion before turning it into the rotation matrix.
				float thetaOverTwo = omegaMagnitude * dT / 2.0f;
				float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
				float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
				deltaRotationVector[0] = sinThetaOverTwo * axisX;
				deltaRotationVector[1] = sinThetaOverTwo * axisY;
				deltaRotationVector[2] = sinThetaOverTwo * axisZ;
				deltaRotationVector[3] = cosThetaOverTwo;
			 }
			 
			 timestamp = event.timestamp;
			 float[] deltaRotationMatrix = new float[9];
			 SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
			    // User code should concatenate the delta rotation we computed with the current rotation
			    // in order to get the updated rotation.
			    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
			 text.setText("\n0: " + deltaRotationVector[0] + 
					 "\n1: " + deltaRotationVector[1] + 
					 "\n2: " + deltaRotationVector[2] + 
					 "\n3: " + deltaRotationVector[3] +
					 "\nAxis X: " + axisX +
					 "\nAxis Y: " + axisY +
					 "\nAxis Z: " + axisZ +
					 "\nDR 1 :: " + deltaRotationMatrix[0] + " " + deltaRotationMatrix[1] + " " + deltaRotationMatrix[2] +
					 "\nDR 2 :: " + deltaRotationMatrix[3] + " " + deltaRotationMatrix[4] + " " + deltaRotationMatrix[5] +
	 				 "\nDR 3 :: " + deltaRotationMatrix[6] + " " + deltaRotationMatrix[7] + " " + deltaRotationMatrix[8]);
			 text.append(event.toString());*/
			
		}
		

	}
}
