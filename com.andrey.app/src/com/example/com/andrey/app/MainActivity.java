package com.example.com.andrey.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener
{
	
    Button button ;
    Button btnProcessList ;
    TextView text;
    private SensorManager mSensorManager;
    private Sensor mSensor;
 // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    protected void onResume() {

    	super.onResume();
    	try
    	{
        	List<Sensor> typedSensors =		mSensorManager.getSensorList(Sensor.TYPE_LIGHT);
        	// also: TYPE_ALL
        	if (typedSensors == null || typedSensors.size() <= 0) 
        		text.setText("Error");
        	
        	mSensorManager.registerListener(this, typedSensors.get(0),SensorManager.SENSOR_DELAY_GAME);
        	// Rates: SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME,
        	// SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI	
    	}
    	catch(Exception ex)
    	{
    		
    	}

    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		
	    button = (Button) findViewById(R.id.btnCpuInfo);
	    btnProcessList = (Button) findViewById(R.id.btnShowProcessList);
	    text = (TextView) findViewById(R.id.textView1);
	    
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

	}
	
	@SuppressLint("NewApi")
	public void onSensorChanged(SensorEvent event) 
	{
		 float axisX = 0 ,axisY=0,axisZ=0;
		 
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
		 text.append(event.toString());
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

 /*       if(list != null){
         for(int i=0;i<list.size();++i){
          //if("com.android.email".matches(list.get(i).processName)){
           //int pid = android.os.Process.getUidForName("com.android.email");
          //       android.os.Process.killProcess(pid);
         // }else{
           text.append(list.get(i).processName + "\n");
         // }
         }
        }*/
	}
	
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

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
