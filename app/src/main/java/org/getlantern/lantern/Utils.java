/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */
package org.getlantern.lantern;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class Utils implements Constants {

	
	
	public static int findProcessId(String command) throws IOException 
	{
		int procId = findProcessIdWithPS(command);
		return procId;
	}
	
	//use 'ps' command
	public static int findProcessIdWithPS(String command) throws IOException 
	{
		
		int procId = -1;
		
		Runtime r = Runtime.getRuntime();
		    	
		Process procPs = null;
		
		String processKey = new File(command).getName();
		
        procPs = r.exec(SHELL_CMD_PS + ' ' + processKey); // this is the android ps <name> command
            
        BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
        String line = null;
        
        while ((line = reader.readLine())!=null)
        {
        	if (line.contains("PID"))
        		continue;
        		
        	if (line.contains('/' + processKey))
        	{
        		
        		String[] lineParts = line.split("\\s+");
        		
        	    try { 
        	        
        	        procId = Integer.parseInt(lineParts[1]); //for most devices it is the second number
        	    } catch(NumberFormatException e) {
        	    	procId = Integer.parseInt(lineParts[0]); //but for samsungs it is the first
        	        
        	    }
        		
        		
        		break;
        	}
        }
        
        try { procPs.destroy(); } catch (Exception e) {} // try to destroy just to make sure we clean it up
       
        return procId;

	}
	
	public static SharedPreferences getSharedPrefs (Context context)
	{
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
			return context.getSharedPreferences(Constants.PREF_LANTERN_SHARED_PREFs,0 | Context.MODE_MULTI_PROCESS);
		else
			return context.getSharedPreferences(Constants.PREF_LANTERN_SHARED_PREFs,Context.MODE_PRIVATE);
		
	}
}
