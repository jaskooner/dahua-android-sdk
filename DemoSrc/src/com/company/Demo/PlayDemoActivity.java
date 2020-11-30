package com.company.Demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;   
import android.os.Message;  
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.EditText;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.opengl.GLES20;

import com.company.PlaySDK.IPlaySDKCallBack.*;
import com.company.PlaySDK.Constants;
import com.company.PlaySDK.IPlaySDK;

public class PlayDemoActivity extends Activity {
		
	public  static final int FILE = 0;		
	public  static final int FILESTREAM = 1;	
	private static final int FILE_PATH = 101; // ÎÄ¼þÂ·¾¶
	
	static int port = 0;
	int mode = FILE; 
	String curfile;
	
	private SurfaceView sv1;
	private SeekBar ProceseekBar;
	private EditText etFile;
	private Button btMode;
	private Button BtnPlay;
	private Button btStop;
	private Button btSnapPict;
	private Button btFast;
	private Button btSlow;
	private Button btNormal;
	private Button btQuality;
	private Button btCapture;
	private View layoutQuality;
	private PopupWindow popQuality;
	
	private boolean bPlay = false;
	private boolean bPause = false;
	private boolean bCapture = false;
	
	private String strSpeed[] = new String[]{"1/64X", "1/32X", "1/16X", "1/8X", "1/4X", "1/2X", "1X", "2X", "4X", "8X", "16X", "32X", "64X"};
	private int nSpeedCur = 6;
	
	public class TestAudioRecord implements pCallFunction
	{
		public void invoke(byte[] pDataBuffer,int nBufferLen, long pUserData)
		{
			try
			{
				//sound capture.
				File file = new File("/mnt/sdcard/audiorecord.pcm");
				FileOutputStream fout = new FileOutputStream(file, true);
				fout.write(pDataBuffer); 		
				fout.close();
			
			}
			catch(Exception e)
			{
			}
		}
	
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		sv1 = (SurfaceView)findViewById(R.id.sv_demo_view);
		
		SurfaceHolder holder = sv1.getHolder();
		holder.addCallback(new Callback() {
		   	public void surfaceCreated(SurfaceHolder holder)
			{
		   		//if PLAYPlay is called brefore system surfaceCreated.
		   		//Initsurface should be called here. 
		   		IPlaySDK.InitSurface(port, sv1);
		   		Log.d("[playsdk]", "surface surfaceCreated");
			}
			
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height)
			{
				//if the size of sufaceView is changed,
				//PLAYViewResolutionChanged should be called here.
				IPlaySDK.PLAYViewResolutionChanged(port, width, height, 0);
				Log.d("[playsdk]", " surface surfaceChanged");
			}
		
			public void surfaceDestroyed(SurfaceHolder holder)
			{
				Log.d("[playsdk]", "surface surfaceDestroyed");
			}
		});
		     
		Button BtnOpenFile = (Button)findViewById(R.id.bt_demo_file);
		BtnOpenFile.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Select file...", Toast.LENGTH_LONG).show();
			
