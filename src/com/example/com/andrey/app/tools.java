package com.example.com.andrey.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.ActivityManager;

public class tools {
	
	public String getProcessList( ActivityManager servMng)
	{
		String ret = "";
		try
		{
	        List<ActivityManager.RunningAppProcessInfo> list = servMng.getRunningAppProcesses();
	        if(list != null)
	        {
	        	for(int i=0;i<list.size();++i)
	        	{
	        		ret +=(list.get(i).processName + " " + list.get(i).pid + "\n");
	        	}
	        }
		}
		catch(Exception ex)
		{
			ret = ("Error:" + ex.getMessage());
		}
		return ret;
	}
	
	/**
	 * ReadCPUinfo
	 * @return
	 */
	public String ReadCPUinfo()
	{
		ProcessBuilder cmd;
		String result="";
		try
		{
			String[] 	args 	= {"/system/bin/cat", "/proc/cpuinfo"};
			cmd 				= new ProcessBuilder(args);
			Process 	process = cmd.start();
			InputStream in 		= process.getInputStream();
			byte		[]re 	= new byte[1024];
			
			while(in.read(re) != -1)
				result = result + new String(re);
			
			in.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		return result;
	}
}
