package cn.njcit.showimage;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.njcit.showimage.adapter.ListViewAdapter;
import cn.njcit.showimage.meta.MetaData;
import cn.njcit.showimage.util.Util;

import static cn.njcit.showimage.meta.MetaData.albums;

public class MainActivity extends ListActivity implements OnItemClickListener, OnClickListener{//ʵ��ListView��item����¼�
	
	private final String TAG = "MainActivity";
	private ListViewAdapter listViewAdapter;//������ҳ����ListView�ؼ����ʹ�õ�ListViewAdapter
	private ImageButton infoButton;
	private ImageButton cameraButton;
	private static final String SETTING_INFOS = "cn.njcit.showimage_preferences";
	//private static AsyncTask<String, Integer, Integer> scanAlbumsTask = null;
	private ScanAlbumsTask scanAlbumsTask;
	private static final String SHORTCUT_INFOS = "shortCut_infos";
	private static boolean isAddShortCut = false;
	private  static final String ACTION_INSTALL = "com.android.launcher.action.INSTALL_SHORTCUT";


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		//initData();//ʹ��ģ�����ݽ������ݳ�ʼ��
		getPreferencesDataFromSETTING_INFOS();
		getPreferencesDataFromSHORTCUT_INFOS();
		initTitle();
		listViewAdapter = new ListViewAdapter(getApplicationContext(), albums);//����ListViewAdapter����
		//listViewAdapter.notifyDataSetChanged();//ˢ������������
		setListAdapter(listViewAdapter);//��������ListView�ؼ���
		getListView().setOnItemClickListener(this);

	}

	@Override
	protected void onResume() {
		initAlbumsList();//��ȡ�豸�ϵ������Ϣ
		listViewAdapter.refresh(albums);
		super.onResume();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_layout, menu);
    	/*
    	//Begin:new added===
    	menu.add(Menu.NONE, Menu.FIRST, 0, "ɨ��SD��").setIcon(R.drawable.scan);
		menu.add(Menu.NONE, Menu.FIRST + 2, 0, "����").setIcon(R.drawable.update);
		menu.add(Menu.NONE, Menu.FIRST + 3, 0, "����").setIcon(R.drawable.setting);
		//End:new added===
		*/
        return true;
    }
    


    //Begin:new added===
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
    	super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		//case Menu.FIRST:
		case R.id.item1:
			Toast.makeText(this, "ѡ����\"ɨ��SD��\"�˵���", Toast.LENGTH_SHORT).show();
			break;
		//case Menu.FIRST + 2:
		case R.id.item2:
			Toast.makeText(this, "ѡ����\"����\"�˵���", Toast.LENGTH_SHORT).show();
			break;
		//case Menu.FIRST + 3:
		case R.id.item3:
			settings();
			break;
		default:
			break;
		}

		return true;
	}
    
    private void settings() {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SettingActivity.class);
		startActivity(intent);
	}
    //End:new added===

