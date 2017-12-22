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

class TextViewClickObservableAdd extends Observable<Integer> {
    private TextView view;
    private ProgressBar bar;
    private Context context;
    private OptionsPickerView pvOptions;
    private ArrayList<Integer> mSelected;

    TextViewClickObservableAdd(Context context, TextView view, ProgressBar bar, int total, int offset) {
        this.context = context;
        this.view = view;
        this.bar = bar;
        mSelected = new ArrayList<Integer>();
        for (int i = 0; i <= total; i += offset) {
            mSelected.add(i);
        }
        bar.setMax(total);
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        Listener listener = new Listener(view, observer);
        observer.onSubscribe(listener);
        view.setOnClickListener(listener);
    }

    class Listener extends MainThreadDisposable implements View.OnClickListener {
        private final TextView view;
        private final Observer<? super Integer> observer;

        Listener(TextView view, Observer<? super Integer> observer) {
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
                        int pro = mSelected.get(options1);
                        bar.setProgress(pro);
                        bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_add));
                        view.setText(pro + "");
                        view.setTextColor(Color.parseColor("#ffa0a4aa"));
                        view.setTextSize(16);
                        view.setBackgroundDrawable(null);

                        observer.onNext(pro);
                    }
                }).build();
                pvOptions.setPicker(mSelected);
                pvOptions.show();
            }
        }

        @Override
        protected void onDispose() {
            view.setOnClickListener(null);
        }
    }
}