package cn.njcit.showimage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.njcit.showimage.adapter.GridViewAdapter;
import cn.njcit.showimage.adapter.ListDialogAdapter;
import cn.njcit.showimage.bean.ActionItem;
import cn.njcit.showimage.meta.MetaData;
import cn.njcit.showimage.util.BitmapUtil;
import cn.njcit.showimage.util.Util;
import cn.njcit.showimage.view.QuickActionBar;

@SuppressLint("HandlerLeak")
public class BrowseImageActivity extends Activity implements OnGestureListener, OnItemClickListener, OnClickListener{//ʵ��������ؽӿ�

	private final String TAG = "BrowseImageActivity";
	private ViewFlipper viewFlipper;//����ͼƬ�����Ŀؼ�
	private int albumId;//��¼���ID
	
	//Begin:new added===
	private GridView gridView;
	private GestureDetector detector;//��������ʶ�����
//	private String[] testData = { "/storage/sdcard/pic/1.jpg",
//			"/storage/sdcard/pic/2.jpg", "/storage/sdcard/pic/3.jpg",
//			"/storage/sdcard/pic/4.jpg", "/storage/sdcard/pic/5.jpg", };
	private int currentPosition = 0;//testData�����е�����,����ָʾ��ǰ��ʾ����ͼƬ
	private int showMode = 0;//ȷ�������ͼƬ����ʾģʽ,��ViewFlipperģʽ����GridViewģʽ
	
	private String[] actionbar_array = { "��ʾ��ʽ", "ͼƬ����", "ͼƬ��Ч", "ͼƬ����", "ͼƬ����" };
	private String[] show_array = { "�������", "Gallery���" };
	private String[] operate_array = { "����ͼƬ", "����ͼƬ", "��תͼƬ", "ͼƬ��ɫ", "ɾ��ͼƬ" };
	private String[] effect_array = { "��ƬЧ��", "����Ч��", "����Ч��", "��Ч��", "����Ч��", "ģ��Ч��" };
	// private String[] info_array = { "��ƬЧ��"};
	private String[] share_array = { "��Ϊǽֽ", "���ŷ���", "�ϴ���������" };
	
	private int[] show_array_imgIds = { R.drawable.grid64, R.drawable.flow64 };
	private int[] operate_array_imgIds = { R.drawable.zoom, R.drawable.crop,
			R.drawable.rotate, R.drawable.paint, R.drawable.delete };
	private int[] effect_array_imgIds = { R.drawable.number1,
			R.drawable.number2, R.drawable.number3, R.drawable.number4,
			R.drawable.number5, R.drawable.number6 };
	private int[] share_array_imgIds = { R.drawable.wallpaper, R.drawable.mms,
			R.drawable.renren };
	private boolean isDeleted = false;
	//End:new added===
	
