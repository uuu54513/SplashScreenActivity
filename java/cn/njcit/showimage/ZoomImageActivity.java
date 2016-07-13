package cn.njcit.showimage;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ZoomControls;
import cn.njcit.showimage.util.BitmapUtil;

public class ZoomImageActivity extends Activity implements OnTouchListener {
	private static final String TAG = "ZoomPictureActivity";

	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF start = new PointF();
	private PointF mid = new PointF();
	float oldDist;
	private ImageView imageView;
	private ZoomControls zoomControls;

	// button zoom
	private float scaleWidth = 1;
	private float scaleHeight = 1;
	private int displayWidth;
	private int displayHeight;
	private Bitmap mBitmap, zoomedBMP;
	private static final double ZOOM_IN_SCALE = 1.25;// �Ŵ�ϵ��
	private static final double ZOOM_OUT_SCALE = 0.8;// ��Сϵ��

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// private boolean isContinue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.zoompicture_layout);

		// �������л���ֹ����
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		initZoomControls();
		initImageView();
		initViewSize();
		enlarge();
	}
	
	private void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);
	}

	/**
	 * ��ʼ�����ſؼ�
	 */
	private void initZoomControls() {
		// �Ŵ�ť
		zoomControls = (ZoomControls) findViewById(R.id.zoomcontrols);
		/* �Ŵ�ťonClickListener */
		zoomControls.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enlarge();
			}
		});

		/* ��С��ťonClickListener */
		zoomControls.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				small();
			}

		});
	}

	/**
	 * ��ʼ��ͼƬ�ؼ�
	 */
	private void initImageView() {
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnTouchListener(this);
		String imagePath;
		Intent intent = this.getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			Uri mUri = intent.getData();
			imagePath = mUri.getPath().toString();
		} else {
			imagePath = intent.getExtras().getString("imagePath");
		}

		mBitmap = BitmapUtil.getResizeBitmap(imagePath);
		imageView.setImageBitmap(mBitmap);
		imageView.setOnTouchListener(this);
	}

	/**
	 * ��ʼ����Ļ�ߴ�
	 */
	private void initViewSize() {
		/* ȡ����Ļ�ֱ��ʴ�С */
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		displayWidth = dm.widthPixels;
		displayHeight = dm.heightPixels;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// Handle touch events here...
		ImageView view = (ImageView) v;

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// ��������ģʽ
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			Log.d(TAG, "mode=NONE");
			break;
		// ���ö�㴥��ģʽ
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;
		// ��ΪDRAGģʽ�������ƶ�ͼƬ
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				// ����λ��
				matrix.postTranslate(event.getX() - start.x, event.getX()
						- start.x);
			}
			// ��ΪZOOMģʽ�����㴥������
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d(TAG, "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					// �������ű�����ͼƬ�е�λ��
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		// Perform the transformation
		view.setImageMatrix(matrix);

		return true; // indicate event was handled
	}

	/*
	 * private void continueShow() { new Thread() { public void run() { try {
	 * Thread.sleep(3000); isContinue = true; mHandler.sendEmptyMessage(1); }
	 * catch (Exception e) { System.out.println(e.toString()); } } }.start(); }
	 * 
	 * private void goBack() { new Thread() { public void run() { try {
	 * Thread.sleep(3000); isContinue = false; mHandler.sendEmptyMessage(0); }
	 * catch (Exception e) { System.out.println(e.toString()); } } }.start(); }
	 * 
	 * 
	 * private Handler mHandler = new Handler() { public void
	 * handleMessage(Message msg) { if (msg.what == 1) { if (isContinue)
	 * mHandler.sendEmptyMessageDelayed(1, 100); } else if (msg.what == 0) { if
	 * (!isContinue){ mHandler.sendEmptyMessageDelayed(0, 100); finish(); }
	 * 
	 * } } };
	 */

	// �����ƶ�����
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	// �����е�λ��
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	// ��ť�����С����
	private void small() {

		int bmpWidth = mBitmap.getWidth();
		int bmpHeight = mBitmap.getHeight();

		scaleWidth = (float) (scaleWidth * ZOOM_OUT_SCALE);
		scaleHeight = (float) (scaleHeight * ZOOM_OUT_SCALE);

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		zoomedBMP = Bitmap.createBitmap(mBitmap, 0, 0, bmpWidth, bmpHeight,
				matrix, true);
		imageView.setImageBitmap(zoomedBMP);
		imageView.setOnTouchListener(this);

		/* ��ΪͼƬ�ŵ����ʱ�Ŵ�ť��disable����������Сʱ��������Ϊenable */
		zoomControls.setIsZoomInEnabled(true);
	}

	// ��ť����Ŵ���
	private void enlarge() {

		int bmpWidth = mBitmap.getWidth();
		int bmpHeight = mBitmap.getHeight();

		scaleWidth = (float) (scaleWidth * ZOOM_IN_SCALE);
		scaleHeight = (float) (scaleHeight * ZOOM_IN_SCALE);

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		zoomedBMP = Bitmap.createBitmap(mBitmap, 0, 0, bmpWidth, bmpHeight,
				matrix, true);
		imageView.setImageBitmap(zoomedBMP);
		imageView.setOnTouchListener(this);

		/* ����ٷŴ�ᳬ����Ļ��С���Ͱ�Button disable */
		if (scaleWidth * ZOOM_IN_SCALE * bmpWidth > bmpWidth * 3
				|| scaleHeight * ZOOM_IN_SCALE * bmpHeight > bmpWidth * 3
				|| scaleWidth * ZOOM_IN_SCALE * bmpWidth > displayWidth * 5
				|| scaleHeight * ZOOM_IN_SCALE * bmpHeight > displayHeight * 5) {
			zoomControls.setIsZoomInEnabled(false);
		} else {
			zoomControls.setIsZoomInEnabled(true);
		}
	}
}