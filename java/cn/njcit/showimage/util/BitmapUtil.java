package cn.njcit.showimage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.Log;

import cn.njcit.showimage.meta.MetaData;

public class BitmapUtil {

	private static final String TAG = "BitmapUtil";

	public static Bitmap getOptionBitmap(String path) {

		File f = new File(path);
		Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		if (f.length() < 51200) { // 20-50k
			opts.inSampleSize = 2;
		} else if (f.length() < 307200) { // 50-300k
			opts.inSampleSize = 4;
		} else if (f.length() < 819200) { // 300-800k
			opts.inSampleSize = 6;
		} else if (f.length() < 1048576) { // 800-1024k
			opts.inSampleSize = 8;
		} else if (f.length() < 3145728) { // 1024-3072k
			opts.inSampleSize = 10;
		} else {
			opts.inSampleSize = 12;
		}

		try {
			Log.i(TAG, "file.length=" + f.length());
			resizeBmp = BitmapFactory.decodeFile(f.getPath(), opts);
		} catch (Exception err) {
			err.printStackTrace();
			Log.i(TAG,
					"BitmapFactory.decodeFile(f.getPath(),opts) has err!!!!!!");
		}

		return resizeBmp;
	}

	public static Bitmap getResizeBitmap(String path) {
		Bitmap mBitmap;
		try {
			mBitmap = BitmapFactory.decodeFile(path);
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
			mBitmap = getOptionBitmap(path);
		}

		if (mBitmap != null) {
			int bmpWidth = mBitmap.getWidth();
			int bmpHeight = mBitmap.getHeight();

			if (bmpWidth <= MetaData.screenWidth
					&& bmpHeight <= MetaData.screenHeight) {
				return mBitmap;
			}

			float scale;

			if (bmpWidth > MetaData.screenWidth) {
				scale = (float) MetaData.screenWidth / (float) bmpWidth;
			} else {
				scale = (float) MetaData.screenHeight / (float) bmpHeight;
			}

		/*
		 * if (isLandScape) { if ((float) bmpWidth > (float)
		 * MetaData.screenWidth) { scale = (float) MetaData.screenWidth /
		 * (float) bmpWidth; } else { scale = (float) MetaData.screenHeight /
		 * (float) bmpHeight; } } else { if ((float) bmpWidth > (float)
		 * MetaData.screenWidth) { scale = (float) MetaData.screenHeight /
		 * (float) bmpHeight; } else { scale = (float) MetaData.screenWidth /
		 * (float) bmpWidth; } }
		 */

		/* ����reSize���Bitmap���� */
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap resizeBitmap = Bitmap.createBitmap(mBitmap, 0, 0, bmpWidth,
					bmpHeight, matrix, true);

			return resizeBitmap;
		}else {
			return null;
		}
	}

	/**
	 * ����ͼƬ�ķ��� ���java.lang.outofmemoryerror : bitmap size exceeds vm budget����
	 * 
	 * @param filePath
	 * @return
	 */
	public static Bitmap fitSizePic(float size, String filePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options); // ��ʱ����bitmapΪ��
		options.inJustDecodeBounds = false;
		// ���ű�
		int proportion = (int) (options.outHeight / size);
		if (proportion <= 0) {
			proportion = 1;
		}

