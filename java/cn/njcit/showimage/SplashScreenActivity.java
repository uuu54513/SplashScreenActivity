package cn.njcit.showimage;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SplashScreenActivity extends Activity {
	private  final int STOPSPLASH=1;
	private  int SPLASHTMIME=3000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Message msg=new Message();
		msg.what=STOPSPLASH;
		splashHandler.sendMessageDelayed(msg,SPLASHTMIME);
		
	}
	
	private Handler splashHandler=new Handler(){
		 
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				startActivity(new Intent(getApplicationContext(),MainActivity.class));
				finish();
				break;
			}
			super.handleMessage(msg);
		}
		};
	
}