					jumpToFileListActivity();
			}
		});
		
		etFile = (EditText)findViewById(R.id.et_demo_file);
		
		btMode = (Button)findViewById(R.id.bt_demo_mode);
		btMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mode = (mode + 1) % 2;
				if (FILE == mode) {
					btMode.setText(R.string.demo_activity_mode_stream);
				} else {
					btMode.setText(R.string.demo_activity_mode_file);
				}
			}
		});
		
		 
		btCapture = (Button)findViewById(R.id.bt_demo_capture);
		btCapture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (bCapture == false) {
					//sound capture.
					int m_nBitPerSample = 16;
					int m_nSamplePerSecond = 8000;
					int lSampleLen = 1024;
					TestAudioRecord m_audiorecordcallback = new TestAudioRecord();
					int retValue = IPlaySDK.PLAYOpenAudioRecord(m_audiorecordcallback,m_nBitPerSample,m_nSamplePerSecond, lSampleLen, 0); 	
					if(0 == retValue)
					{
						Log.d("[playsdk]", "PLAYOpenAudioRecord Failed.");
					}
					
					btCapture.setText(R.string.demo_activity_stopcapture);
					bCapture = true;
				} else {
					IPlaySDK.PLAYCloseAudioRecord();
					btCapture.setText(R.string.demo_activity_capture);
					bCapture = false;
				}
			}
		});
        
		BtnPlay = (Button)findViewById(R.id.bt_demo_play);
		BtnPlay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!bPlay) {
					curfile = etFile.getText().toString();
					if (curfile == null || curfile.equals(""))
			    	{
						Toast.makeText(getApplicationContext(), "Please select/input one file to play", Toast.LENGTH_LONG).show();
			    		return;
			    	}
					
					bPlay = true;
				
					//start play
					StartPlaySDK();
					
					//start process control timer
					new Thread(new ThreadProcess()).start(); 
				 				
	 				BtnPlay.setText(R.string.demo_activity_pause);
	 				
	 				btMode.setEnabled(false);
	 				btStop.setEnabled(true);
	 				btSnapPict.setEnabled(true);
	 				
	 				btFast.setEnabled(true);
					btSlow.setEnabled(true);
					btNormal.setEnabled(true);
					btQuality.setEnabled(true);
					
					nSpeedCur = 6;
	 			} else {
	 				if (!bPause) {
	 					IPlaySDK.PLAYPause(port, (short)1);
	 					
	 					bPause = true;
		 				BtnPlay.setText(R.string.demo_activity_play);
	 				} else {
	 					IPlaySDK.PLAYPause(port, (short)0);
	 					
	 					bPause = false;
		 				BtnPlay.setText(R.string.demo_activity_pause);
	 				}
	 			}
			}
		});
		
		btStop = (Button)findViewById(R.id.bt_demo_stop);
		btStop.setEnabled(false);
		btStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//stop play
	    		try {
	                Message msg = new Message();   
	                msg.what = 2;   
	                handler.sendMessage(msg);   
	            } catch (Exception e) {   
	                e.printStackTrace();   
	            }
			}
		});
		
		btSnapPict = (Button)findViewById(R.id.bt_demo_SnapPict);
		btSnapPict.setEnabled(false);
		btSnapPict.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d("[playsdk]", "bt_demo_SnapPict onClick");
				//Get time
				File SDFile = Environment.getExternalStorageDirectory();
				File sdPath = new File(SDFile.getAbsolutePath() + "/test");
				if (sdPath.canWrite())
				{
					byte[] rr = new byte[24];  // array size must large than 24 , because it is used to store 6 int. if not , QueryInfo will return false.
					long year = 0;
					long month = 0;
					long day = 0;
					long hour = 0;
					long minute = 0;
					long second = 0;
					Integer gf = Integer.valueOf(0);
					if(IPlaySDK.PLAYQueryInfo(port, Constants.PLAY_CMD_GetTime, rr, rr.length, gf) != 0)
					{
						year 	= ((long)(rr[3]  & 0xff) << 24) + ((long)(rr[2]  & 0xff) << 16) + ((long)(rr[1]  & 0xff) << 8) + (long)(rr[0]  & 0xff);
						month 	= ((long)(rr[7]  & 0xff) << 24) + ((long)(rr[6]  & 0xff) << 16) + ((long)(rr[5]  & 0xff) << 8) + (long)(rr[4]  & 0xff);
						day 	= ((long)(rr[11] & 0xff) << 24) + ((long)(rr[10] & 0xff) << 16) + ((long)(rr[9]  & 0xff) << 8) + (long)(rr[8]  & 0xff);
						hour 	= ((long)(rr[15] & 0xff) << 24) + ((long)(rr[14] & 0xff) << 16) + ((long)(rr[13] & 0xff) << 8) + (long)(rr[12] & 0xff);
						minute 	= ((long)(rr[19] & 0xff) << 24) + ((long)(rr[18] & 0xff) << 16) + ((long)(rr[17] & 0xff) << 8) + (long)(rr[16] & 0xff);
						second 	= ((long)(rr[23] & 0xff) << 24) + ((long)(rr[22] & 0xff) << 16) + ((long)(rr[21] & 0xff) << 8) + (long)(rr[20] & 0xff);
					}
					String name = year + "_" + month + "_" + day + "_" + hour + "_" + minute + "_" + second + ".bmp"; 
					String snapPictFile = sdPath.getAbsolutePath() + "/" + name;
					Log.d("[playsdk]", "SnapPictFile :" + snapPictFile);
					//Snap Picture
					if(IPlaySDK.PLAYCatchPic(port, snapPictFile) != 0)
					{
						Log.d("[playsdk]", "PLAYCatchPic Success" );
					}
				}
			}
		});
		

		btFast = (Button)findViewById(R.id.bt_demo_fast);
		btFast.setEnabled(false);
		btFast.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				int b = IPlaySDK.PLAYFast(port);
				if (0 != b) {
					if (nSpeedCur < strSpeed.length - 1) {
						nSpeedCur++;
						Toast.makeText(getApplicationContext(), strSpeed[nSpeedCur], Toast.LENGTH_LONG).show();
					}
				}
        	}
        });
		
		btSlow = (Button)findViewById(R.id.bt_demo_slow);
		btSlow.setEnabled(false);
		btSlow.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				int b = IPlaySDK.PLAYSlow(port);
				if (0 != b) {
					if (nSpeedCur > 0) {
						nSpeedCur--;
						Toast.makeText(getApplicationContext(), strSpeed[nSpeedCur], Toast.LENGTH_LONG).show();
					}
				}
        	}
        });
		
		btNormal = (Button)findViewById(R.id.bt_demo_normal);
		btNormal.setEnabled(false);
		btNormal.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				int b = IPlaySDK.PLAYPlay(port, sv1);
				if (0 != b) {
					BtnPlay.setText(R.string.demo_activity_pause);
					bPause = false;
					
					nSpeedCur = 6;
					Toast.makeText(getApplicationContext(), strSpeed[nSpeedCur], Toast.LENGTH_LONG).show();
				}
			}
        });
		
		layoutQuality = View.inflate(PlayDemoActivity.this, R.layout.qualityview, null);
		btQuality = (Button)findViewById(R.id.bt_demo_quality);
		btQuality.setEnabled(false);
        btQuality.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
	        	Integer stBright = Integer.valueOf(0);
	        	Integer stContrast = Integer.valueOf(0);
	        	Integer stSaturation = Integer.valueOf(0);
	        	Integer stHuen = Integer.valueOf(0);
	        	int nRet = IPlaySDK.PLAYGetColor(port, 0, stBright, stContrast, stSaturation, stHuen);
	        	if (0 == nRet) {
	        		Toast.makeText(getApplicationContext(), "Get color failed", Toast.LENGTH_LONG).show();
	        		return;
	        	}
	        	
	        	if (null == popQuality) {
	        		popQuality = new PopupWindow(layoutQuality, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    		}
	    		
	    		if (popQuality.isShowing()) {
	    			popQuality.dismiss();
	    			return;
	    		} else {
	    			popQuality.showAtLocation(findViewById(R.id.layout_demo), Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 150);
	    		}
	    		
	    		SeekBar sbBright = (SeekBar)layoutQuality.findViewById(R.id.sb_quality_bright);
	    		sbBright.setMax(128);
	    		sbBright.incrementProgressBy(1);
	    		sbBright.setProgress(stBright);
	    		sbBright.setOnSeekBarChangeListener(new ColorSeekBarListenner());
	    		
	    		SeekBar sbContrast = (SeekBar)layoutQuality.findViewById(R.id.sb_quality_contrast);
	    		sbContrast.setMax(128);
	    		sbContrast.incrementProgressBy(1);
	    		sbContrast.setProgress(stContrast);
	    		sbContrast.setOnSeekBarChangeListener(new ColorSeekBarListenner());
	    		
	    		SeekBar sbSaturation = (SeekBar)layoutQuality.findViewById(R.id.sb_quality_saturation);
	    		sbSaturation.setMax(128);
	    		sbSaturation.incrementProgressBy(1);
	    		sbSaturation.setProgress(stSaturation);
	    		sbSaturation.setOnSeekBarChangeListener(new ColorSeekBarListenner());
	    		
	    		SeekBar sbHuen = (SeekBar)layoutQuality.findViewById(R.id.sb_quality_huen);
	    		sbHuen.setMax(128);
	    		sbHuen.incrementProgressBy(1);
	    		sbHuen.setProgress(stHuen);
	    		sbHuen.setOnSeekBarChangeListener(new ColorSeekBarListenner());
        	}
        });
        
        ProceseekBar =(SeekBar)findViewById(R.id.sb_demo_process);
        ProceseekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
		           if (fromUser) {
		                int SeekPosition=seekBar.getProgress();
		                float fpos = (float)SeekPosition/100;
		        	   if(mode == FILE){
			                IPlaySDK.PLAYSetPlayPos(port, fpos);
		        	   }else{
		        		   IPlaySDK.PLAYResetSourceBuffer(0);
			       			try {
			       				fis.seek((long) (fpos*fileLength));
			       			} catch (IOException e) {
			       				e.printStackTrace();
			       			}
			       			bResetStreamPos = true;
		        	   }
		            }

				
			}
		});
    }
    
    @Override
	public void onPause()
	{
    	// close surface when PlayDemoActivity changeState
		if(bPlay)
		{
		    IPlaySDK.PLAYSurfaceChange(port,null);
		}
		super.onPause();
	}
    
    @Override
	public void onResume()
	{
		if(bPlay)
		{
		    IPlaySDK.PLAYSurfaceChange(port, sv1);
		}
		super.onResume();
	}
    
    public void StartPlaySDK()
    {
    	int retValue = 0;
		if(mode == FILE){
			long lUsrdata = 0;
			retValue = IPlaySDK.PLAYSetFileEndCallBack(port, new PlayEndCallBack(), lUsrdata);
			if(0 == retValue){
				return;
			}
			
			retValue = IPlaySDK.PLAYOpenFile(port,curfile);
			if(0 == retValue){
				return;
			}
		}else{
			IPlaySDK.PLAYSetStreamOpenMode(port, Constants.STREAME_FILE);
			retValue = IPlaySDK.PLAYOpenStream(port, null, 0, 5*1024*1024);
			if(0 == retValue){
				return;
			}
		}
		
		/*DrawCallBack Test*/
		/*retValue = IPlaySDK.PLAYRigisterDrawFun(port, 0, new DrawCallback(), 0);
		if(0 == retValue)
		{
			return;
		}*/

		// this interface can be called to accelerate when decode h264
		IPlaySDK.PLAYSetDecodeThreadNum(port, 4);

		retValue = IPlaySDK.PLAYPlay(port, sv1);
		if(0 == retValue){
			return;
		}
		
		retValue = IPlaySDK.PLAYPlaySound(port);
		if(0 == retValue){
			Log.d("[playsdk]", "PLAYPlaySound Failed.");
		}
		
		if(mode == FILESTREAM){
			new Thread(new FileStreamDataFill()).start();
		}
    }
    
    public void StopPlaySDK()
    {
    	IPlaySDK.PLAYRigisterDrawFun(port, 0, null, 0);
    	if(0 != mProgram)
    	{
    		GLES20.glDeleteProgram(mProgram);
    		mProgram = 0;
    	}
    	
		IPlaySDK.PLAYStopSound();
		//clear Screen
		IPlaySDK.PLAYCleanScreen(port, 0/*red*/, 0/*green*/, 0/*blue*/, 1/*alpha*/, 0);
		IPlaySDK.PLAYStop(port);
			
		if(mode == FILE)
		{
			IPlaySDK.PLAYCloseFile(port);
		}
		else
		{
			IPlaySDK.PLAYCloseStream(port);
		}
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (bPlay) {
    		StopPlaySDK();
    	}
    	
    	if (null != popQuality && popQuality.isShowing()) {
    		popQuality.dismiss();
    	}
    }

    //implement timer
    Handler handler = new Handler() {   
    public void handleMessage(Message msg) {   
                if (msg.what == 1) {   
                	   if(mode == FILE){
                		   float fproc = IPlaySDK.PLAYGetPlayPos(port);
                		   if(fproc < 0.01)
                		   {
                			   Log.d("","");
                		   }
                		   ProceseekBar.setProgress((int)(fproc*100));
		        	   }
                	   else{                		   
                		   long curPos = 0;
							try {
								curPos = fis.getFilePointer() - IPlaySDK.PLAYGetSourceBufferRemain(port);
							} catch (IOException e) {
								e.printStackTrace();
							}
                		   if(fileLength != 0){
                			   ProceseekBar.setProgress((int)(curPos*100 / fileLength));
                		   }
		        	   }
               } else if (msg.what == 2) {
            	   	
            	   	StopPlaySDK();
					bPlay = false;
					btStop.setEnabled(false);
					btSnapPict.setEnabled(false);
					
					BtnPlay.setText(R.string.demo_activity_play);
					bPause = false;
					
					btMode.setEnabled(true);
					
					btFast.setEnabled(false);
					btSlow.setEnabled(false);
					btNormal.setEnabled(false);
					btQuality.setEnabled(false);
					
					if (null != popQuality && popQuality.isShowing()) {
						popQuality.dismiss();
					}
               }
            };   
      };   

    //implement timer
    class ThreadProcess implements Runnable {   

       public void run() {   
            while (bPlay) {   
               try {   
                    Thread.sleep(10);   
                    Message msg = new Message();   
                    msg.what = 1;   
                    handler.sendMessage(msg);   
                } catch (Exception e) {   
                    e.printStackTrace();   
                }
            } 
	   		 Log.d("[playsdk]", "ThreadProcess End");
       }
    }

    long fileLength;
    boolean bResetStreamPos;
    RandomAccessFile fis;
	class FileStreamDataFill implements Runnable { 
		
		public void run() {
			try
			{
				fis = new RandomAccessFile(curfile,"rw");
				fileLength = fis.length();
				int readsize = 1024;
				byte[] buffer = new byte[readsize];
				int readlen = 0;
				int ret = 1;
				bResetStreamPos = false;
				while(bPlay){
					if(bResetStreamPos){
						bResetStreamPos = false;
						ret = 1;
					}
					if(ret == 1){
						readlen = fis.read(buffer);
					}
					if(readlen == -1){
						Thread.sleep(100);
						continue;
					}

					ret = IPlaySDK.PLAYInputData(port, buffer, readlen);
					if(ret == 0){
						Thread.sleep(10); 
						Log.d("[playsdk]", "PLAYInputData Failed.");
					}
				}
				fis.close();
			}
			catch(Exception e)
			{
			}
		}
	}

    private class PlayEndCallBack implements fpFileEndCBFun {
    	@Override
    	public void invoke(int nPort, long pUserData) {
    		try {
                Message msg = new Message();   
                msg.what = 2;   
                handler.sendMessage(msg);   
            } catch (Exception e) {   
                e.printStackTrace();   
            }
    	}
    }
    
    private final String vertexShader = 
    		"attribute vec4 aColor;\n" + 
    		"varying vec4 vColor;\n" + 
    		"attribute vec4 vPosition;\n" +
    		"void main(){ \n" +
    		"gl_Position = vPosition;\n" +
    		"vColor = aColor;\n" +
    		"}\n";
    
    private final String fragShader = 
    		"precision mediump float; \n" + 
    		"varying vec4 vColor;\n" + 
    		"void main(){ \n" +
    		"gl_FragColor = vColor;\n" +
    		"}\n";
    
    private final float[] vertexArray= new float[]{
			-0.5f, 0.5f, 0.0f,
			-0.5f, -0.5f, 0.0f,
			0.5f, -0.5f, 0.0f,
			0.5f, 0.5f, 0.0f
	};
		
    private final float[] colorArray= new float[]{
			255.0f, 0.0f, 0.0f, 1.0f,
			255.0f, 0.0f, 0.0f, 1.0f,
			255.0f, 0.0f, 0.0f, 1.0f,
			255.0f, 0.0f, 0.0f, 1.0f
	};
		
    private int mProgram = 0;
	private int maPositionHandle = 0;
	private int maColorHandle = 0;
	FloatBuffer vertexBuffer;
	FloatBuffer colorBuffer;
	
    private class DrawCallback implements fDrawCBFun {
    	@Override
    	public void invoke(int nPort,int regionnum, long eglContext, long pUserData)
		{		
     		if(mProgram == 0)
     		{        		
        		int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
         		GLES20.glShaderSource(vshader, vertexShader);
         		GLES20.glCompileShader(vshader);
         		int compiled[] = new int[1];
         		GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        		if(compiled[0] != GLES20.GL_TRUE)
        		{
        			Log.d("[playsdk]", "compile vsharder failed.");
        		}
         		
         		int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
         		GLES20.glShaderSource(fshader, fragShader);
         		GLES20.glCompileShader(fshader);
         		GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        		if(compiled[0] != GLES20.GL_TRUE)
        		{
        			Log.d("[playsdk]", "compile fsharder failed.");
        		}
         		
        		mProgram = GLES20.glCreateProgram();
        		GLES20.glAttachShader(mProgram, vshader);
        		GLES20.glAttachShader(mProgram, fshader);
        		GLES20.glLinkProgram(mProgram);
        		GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, compiled, 0);
        		if(compiled[0] != GLES20.GL_TRUE)
        		{
        			Log.d("[playsdk]", "link program failed.");
					GLES20.glDeleteShader(vshader);
            		GLES20.glDeleteShader(fshader);
        			GLES20.glDeleteProgram(mProgram);
        			mProgram = 0;
        			return;
        		}
      
            	GLES20.glDeleteShader(vshader);
            	GLES20.glDeleteShader(fshader);
        
        		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        		maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        		
     			ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length*4);
        		vbb.order(ByteOrder.nativeOrder());
        		vertexBuffer = vbb.asFloatBuffer();
        		vertexBuffer.put(vertexArray);
        		vertexBuffer.position(0);
        		
        		ByteBuffer cbb = ByteBuffer.allocateDirect(colorArray.length*4);
        		cbb.order(ByteOrder.nativeOrder());
        		colorBuffer = cbb.asFloatBuffer();
        		colorBuffer.put(colorArray);
        		colorBuffer.position(0);
     		}
    		
    		GLES20.glUseProgram(mProgram);
    			
    		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
    		GLES20.glEnableVertexAttribArray(maPositionHandle);
    		
    		GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false, 16, colorBuffer);
    		GLES20.glEnableVertexAttribArray(maColorHandle);
    		
    		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
    	
		}	
	}
    
    
    public void jumpToFileListActivity()
    {
		Intent intent = new Intent();
		intent.setClass(this, FileListActivity.class);
		startActivityForResult(intent, FILE_PATH);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data == null) {
			return;
		}
		
		if (requestCode == FILE_PATH && resultCode == RESULT_OK) {
			curfile = data.getStringExtra("selectabspath");
			etFile.setText(curfile);
			etFile.setSelection(etFile.length());
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK)
    	{
    		StopPlaySDK();
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    private class ColorSeekBarListenner implements OnSeekBarChangeListener {
		@Override
    	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    		if (fromUser) {
    			Integer stValue[] = new Integer[4];
    			for (int i = 0; i < 4; i++) {
    				stValue[i] = Integer.valueOf(0);
    			}
    			
    			@SuppressWarnings("unused")
				int nRet = IPlaySDK.PLAYGetColor(port, 0, stValue[0], stValue[1], stValue[2], stValue[3]);
    			if (seekBar == (SeekBar)layoutQuality.findViewById(R.id.sb_quality_bright)) {
    				nRet = IPlaySDK.PLAYSetColor(port, 0, progress, stValue[1], stValue[2], stValue[3]);
    			} else if (seekBar == (SeekBar)layoutQuality.findViewById(R.id.sb_quality_contrast)) {
    				nRet = IPlaySDK.PLAYSetColor(port, 0, stValue[0], progress, stValue[2], stValue[3]);
    			} else if (seekBar == (SeekBar)layoutQuality.findViewById(R.id.sb_quality_saturation)) {
    				nRet = IPlaySDK.PLAYSetColor(port, 0, stValue[0], stValue[1], progress, stValue[3]);
    			} else if (seekBar == (SeekBar)layoutQuality.findViewById(R.id.sb_quality_huen)) {
    				nRet = IPlaySDK.PLAYSetColor(port, 0, stValue[0], stValue[1], stValue[2], progress);
    			}
    		}
    	}
    	
		

    	@Override
    	public void onStartTrackingTouch(SeekBar seekBar) {
    		
    	}
    	
    	@Override
    	public void onStopTrackingTouch(SeekBar seekBar) {
    		
    	}
	}
}