		options.inSampleSize = proportion;
		// ���¶���ͼƬ��ע�����Ҫ��options.inJustDecodeBounds ��Ϊ false
		options.inTempStorage = new byte[1024 * 1024 * 5]; // 5MB����ʱ�洢�ռ�
		// bitmap = BitmapFactory.decodeFile(filePath, options);
		try {
			bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(
					filePath)), null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	/**
	 * ��ӰЧ��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createReflectedImage(Bitmap originalBitmap) {
		// ͼƬ�뵹Ӱ�������
		final int reflectionGap = 10;

		// ͼƬ�Ŀ��
		int width = originalBitmap.getWidth();
		// ͼƬ�ĸ߶�
		int height = originalBitmap.getHeight();

		Matrix matrix = new Matrix();
		// ͼƬ���ţ�x���Ϊԭ����1����y��Ϊ-1��,ʵ��ͼƬ�ķ�ת
		matrix.preScale(1, -1);
		// ������ת���ͼƬBitmap����ͼƬ����ԭͼ��һ�롣
		Bitmap reflectionBitmap = Bitmap.createBitmap(originalBitmap, 0,
				height / 3, width, height / 3, matrix, false);
		// ������׼��Bitmap���󣬿��ԭͼһ�£�����ԭͼ��1.5����
		Bitmap withReflectionBitmap = Bitmap.createBitmap(width, (height
				+ height / 3 + reflectionGap), Config.ARGB_8888);

		// ���캯������Bitmap����Ϊ����ͼƬ�ϻ�ͼ
		Canvas canvas = new Canvas(withReflectionBitmap);
		// ��ԭʼͼƬ
		canvas.drawBitmap(originalBitmap, 0, 0, null);

		// ���������
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);

		// ����ӰͼƬ
		canvas.drawBitmap(reflectionBitmap, 0, height + reflectionGap, null);

		// ʵ�ֵ�ӰЧ��
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalBitmap.getHeight(), 0,
				withReflectionBitmap.getHeight(), 0x70ffffff, 0x00ffffff,
				TileMode.MIRROR);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// ����Ч��
		canvas.drawRect(0, height, width, withReflectionBitmap.getHeight(),
				paint);

		return withReflectionBitmap;
	}

	/**
	 * ��ƬЧ��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createPhotographicImage(Bitmap originalBitmap) {
		Bitmap effectedBitmap = null;

		try {
			int Width = originalBitmap.getWidth();
			int Height = originalBitmap.getHeight();
			int[] pixelBuf = new int[Width * Height];
			effectedBitmap = Bitmap.createBitmap(Width, Height,
					Config.ARGB_8888);
			effectedBitmap.getPixels(pixelBuf, 0, Width, 0, 0, Width, Height);
			for (int x = 0; x < Width; x++)
				for (int y = 0; y < Height; y++) {
					int r;
					int g;
					int b;
					int index = y * Width + x;
					r = 255 - (pixelBuf[index] >> 16) & 0xff;
					g = 255 - (pixelBuf[index] >> 8) & 0xff;
					b = 255 - (pixelBuf[index]) & 0xff;
					pixelBuf[index] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			effectedBitmap.setPixels(pixelBuf, 0, Width, 0, 0, Width, Height);
		} catch (Exception e) {
			Log.v(TAG, "Set Effects Fails!");
		}

		return effectedBitmap;
	}

	/**
	 * ��תЧ��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createRotateImage(int r, Bitmap originalBitmap) {
		int bmWidth = originalBitmap.getWidth();
		int bmHeight = originalBitmap.getHeight();
		Bitmap rotateBmp = null;

		Matrix matrix = new Matrix();

		try {
			matrix.postRotate(r);
			rotateBmp = Bitmap.createBitmap(originalBitmap, 0, 0, bmWidth,
					bmHeight, matrix, true);
		} catch (Exception err) {
			err.printStackTrace();
		}

		return rotateBmp;

	}

	/**
	 * ͼƬ��ת
	 * 
	 * @param bm
	 * @param flag
	 *            0Ϊˮƽ��ת��1Ϊ��ֱ��ת
	 * @return
	 */
	public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
		float[] floats = null;

		switch (flag) {
		case 0: // ˮƽ��ת
			floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
			break;
		case 1: // ��ֱ��ת
			floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
			break;
		}

		if (floats != null) {
			Matrix matrix = new Matrix();
			matrix.setValues(floats);
			return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
		}