	//Begin:new added for week 8===
	private static final String HISTORY_ID = "history_id";
	private static final String SHOWMODE_ID = "showMode_id";
	private ProgressDialog initDialog;
	private ProgressDialog progressDialog;
	private static int percent;
	//End:new added for week 8===
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.browseimage_layout);
		//Begin:new added===
		Display display = getWindowManager().getDefaultDisplay();
		MetaData.screenWidth = display.getWidth();
		MetaData.screenHeight = display.getHeight();
		Log.d(TAG,"screenWidth:"+MetaData.screenWidth+" and screenHeight:"+MetaData.screenHeight);
		//End:new added===
		getDataFromIntent();//��ȡ����ҳ�洫�ݽ��������ID
		initImagesList();//�����û�ѡ������ID��ʾ����е�ͼƬ
		//Begin:new added for week 8===
		if (MetaData.isCleanHistory) {
			currentPosition = 0;
			showMode = 0;
		} else {
			getDataFromPreference();
		}
		//End:new added for week 8===
		initView();
	}
	
	//Begin:new added for week 7===
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		// ���������Ϣ
		SharedPreferences settings = getSharedPreferences(HISTORY_ID, 0);
		settings.edit().putInt(MetaData.albums.get(albumId).path, currentPosition).commit();
		SharedPreferences mode = getSharedPreferences(SHOWMODE_ID, 0);
		mode.edit().putInt("mode_id", showMode).commit();
		
		// ���¼������
		if (this.isDeleted) {
			Intent intent = new Intent();
			intent.setClass(BrowseImageActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	//End:new added for week 7===

	private void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);
	}
	
	@SuppressWarnings("unchecked")
	private void initView() {
		//Begin:new added===
		detector = new GestureDetector(this);//��������ʶ�����
		//End:new added===
		
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		viewFlipper.removeAllViews();
		
		//Begin:new added===
		gridView = (GridView) findViewById(R.id.gridview);//����GridView����
		//gridView.setAdapter(new GridViewAdapter(getApplicationContext(),testData));//ΪGridView�����������
		
		//Begin:new added for week 7===
		String[] albums = null;
		/*
		List list = MetaData.albums.get(albumId).tag;
		albums = (String[]) list.toArray(new String[list.size()]);
		*/
		
		int size=MetaData.pictures.size();  
		albums = (String[])MetaData.pictures.toArray(new String[size]);  
		
		for(int i=0;i<albums.length;i++){
			Log.d(TAG,"===albums:"+albums[i]);
		}
		
		//gridView.setAdapter(new GridViewAdapter(getApplicationContext(), Util.getNames(albums)));
		gridView.setAdapter(new GridViewAdapter(getApplicationContext(), albums));
		//End:new added for week 7===
		
		//initViewFlipper();//ʹ��ViewFlipper��ʾ����е�ͼƬ
		//initGridView();//ʹ��GridView��ʾ����е�ͼƬ
		//End:new added===
		
		//Begin:new added for week 8===
		if (showMode == 0) {
			initViewFlipper();
		} else if (showMode == 1) {
			initGridView();
		}
		//End:new added for week 8===
	}
	
	//Begin:new added===
	private void initViewFlipper() {

		showMode = 0;

		if (MetaData.pictures.size() == 0) {
			finish();
			return;
		}

		// ��ֹMediaStore.Images.Media.EXTERNAL_CONTENT_URI�����쳣
		if (currentPosition >= MetaData.pictures.size()) {
			currentPosition = MetaData.pictures.size() - 1;
		}

		showBitmap(currentPosition);

		viewFlipper.setVisibility(View.VISIBLE);
		gridView.setVisibility(View.GONE);
		

	}
	
	private void initGridView() {
		if(this.isDeleted){
			finish();
		}else{
			showMode = 1;
			gridView.setSelection(currentPosition);
			gridView.setOnItemClickListener(this);//����GridView��item����¼�
			viewFlipper.setVisibility(View.GONE);
			gridView.setVisibility(View.VISIBLE);
		}
	}
	
	/*
	private void showBitmap(int pos) {

		viewFlipper.removeAllViews();

		String picturePath;
		// �ļ���ֻ��һ��ͼƬ
		if (this.testData.length == 1) {
			picturePath = this.testData[0];
		} else {
			picturePath = this.testData[pos];
		}
		ImageView imageView = new ImageView(this);
		Bitmap mBitmap = BitmapUtil.getResizeBitmap(picturePath);

		imageView.setImageBitmap(mBitmap);
		viewFlipper.addView(imageView);//��ViewFlipper�����ͼƬ
	}
	*/
	
	//Begin:new added for week 7===
	private void showBitmap(int pos) {

		viewFlipper.removeAllViews();

		String picturePath;
		// �ļ���ֻ��һ��ͼƬ
		if (MetaData.pictures.size() == 1) {
			picturePath = MetaData.pictures.get(0).toString();
		} else {
			picturePath = MetaData.pictures.get(pos).toString();
		}

		File imageFile = new File(picturePath);
		if (imageFile.length() == 0) {
			Util.notifyMediaRemove(BrowseImageActivity.this, imageFile);
			Toast.makeText(getApplicationContext(), "ͼƬ�����ڣ�����sd��", Toast.LENGTH_SHORT).show();
			return;
		}

		ImageView imageView = new ImageView(this);
		Bitmap mBitmap = BitmapUtil.getResizeBitmap(picturePath);

		imageView.setImageBitmap(mBitmap);
		viewFlipper.addView(imageView);
	}
	//End:new added for week 7===
	
	//End:new added===
	
	private void getDataFromIntent() {//���ڻ�ȡ�û�ѡ�������һ�����
		Intent intent = this.getIntent();
		albumId = intent.getIntExtra("id", 0);
		Log.d(TAG,"albumId:"+albumId);
	}
	
	
	//Begin:new added===
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return this.detector.onTouchEvent(event);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		showActionBar(viewFlipper);//���ڵ��ViewFlipper����ʾActionBar
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1 == null || e2 == null) {
			return false;
		}

		if (e1.getX() - e2.getX() > 120) {
			if (showMode == 0) {
				showNext();
			}

			return true;
		} else if (e1.getX() - e2.getX() < -120) {

			if (showMode == 0) {
				showPrevious();
			}

			return true;
		}

		return false;
	}
	
	/*
	private void showNext() {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.in_rightleft));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.out_rightleft));

		if (++currentPosition < testData.length) {
			showBitmap(currentPosition);
		} else {
			currentPosition = 0;
			showBitmap(currentPosition);
		}

		viewFlipper.showNext();
	}

	private void showPrevious() {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.in_leftright));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.out_leftright));

		if (--currentPosition < 0) {
			currentPosition = testData.length - 1;
			showBitmap(currentPosition);
		} else {
			showBitmap(currentPosition);
		}

		viewFlipper.showPrevious();
	}
	*/
	
	//Begin:new added for week 7===
	private void showNext() {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.in_rightleft));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.out_rightleft));

		if (++currentPosition < MetaData.pictures.size()) {
			showBitmap(currentPosition);
		} else {
			currentPosition = 0;
			showBitmap(currentPosition);
		}

		viewFlipper.showNext();
	}

	private void showPrevious() {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.in_leftright));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.out_leftright));

		if (--currentPosition < 0) {
			currentPosition = MetaData.pictures.size() - 1;
			showBitmap(currentPosition);
		} else {
			showBitmap(currentPosition);
		}

		viewFlipper.showPrevious();
	}
	//End:new added for week 7===

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO �Զ����ɵķ������
		Log.d(TAG,"onItemClick with position:"+position+" and id:"+id);
		//finish();
		//Begin:new added for week 8===
		currentPosition = position;
		initViewFlipper();
		//End:new added for week 8===
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		showActionBar(v);//����ActionBar��ÿ���˵���ĵ���¼�
	}
	
	private void showActionBar(View view) {
		if (view instanceof ViewFlipper) {//��ʾActionBar
			ActionItem showMode = new ActionItem(getResources().getDrawable(
					R.drawable.gallery), actionbar_array[0], this);
			ActionItem operate = new ActionItem(getResources().getDrawable(
					R.drawable.operate), actionbar_array[1], this);
			ActionItem effects = new ActionItem(getResources().getDrawable(
					R.drawable.effects), actionbar_array[2], this);
			ActionItem infoPic = new ActionItem(getResources().getDrawable(
					R.drawable.info), actionbar_array[3], this);
			ActionItem setAs = new ActionItem(getResources().getDrawable(
					R.drawable.share), actionbar_array[4], this);

			QuickActionBar qaBar = new QuickActionBar(view);
			qaBar.setEnableActionsLayoutAnim(true);

			qaBar.addActionItem(showMode);
			qaBar.addActionItem(operate);
			qaBar.addActionItem(effects);
			qaBar.addActionItem(infoPic);
			qaBar.addActionItem(setAs);

			qaBar.show();
		} else if (view instanceof LinearLayout) {//��ʾActionBar��ĳ���˵��������
			LinearLayout actionsLayout = (LinearLayout) view;
			QuickActionBar bar = (QuickActionBar) actionsLayout.getTag();
			bar.dismissQuickActionBar();

			TextView txtView = (TextView) actionsLayout
					.findViewById(R.id.qa_actionItem_name);
			String actionName = txtView.getText().toString();

			// ��ʾͼƬ��Ϣ
			if (actionName.equals(actionbar_array[3])) {
				showImageDetail();
				return;
			}

			for (int id = 0; id < actionbar_array.length; id++) {

				if (actionName.equals(actionbar_array[id])) {
					showDialog(id);
					break;
				}
			}

		}
	}
	
	
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		// TODO Auto-generated method stub
		Dialog dialog = null;
		BaseAdapter adapter = null;
		Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.logo);
		builder.setTitle(actionbar_array[id]);

		switch (id) {
		case 0:
			adapter = new ListDialogAdapter(BrowseImageActivity.this,
					show_array_imgIds, show_array);
			break;
		case 1:
			adapter = new ListDialogAdapter(BrowseImageActivity.this,
					operate_array_imgIds, operate_array);
			break;
		case 2:
			adapter = new ListDialogAdapter(BrowseImageActivity.this,
					effect_array_imgIds, effect_array);
			break;
		case 4:
			adapter = new ListDialogAdapter(BrowseImageActivity.this,
					share_array_imgIds, share_array);
			break;
		default:
			break;
		}

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				actionItemOperate(id, which);
			}
		};

		builder.setAdapter(adapter, listener);
		dialog = builder.create();

		return dialog;
	}
	
	private void actionItemOperate(int id, int position) {
		switch (id) {
		case 0:
			showMode(position);
			break;
		case 1:
			imageOperate(position);
			break;
		case 2:
			imageEffect(position);
			break;
		case 4:
			imageShare(position);
			break;
		default:
			break;
		}
	}

	private void imageShare(int position) {
		if (position==0){
			setWallpaper();
			return;
		}
		if (position==1){
			sendEMail();
			return;
		}
	}

	private void setWallpaper() {
		final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		String imagePath = MetaData.pictures.get(currentPosition);
//		@SuppressWarnings("deprecation")
//		BitmapDrawable source = new BitmapDrawable(imagePath);

		showWaitDialog();
		try {
//			wallpaperManager.setBitmap(source.getBitmap());
			wallpaperManager.setBitmap(BitmapUtil.getResizeBitmap(imagePath));
			Toast.makeText(BrowseImageActivity.this,"ǽֽ���óɹ�",Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(BrowseImageActivity.this,"ǽֽ����ʧЧ",Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	private void showWaitDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("��������ǽֽ�����Ժ�...");
		progressDialog.setProgress(0);
		progressDialog.show();
		startSplashTimer();
	}

	private void startSplashTimer() {
		Thread waitTimer = new Thread(){
			@Override
			public void run() {
				try {
				long ms = 0;
				while (ms<2000) {

					sleep(100);
					ms += 100;
				}
					progressDialog.dismiss();
				} catch (Exception e) {
					Log.e("Splash",e.getMessage());
				}finally {
					finish();
				}

			}
		};
		waitTimer.start();
	}

	private void sendEMail() {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+MetaData.pictures.get(currentPosition)));
		sendIntent.setType("image/*");
		startActivity(sendIntent);
	}


	private void imageEffect(int position) {
		Intent intent=new Intent();
		intent.setClass(BrowseImageActivity.this,EffectsImageActivity.class);
		String imagePath=MetaData.pictures.get(currentPosition);
		intent.putExtra("imagePath",imagePath);
		intent.putExtra("type",position);
		startActivity(intent);
	}

	private void showMode(int position) {

		// �������
		if (position == 0) {
			initGridView();
			return;
		}

		// Gallery���
		if (position == 1) {
			//initGallery();
			return;
		}
	}

	/*
	private void showImageDetail() {
		// String imagePath = MetaData.pictures.get(currentPosition);
		// ��������
		String imagePath = testData[0];

		final File file = new File(imagePath);
		String[] items = { "ͼƬ���ƣ�" + file.getName(),
				"ͼƬ·����" + file.getParentFile().getAbsolutePath(),
				"ͼƬ��С��" + Util.getFileSize(Util.getFileSize(file)),
				"�������ڣ�" + Util.getFileDateTime(file) };

		AlertDialog dialog = new AlertDialog.Builder(BrowseImageActivity.this)
				.setIcon(R.drawable.logo).setTitle("ͼƬ��Ϣ")
				.setItems(items, null).create();
		dialog.show();
	}
	*/
	
	//Begin:new added for week 7===
	private void showImageDetail() {
		String imagePath = MetaData.pictures.get(currentPosition);
		final File file = new File(imagePath);
		String[] items = { "ͼƬ���ƣ�" + file.getName(),
				"ͼƬ·����" + file.getParentFile().getAbsolutePath(),
				"ͼƬ��С��" + Util.getFileSize(Util.getFileSize(file)),
				"�������ڣ�" + Util.getFileDateTime(file) };

		AlertDialog dialog = new AlertDialog.Builder(BrowseImageActivity.this)
				.setIcon(R.drawable.logo).setTitle("ͼƬ��Ϣ")
				.setItems(items, null).create();
		dialog.show();
	}
	//End:new added for week 7===

	//End:new added===
	
	//Begin:new added for week 7===
	private void initImagesList() {
		MetaData.pictures.clear();
		//Util.getPicList(MetaData.albums.get(albumId).path);
		//Util.getPicListNew(albumId);
		new Timer().schedule(new ImageSearchTimerTask(),1);
		initDialog=ProgressDialog.show(this, "", "���װ����...");
		initDialog.show();
		initDialog.setCancelable(false);
	}
	
//	private static Handler handler =new Handler(){
//		public void handleMessage(Message msg){
//			super.handleMessage(msg);
//			switch (msg.what) {
//			case 0:
//				percent=(int)(count*100/length);
//				pBar.setMessage("���Ժ�...("+percent+"%)");
//				break;
//
//			}
//		}
//	};

	class ImageSearchTimerTask extends TimerTask{

		@Override
		public void run() {
			// TODO �Զ����ɵķ������
			Util.getPicList(MetaData.albums.get(albumId).path);
			h.sendEmptyMessage(0);
		}
	}
	
	Handler h=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO �Զ����ɵķ������
			switch (msg.what) {
			case 0:
				if (showMode==0) {
					initViewFlipper();
				}else if (showMode==1) {
					initGridView();
				}

				initDialog.dismiss();
			}
			super.handleMessage(msg);
		}
	};
		
	
	
	
	private void imageOperate(int position) {

		// ɾ��ͼƬ
		if (position == 4) {
			deleteImage();
			return;
		}

		Intent intent = new Intent();

		if (position == 0){
			intent.setClass(BrowseImageActivity.this, ZoomImageActivity.class);
		}

		if (position == 1){
			intent.setClass(BrowseImageActivity.this, ClipImageActivity.class);
		}

		if (position == 2){
			intent.setClass(BrowseImageActivity.this, RotateImageActivity.class);
		}

		if (position == 3){
			intent.setClass(BrowseImageActivity.this, ToningImageActivity.class);
		}

		String imagePath = MetaData.pictures.get(currentPosition);
		intent.putExtra("imagePath",imagePath);
		startActivity(intent);
	}
	
	private void deleteImage() {
		final String path = MetaData.pictures.get(currentPosition);
		final File file = new File(path);

		if (file.exists()) {
			new AlertDialog.Builder(BrowseImageActivity.this)
					.setIcon(R.drawable.delete)
					.setTitle("ɾ��ͼƬ")
					.setMessage("ȷ��Ҫɾ��ͼƬ " + file.getName() + " �� ")
					.setPositiveButton("�_��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									// ����MediaStore.Images.Media.EXTERNAL_CONTENT_URI
									try {
										Util.notifyMediaRemove(BrowseImageActivity.this, file);
										Toast.makeText(BrowseImageActivity.this,
												"ͼƬ " + file.getName() + "��ɾ���� ",
												Toast.LENGTH_SHORT).show();
										isDeleted = true;
										} catch (Exception err) {
										 err.printStackTrace();
										Toast.makeText(BrowseImageActivity.this,
												"ͼƬɾ��ʧ�ܣ�", Toast.LENGTH_SHORT).show();
										return;
									}

									// ȥ����ɾ���ļ���·��
									ArrayList<String> list = new ArrayList<String>();
									for (int i = 0; i < MetaData.pictures.size(); i++) {
										if (MetaData.pictures.get(i).equals(path))
											continue;
										list.add(MetaData.pictures.get(i));
									}

									// ���ɾ������Ψһ��ͼƬ�����˳�
									if (list.size() == 0) {
										finish();
										return;
									}

									// ����ͼƬ·������
									MetaData.pictures.clear();
									for (int i = 0; i < list.size(); i++)
									{
										String str = list.get(i);
										MetaData.pictures.add(str);
									}

									// ���ɾ�����Ƕ���β����ͼƬ����ǰλ��ǰ��
									if (currentPosition >= MetaData.pictures.size())
									{
										currentPosition = MetaData.pictures.size() - 1;
									}

									// ��ʾͼƬǰһ��
									showBitmap(currentPosition);
								}
							}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int whichButton) {
								}
							}).show();
		}
	}

	//End:new added for week 7===
	
	private void getDataFromPreference() {
		SharedPreferences histroy = getSharedPreferences(HISTORY_ID, 0);
		currentPosition = histroy.getInt(MetaData.albums.get(albumId).path, 0);

		SharedPreferences mode = getSharedPreferences(SHOWMODE_ID, 0);
		showMode = mode.getInt("mode_id", 0);
	}
}
