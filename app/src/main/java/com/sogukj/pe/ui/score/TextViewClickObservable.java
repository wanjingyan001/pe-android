package com.sogukj.pe.ui.score;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.sogukj.pe.R;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by sogubaby on 2017/12/15.
 */

class TextViewClickObservable extends Observable<CharSequence> {
    private TextView view;
    private ProgressBar bar;
    private Context context;
    private OptionsPickerView pvOptions;
    private ArrayList<Integer> options1Items;

    TextViewClickObservable(Context context, TextView view, ProgressBar bar) {
        this.context = context;
        this.view = view;
        this.bar = bar;
        options1Items = new ArrayList<Integer>();
        for (int i = 0; i <= 120; i++) {
            options1Items.add(i);
        }
    }

    @Override
    protected void subscribeActual(Observer<? super CharSequence> observer) {
        Listener listener = new Listener(view, observer);
        observer.onSubscribe(listener);
        view.setOnClickListener(listener);
    }

    class Listener extends MainThreadDisposable implements View.OnClickListener {
        private final TextView view;
        private final Observer<? super CharSequence> observer;

        Listener(TextView view, Observer<? super CharSequence> observer) {
            this.view = view;
            this.observer = observer;
        }

        @Override
        public void onClick(View v) {
            if (!isDisposed()) {
                pvOptions = new OptionsPickerView.Builder(context, new OptionsPickerView.OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3, View v) {
                        //返回的分别是三个级别的选中位置
                        int pro = options1Items.get(options1);
                        bar.setProgress(pro);
                        if (pro >= 101 && pro <= 120) {
                            bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_a));
                        } else if (pro >= 81 && pro <= 100) {
                            bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_b));
                        } else if (pro >= 61 && pro <= 80) {
                            bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_c));
                        } else if (pro >= 0 && pro <= 60) {
                            bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_d));
                        }
                        view.setText(pro + "");
                        view.setTextColor(Color.parseColor("#ffa0a4aa"));
                        view.setTextSize(16);
                        view.setBackgroundDrawable(null);

                        observer.onNext("onClick");
                    }
                }).build();
                pvOptions.setPicker(options1Items);
                pvOptions.show();
            }
        }

        @Override
        protected void onDispose() {
            view.setOnClickListener(null);
        }
    }
}