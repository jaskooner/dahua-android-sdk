package com.company.Demo;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;


public class FileListActivity extends ListActivity 
{
	private List<String> arrayData;
	
    public void showLog(String strLog)
    {
    	Toast.makeText(this, strLog, Toast.LENGTH_SHORT).show();
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        arrayData = getData();
     	if(arrayData.isEmpty())
    	{
    		showLog("Sorry, no dav files found in sdcard.");
    	}
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayData);
        setListAdapter(adapter);
    }
    
    private List<String> getData() {
        List<String> data = new ArrayList<String>();
    	String sDStateString = Environment.getExternalStorageState();
    	if (sDStateString.equals(Environment.MEDIA_MOUNTED)) {
    		try {
    			File SDFile = Environment.getExternalStorageDirectory();
    			/*default filepath is: /mnt/sdcard/test/, can be changed here*/
    			File sdPath = new File(SDFile.getAbsolutePath() + "/test");
    			if (sdPath.listFiles().length > 0) {
    				for (File file : sdPath.listFiles()) {
    					{
    						data.add(file.getAbsolutePath());
    					}
    				}
    			}
    			sdPath = new File("storage/sdcard1/test");
    			if (sdPath.listFiles().length > 0) {
    				for (File file : sdPath.listFiles()) {
    					{
    						data.add(file.getAbsolutePath());
    					}
    				}
    			} 
    		} catch (Exception e) {

    		}    		
    	}    	 	
        return data;
    }

	protected void onDestroy() 
	{
		super.onDestroy();
	}
	
    public void jumpToPlayDemoActivity(String selectfile)
    {
    	Intent intent = new Intent();
		intent.putExtra("selectabspath", selectfile);
		setResult(RESULT_OK, intent);
		finish();
    }
   
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		String selectfile = arrayData.get(position);
		jumpToPlayDemoActivity(selectfile);
	
		super.onListItemClick(l, v, position, id);
	}
}