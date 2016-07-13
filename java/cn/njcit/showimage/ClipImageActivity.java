package cn.njcit.showimage;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import cn.njcit.showimage.util.BitmapUtil;
import cn.njcit.showimage.util.Util;

public class ClipImageActivity extends Activity {

	private final String TAG = "ClipImageActivity";
	private ImageView imageView;
	private Bitmap mBitmap;
	private String imagePath;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.croppicture_layout);

		initImageView();
		startPhotoCrop(imagePath);
	}

	private void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);
	}

	/**
	 * ��ʼ��ͼƬ�ؼ�
	 */
	private void initImageView() {
		imageView = (ImageView) findViewById(R.id.imageView);
		Intent intent = this.getIntent();
		imagePath = intent.getExtras().getString("imagePath");
		mBitmap = BitmapUtil.getResizeBitmap(imagePath);
		imageView.setImageBitmap(mBitmap);
	}

	/**
	 * �ü�ͼƬ����ʵ��
	 * 
	 * @param uri
	 */
	public void startPhotoCrop(String path) {
		File mFile = new File(path);
		Uri uri = Uri.parse("file://" + mFile.getAbsolutePath());

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// �������crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�
		intent.putExtra("crop", "true");

		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 2);
		intent.putExtra("aspectY", 3);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 480);

		intent.putExtra("return-data", true);

		try {
			Log.d(TAG,"startActivityForResult");
			startActivityForResult(intent, 1);
			return;
		} catch (ActivityNotFoundException ex) {
			Log.d("PictureShow", "activity not found! ");
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// ȡ�òü����ͼƬ
		case 1:
			if (data != null) {
				Log.d(TAG,"setPicToView");
				setPicToView(data);
			}
			break;
		default:
			Log.d(TAG,"other case");
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ����ü�֮���ͼƬ����
	 * 
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		final Bitmap bmp;

		if (extras != null) {
			bmp = extras.getParcelable("data");
			Util.showSaveDialog(ClipImageActivity.this, bmp, imagePath);
		}
	}

}