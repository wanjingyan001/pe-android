package com.sogukj.pe.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.UserBean;

/**
 * Created by admin on 2017/12/1.
 */

public class BusinessCardWindow extends PopupWindow {

    private View inflate;
    private ImageView headerImg;
    private TextView cardName;
    private TextView cardPosition;
    private TextView cardCompanyName;
    private TextView cardPhone;
    private TextView cardEmail;
    private TextView cardAddress;
    private Context context;

    public BusinessCardWindow(Activity context, View.OnClickListener listener) {
        super(context);
        this.context = context;
        initView(context, listener);
    }


    private void initView(final Activity context, View.OnClickListener listener) {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_card_window, null);
        headerImg = (ImageView) inflate.findViewById(R.id.headerImage);
        cardName = ((TextView) inflate.findViewById(R.id.cardName));
        cardPosition = ((TextView) inflate.findViewById(R.id.cardPosition));
        cardCompanyName = ((TextView) inflate.findViewById(R.id.cardCompanyName));
        cardPhone = ((TextView) inflate.findViewById(R.id.cardPhone));
        cardEmail = ((TextView) inflate.findViewById(R.id.cardEmail));
        cardAddress = ((TextView) inflate.findViewById(R.id.cardAddress));
        ImageView downloadCard = ((ImageView) inflate.findViewById(R.id.downloadCard));
        downloadCard.setOnClickListener(listener);
        this.setContentView(inflate);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);

    }

    public void setData(@NonNull UserBean bean) {
        if (!TextUtils.isEmpty(bean.getUrl())) {
            Glide.with(context)
                    .load(bean.headImage())
                    .into(headerImg);
        }
        cardName.setText(bean.getName());
        cardPosition.setText(bean.getPosition());
        cardCompanyName.setText(bean.getDepart_name());
        cardPhone.setText(bean.getPhone());
        cardEmail.setText(bean.getEmail());
        cardAddress.setText(bean.getMemo());
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);

        backgroundAlpha((Activity) context, 0.5f);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        backgroundAlpha((Activity) context, 1f);
    }
}