		return bmp;
	}

	/**
	 * ��ƬЧ��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createNegativeImage(Bitmap bmp) {
		// RGBA�����ֵ
		final int MAX_VALUE = 255;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				newR = MAX_VALUE - pixR;
				newG = MAX_VALUE - pixG;
				newB = MAX_VALUE - pixB;

				newR = Math.min(MAX_VALUE, Math.max(0, newR));
				newG = Math.min(MAX_VALUE, Math.max(0, newG));
				newB = Math.min(MAX_VALUE, Math.max(0, newB));

				pixels[pos] = Color.argb(MAX_VALUE, newR, newG, newB);
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	// ģ��Ч��
	/**
	 * ģ��Ч��
	 * 
	 * @param bmp
	 * @return
	 */

	public Bitmap blurImage(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int newColor = 0;

		int[][] colors = new int[9][3];
		for (int i = 1, length = width - 1; i < length; i++) {
			for (int k = 1, len = height - 1; k < len; k++) {
				for (int m = 0; m < 9; m++) {
					int s = 0;
					int p = 0;
					switch (m) {
					case 0:
						s = i - 1;
						p = k - 1;
						break;
					case 1:
						s = i;
						p = k - 1;
						break;
					case 2:
						s = i + 1;
						p = k - 1;
						break;
					case 3:
						s = i + 1;
						p = k;
						break;
					case 4:
						s = i + 1;
						p = k + 1;
						break;
					case 5:
						s = i;
						p = k + 1;
						break;
					case 6:
						s = i - 1;
						p = k + 1;
						break;
					case 7:
						s = i - 1;
						p = k;
						break;
					case 8:
						s = i;
						p = k;
					}
					pixColor = bmp.getPixel(s, p);
					colors[m][0] = Color.red(pixColor);
					colors[m][1] = Color.green(pixColor);
					colors[m][2] = Color.blue(pixColor);
				}

				for (int m = 0; m < 9; m++) {
					newR += colors[m][0];
					newG += colors[m][1];
					newB += colors[m][2];
				}

				newR = (int) (newR / 9F);
				newG = (int) (newG / 9F);
				newB = (int) (newB / 9F);

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				newColor = Color.argb(255, newR, newG, newB);
				bitmap.setPixel(i, k, newColor);

				newR = 0;
				newG = 0;
				newB = 0;
			}
		}

		return bitmap;
	}

	/**
	 * �ữЧ��(��˹ģ��)
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createBlurImage(Bitmap bmp) {
		// ��˹����
		int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int delta = 16; // ֵԽСͼƬ��Խ����Խ����Խ��

		int idx = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + m) * width + k + n];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);

						newR = newR + (int) (pixR * gauss[idx]);
						newG = newG + (int) (pixG * gauss[idx]);
						newB = newB + (int) (pixB * gauss[idx]);
						idx++;
					}
				}

				newR /= delta;
				newG /= delta;
				newB /= delta;

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[i * width + k] = Color.argb(255, newR, newG, newB);

				newR = 0;
				newG = 0;
				newB = 0;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * ����Ч��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createNostalgiaImage(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		int pixColor = 0;
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				pixColor = pixels[width * i + k];
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
				newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
				newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
				int newColor = Color.argb(255, newR > 255 ? 255 : newR,
						newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
				pixels[width * i + k] = newColor;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * ����Ч��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createReliefImage(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				pixColor = pixels[pos + 1];
				newR = Color.red(pixColor) - pixR + 127;
				newG = Color.green(pixColor) - pixG + 127;
				newB = Color.blue(pixColor) - pixB + 127;

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[pos] = Color.argb(255, newR, newG, newB);
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * ͼƬ�񻯣�������˹�任��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createSharpenImage(Bitmap bmp) {
		long start = System.currentTimeMillis();
		// ������˹����
		int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int idx = 0;
		float alpha = 0.3F;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + n) * width + k + m];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);

						newR = newR + (int) (pixR * laplacian[idx] * alpha);
						newG = newG + (int) (pixG * laplacian[idx] * alpha);
						newB = newB + (int) (pixB * laplacian[idx] * alpha);
						idx++;
					}
				}

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[i * width + k] = Color.argb(255, newR, newG, newB);
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		long end = System.currentTimeMillis();
		Log.d("may", "used time=" + (end - start));
		return bitmap;
	}

	/**
	 * ����Ч��
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap createIlluminationImage(Bitmap bmp) {
		final int width = bmp.getWidth();
		final int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int centerX = width / 2;
		int centerY = height / 2;
		int radius = Math.min(centerX, centerY);

		final float strength = 150F; // ����ǿ�� 100~150
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				newR = pixR;
				newG = pixG;
				newB = pixB;

				// ���㵱ǰ�㵽�������ĵľ��룬ƽ������ϵ��������֮��ľ���
				int distance = (int) (Math.pow((centerY - i), 2) + Math.pow(
						centerX - k, 2));
				if (distance < radius * radius) {
					// ���վ����С�������ӵĹ���ֵ
					int result = (int) (strength * (1.0 - Math.sqrt(distance)
							/ radius));
					newR = pixR + result;
					newG = pixG + result;
					newB = pixB + result;
				}

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[pos] = Color.argb(255, newR, newG, newB);
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * �������
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getRectBitmap(Bitmap bitmap) {
		// ԭͼƬ�Ŀ��
		final int bitmapW = bitmap.getWidth();
		final int bitmapH = bitmap.getHeight();
		// ���¶����С
		Bitmap newBitmap = Bitmap.createBitmap(bitmapW + 16, bitmapH + 16, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		Paint p = new Paint();
		p.setColor(Color.argb(255, 178, 178, 178));
		canvas.drawRect(new Rect(0, 0, bitmapW + 18, bitmapH + 18), p);
		// ��ԭͼ
		canvas.drawBitmap(bitmap, 8, 8, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return newBitmap;
	}

	/**
	 * ��ȡ�̶��ߴ�ͼƬ
	 * 
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getResizedBitmap(float newWidth, float newHeight, Bitmap bitmap) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = newWidth / width;
		float scaleHeight = newHeight / height;
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return resizedBitmap;
	}

	/**
	 * �и�ͼƬ
	 * 
	 * @param bitmap
	 *            ԭʼͼƬ
	 * @param xPiece
	 *            �����и�Ƭ��
	 * @param yPiece
	 *            �����и�Ƭ��
	 * @return
	 */
	public static List<ImagePiece> split(Bitmap bitmap, int xPiece, int yPiece) {

		List<ImagePiece> pieces = new ArrayList<ImagePiece>(xPiece * yPiece);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int pieceWidth = width / 3;
		int pieceHeight = height / 3;
		for (int i = 0; i < yPiece; i++) {
			for (int j = 0; j < xPiece; j++) {
				ImagePiece piece = new ImagePiece();
				piece.index = j + i * xPiece;
				int xValue = j * pieceWidth;
				int yValue = i * pieceHeight;
				piece.bitmap = Bitmap.createBitmap(bitmap, xValue, yValue,
						pieceWidth, pieceHeight);
				pieces.add(piece);
			}
		}

		return pieces;
	}

	private static class ImagePiece {
		int index = 0;
		Bitmap bitmap = null;
	}
}