//	private void initData(){
//    	MetaData.albums.clear();
//
//    	Albums info = new Albums();
//    	info.displayName = "pic";
//		info.picturecount = "5";
//		info.path = "/storage/sdcard/pic";
//		info.icon = BitmapUtil.getOptionBitmap("/storage/sdcard/pic/1.jpg");
//		MetaData.albums.add(info);
//    }
	
	private void initTitle() {
		infoButton = (ImageButton) findViewById(R.id.title_logo);
		cameraButton = (ImageButton) findViewById(R.id.title_camera);
		infoButton.setOnClickListener(this);
		cameraButton.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//���������ͼƬ�������
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), BrowseImageActivity.class);
		intent.putExtra("id", position);
		this.startActivity(intent);
	}

	//Begin:new added===
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_logo:
			showAbout();
			break;
		case R.id.title_camera:
			//openCamera();
			break;
		default:
			break;
		}
	}
	
	private void showAbout() {
		AlertDialog.Builder aboutDialog = new AlertDialog.Builder(
				MainActivity.this);
		InputStream ips = MainActivity.this.getResources().openRawResource(
				R.raw.readme);
		DataInputStream dis = new DataInputStream(ips);
		try {
			byte[] bytes = new byte[dis.available()];
			String str = "";
			while (ips.read(bytes) != -1)
				str = str + new String(bytes, "GBK");
			aboutDialog.setIcon(R.drawable.logo).setTitle("����")
					.setMessage(str);
			aboutDialog.setPositiveButton("ȷ��", null);
			aboutDialog.create().show();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				dis.close();
				ips.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//End:new added===
	
	//Begin:new added for week 7===
	private void initAlbumsList() {
		//Util.getThumbnailsPhotosInfo(MainActivity.this, Environment.getExternalStorageDirectory().getPath());
		if(scanAlbumsTask!=null&&scanAlbumsTask.getStatus()==ScanAlbumsTask.Status.RUNNING){
			scanAlbumsTask.cancel(true);
		}
		scanAlbumsTask =new ScanAlbumsTask();
		scanAlbumsTask.execute(Environment.getExternalStorageDirectory().getPath());
	}
	//End:new added for week 7===
	class ScanAlbumsTask extends AsyncTask<String,Integer,Integer>{
		
		public ScanAlbumsTask(){
			albums.clear();
		}
		
		@Override
		protected void onPreExecute() {
			// TODO �Զ����ɵķ������
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO �Զ����ɵķ������
 			if(params==null||params[0]==null||"".equals(params[0])){
				return 0;
			}
			Util.getThumbnailsPhotosInfo(MainActivity.this, params[0]);
			Log.e("String", params[0]);
			return albums.size();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO �Զ����ɵķ������
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			// TODO �Զ����ɵķ������
			super.onPostExecute(result);
			if (MetaData.albums.size()==0){
				isExit();
			}
			listViewAdapter=new ListViewAdapter(getApplicationContext(), albums);
			listViewAdapter.notifyDataSetChanged();
			setListAdapter(listViewAdapter);
		}
		
	}
	
	//Begin:new added for week 8===
	private void getPreferencesDataFromSETTING_INFOS() {
		final SharedPreferences settingData = getSharedPreferences(SETTING_INFOS, 0);
		MetaData.isCleanHistory = settingData.getBoolean("cleanHistory", false);
		//MetaData.isAutoUpdate = settingData.getBoolean("autoUpdate", false);
		//MetaData.appWidgetPath = settingData.getString("listPreference",Environment.getExternalStorageDirectory() + "/DCIM/Camera");
	}
	//Begin:new added for week 8===

	private void isExit(){
		new AlertDialog.Builder(MainActivity.this).setIcon(R.drawable.logo).setTitle("��ʾ")
				.setMessage("δ����ͼ���ļ�,�Ƿ��˳�����?")
				.setPositiveButton("�˳�", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("ȡ��",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}


	private void getPreferencesDataFromSHORTCUT_INFOS(){
		final SharedPreferences shortCutData = getSharedPreferences(SETTING_INFOS,0);
		isAddShortCut = shortCutData.getBoolean("isAdd",true);
		if (isAddShortCut){
			new AlertDialog.Builder(MainActivity.this).setTitle("��ʾ").setIcon(R.drawable.logo)
					.setMessage("�Ƿ���������ӿ�ݷ�ʽ?")
					.setPositiveButton("���", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							shortCutData.edit().putBoolean("isAdd",false)
									.commit();
							addShortToDesktop(MainActivity.this);
						}
					}).setNegativeButton("ȡ��",new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).show();
		}
	}

	private void addShortToDesktop(Context context) {
		Intent shortcut = new Intent(ACTION_INSTALL);
		String pakageName = context.getPackageName();
		String className = "SplashScreenActivity";
		String appClass = pakageName + "." +className;
		Resources res = context.getResources();
		String label = "ͼ��";
		Bitmap bmp = BitmapFactory.decodeResource(res,R.drawable.logo);
		Drawable drawable = new BitmapDrawable(bmp);
		BitmapDrawable iconBitmapDrawabel = (BitmapDrawable) drawable;

		PackageManager packageManager = context.getPackageManager();

		try{
			@SuppressWarnings("unused")
			ApplicationInfo appInfo = packageManager.getApplicationInfo(
					pakageName,PackageManager.GET_META_DATA | PackageManager.GET_ACTIVITIES
			);
		}catch (PackageManager.NameNotFoundException e){
			e.printStackTrace();
			return;
		}
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,label);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON,iconBitmapDrawabel.getBitmap());
		shortcut.putExtra("duplicate",false);
		ComponentName comp = new ComponentName(pakageName,appClass);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,new Intent(Intent.ACTION_MAIN).setComponent(comp));
		context.sendBroadcast(shortcut);
		Toast.makeText(context,"��ݷ�ʽ����ӣ�",Toast.LENGTH_SHORT).show();
	}

}
