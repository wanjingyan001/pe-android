package com.framework.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.framework.R;


/**
 * Created by jin on 16/8/5.
 * 1、AlertDialog()构造器中获取手机参数
 * 2、builder()中获取控件、填充布局、设置长宽
 * 3、setTitle、setMsg、setCancelable、setPositiveButton、setNegativeButton
 * 4、在show()方法中setLayout设置布局文件即可
 */
public class IOSDialog {
    private Context context;
    private Dialog dialog;
    private LinearLayout lLayout_bg;
    private TextView tvTitle;
    private TextView tvContent;
    private Button btnLeft;
    private Button btnRight;
    private ImageView ivDivider;
    private Display display;
    private boolean showTitle = false;
    private boolean showMsg = false;
    private boolean showLeftBtn = false;
    private boolean showRightBtn = false;

    /**
     * 参数为context 用来获取手机参数
     *
     * @param context
     */
    public IOSDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public IOSDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_ios, null);

        // 根布局id
        lLayout_bg = (LinearLayout) view.findViewById(R.id.lLayout_bg);
        // 头部
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.GONE);
        // 消息体
        tvContent = (TextView) view.findViewById(R.id.tv_content);
        tvContent.setVisibility(View.GONE);
        // 取消
        btnLeft = (Button) view.findViewById(R.id.btn_left);
        btnLeft.setVisibility(View.GONE);
        // 退出
        btnRight = (Button) view.findViewById(R.id.btn_right);
        btnRight.setVisibility(View.GONE);
        // 按钮中间的线段
        ivDivider = (ImageView) view.findViewById(R.id.img_line);
        ivDivider.setVisibility(View.GONE);

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogStyle);
        // 填充Dialog布局！！！
        dialog.setContentView(view);

        // 调整dialog背景大小 LinearLayout.setLayoutParams
        // 必须使用FrameLayout.LayoutParams设置参数
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (display
                .getWidth() * 0.75), LinearLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    /**
     * 头部标题
     *
     * @param title
     * @return
     */
    public IOSDialog setTitle(String title) {
        showTitle = true;
        if ("".equals(title)) {
            tvTitle.setText("标题");
        } else {
            tvTitle.setText(title);
        }
        return this;
    }

    /**
     * 消息体
     *
     * @param msg
     * @return
     */
    public IOSDialog setMsg(String msg) {
        showMsg = true;
        if ("".equals(msg)) {
            tvContent.setText("内容");
        } else {
            tvContent.setText(msg);
        }
        return this;
    }

    /**
     * 取消窗口
     *
     * @param cancel
     * @return
     */
    public IOSDialog setCancelable(boolean cancel) {

        // dialog.setCancelable(cancel);或者用这个方法
        dialog.dismiss();
        return this;
    }

    /**
     * 确认按钮
     *
     * @param text
     * @param listener
     * @return
     */
    public IOSDialog setRightButton(String text,
                                    final View.OnClickListener listener) {
        showRightBtn = true;
        if ("".equals(text)) {
            btnRight.setText("确定");
        } else {
            btnRight.setText(text);
        }
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                dialog.dismiss();
            }
        });
        return this;
    }

    /**
     * 取消按钮
     *
     * @param text
     * @param listener
     * @return
     */
    public IOSDialog setLeftButton(String text,
                                   final View.OnClickListener listener) {
        showLeftBtn = true;
        if ("".equals(text)) {
            btnLeft.setText("取消");
        } else {
            btnLeft.setText(text);
        }
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                dialog.dismiss();
            }
        });
        return this;
    }

    /**
     * 设置布局文件
     */
    private void setLayout() {

        if (showTitle) {
            tvTitle.setVisibility(View.VISIBLE);
        }

        if (showMsg) {
            tvContent.setVisibility(View.VISIBLE);
        }

        if (showRightBtn && showLeftBtn) {
            btnRight.setVisibility(View.VISIBLE);
            btnRight.setBackgroundResource(R.drawable.ios_btn_right_selector);
            btnLeft.setVisibility(View.VISIBLE);
            btnLeft.setBackgroundResource(R.drawable.ios_btn_left_selector);
            ivDivider.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 显示dialog视图
     */
    public void show() {
        setLayout();
        dialog.show();
    }
}
