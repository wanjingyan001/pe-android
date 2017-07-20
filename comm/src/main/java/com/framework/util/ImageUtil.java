package com.framework.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.text.Html;

/** 图片处理工具
 * @author Mars
 * 
 */
public class ImageUtil {

	/**
	 * 字节转bitmap
	 */
	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b != null) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * bitmap 转字节
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		if (bm == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * Drawable转bitmap
	 */
	public static Bitmap Drawable2Bitamp(Drawable drawable) {
		BitmapDrawable bd = (BitmapDrawable) drawable;
		return bd.getBitmap();
	}

	/**
	 * bitmap转文件
	 */
	public static boolean Bitmap2file(String file, Bitmap bmp, int quality) {
		CompressFormat format = CompressFormat.JPEG;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}


	/**
	 * 文件转bitmap
	 */
	public static Bitmap File2Bitmap(String file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			return BitmapFactory.decodeStream(fis);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 文件转bitmap根据分辨率
	 */
	public static Bitmap File2Bitmap(String file, int dw, int dh) {
		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		bmpFactoryOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, bmpFactoryOptions);
		try {
			int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
					/ (float) dh);
			int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
					/ (float) dw);

			if (heightRatio > 1 && widthRatio > 1) {
				bmpFactoryOptions.inSampleSize = heightRatio > widthRatio ? heightRatio
						: widthRatio;
			}
			bmpFactoryOptions.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(file, bmpFactoryOptions);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 文件转bitmap根据分辨率并且旋转为正方向
	 */
	public static Bitmap File2BitmapByTransform(String file, int dw, int dh) {

		//获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
		int degree = readImageDegree(file);
		Bitmap bitmap = File2Bitmap(file, dw, dh);

		//把图片旋转为正的方向
		return rotaingImageView(degree, bitmap);
	}

	/**
	 * 图片压缩处理
	 */
	public static void compressImage(String file,
			int dw, int dh, int quality) {
		
		Bitmap bitmap = File2BitmapByTransform(file, dw, dh);
		Bitmap2file(file, bitmap, quality);

		//回收资源
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}
	
	/**
	 * 图片加水印的方法
	 */
	public static boolean effectText(String file, String res[], int quality) {
		Bitmap bitmap = File2Bitmap(file);
		
		int width = bitmap.getWidth();
		int hight = bitmap.getHeight();

		Bitmap icon = Bitmap
				.createBitmap(width, hight, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(icon);
		Paint photoPaint = new Paint();
		photoPaint.setDither(true);
		photoPaint.setFilterBitmap(true);

		Rect src = new Rect(0, 0, width, hight);
		Rect dst = new Rect(0, 0, width, hight);
		canvas.drawBitmap(bitmap, src, dst, photoPaint);


		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
				| Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextSize(24.0f);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setColor(Color.RED);
		// textPaint.setShadowLayer(3f, 1,
		// 1,this.getResources().getColor(android.R.color.background_dark));

		int count = 0;
		for(String item : res){
			canvas.drawText(item , 0, (bitmap.getHeight() - count * 24), textPaint);
			count ++;
		}
		// 字，开始未知x,y采用那只笔绘制
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return Bitmap2file(file,icon,quality);
	}
	
	/**
	 * 获取图片的正方向
	 */
	public static int readImageDegree(String file) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(file);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		System.out.println("angle2=" + angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

}
