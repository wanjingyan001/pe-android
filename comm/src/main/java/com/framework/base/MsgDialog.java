package com.framework.base;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;

/**
 * @author Mars
 *
 */
public class MsgDialog {

	private static final int MAX_PROGRESS = 100;

	/** NOMESS ARETT
	 */
	public static AlertDialog alert(Activity activity, String title) {
		return new Builder(activity)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(title)
				.create();
	}

	/** NOMESS ARETT by theme
	 */
	public static AlertDialog alert(Activity activity, int theme, String title) {
		return new Builder(activity, theme)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(title)
				.create();
	}

	/** MESSAGE ARERT
	 */
	public static AlertDialog alert(Activity activity, String title, String sMsg) {
		return new Builder(activity)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(title)
				.setMessage(sMsg)
				.create();
	}

	/** MESSAGE ARERT by theme
	 */
	public static AlertDialog alert(Activity activity, int theme, String title, String sMsg) {
		return new Builder(activity)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(title)
				.setMessage(sMsg)
				.create();
	}

	/** alert list
	 */
	public static AlertDialog alertList(Activity activity, String title, String items[], DialogInterface.OnClickListener listener){
		return new Builder(activity)
				.setTitle(title)
				.setItems(items, listener)
      			.create();
	}

	/** 列表
	 */
	public static AlertDialog alertList(Activity activity, int theme, String title, String items[], DialogInterface.OnClickListener listener){
		return new Builder(activity, theme)
				.setTitle(title)
				.setItems(items, listener)
        		.create();
	}

	/** 列表
	 */
	public static AlertDialog alertSingleChoice(Activity activity, String title, int checkedItem, String items[], DialogInterface.OnClickListener listener){
		return new Builder(activity)
				.setTitle(title)
				.setSingleChoiceItems(items, checkedItem, listener)
        		.create();
	}

	/** 单选框
	 */
	public static AlertDialog alertSingleChoice(Activity activity, int theme, String title, int checkedItem, String items[], DialogInterface.OnClickListener listener){
		return new Builder(activity, theme)
				.setTitle(title)
				.setSingleChoiceItems(items, 0, listener)
        		.create();
	}

	/** 多选框
	 */
	public static AlertDialog alertMultipleChoice(Activity activity, String title, String items[], boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener){
		return new Builder(activity)
				.setTitle(title)
				.setMultiChoiceItems(items, checkedItems, listener)
				.create();
	}

	/** 多选框
	 */
	public static AlertDialog alertMultipleChoice(Activity activity, int theme, String title, String items[], boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener){
		return new Builder(activity, theme)
				.setTitle(title)
				.setMultiChoiceItems(items, checkedItems, listener)
        		.create();
	}

	/** progress
	 */
	public static ProgressDialog progress(Activity activity, String message){
		ProgressDialog mProgressDialog = new ProgressDialog(activity);
		mProgressDialog.setMessage(message);
		return mProgressDialog;
	}

	/** progress
	 */
	public static ProgressDialog progress(Activity activity, String message, int theme){
		ProgressDialog mProgressDialog = new ProgressDialog(activity, theme);
		mProgressDialog.setMessage(message);
		return mProgressDialog;
	}

	/** progress 进度条
	 */
	public static ProgressDialog progressHorizontal(Activity activity){
		ProgressDialog mProgressDialog = new ProgressDialog(activity);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(MAX_PROGRESS);
		return mProgressDialog;
	}

	/** progress by theme 进度条
	 */
	public static ProgressDialog progressHorizontal(Activity activity, int theme){
		ProgressDialog mProgressDialog = new ProgressDialog(activity, theme);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(MAX_PROGRESS);
		return mProgressDialog;
	}
}
