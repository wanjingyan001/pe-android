package com.framework.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 模块：
 * 创建人：mars
 * 负责人：
 * 创建时间：2015/11/27 10:52
 * 备注：EditText 输入
 */
public class Watcher implements TextWatcher {

    int lenght;
    EditText editText;
    WatcherListener listener;

    public interface WatcherListener{
        void callback(boolean over);
    }

    public Watcher(EditText edit, int lenght, WatcherListener listener){
        this.editText = edit;
        this.lenght = lenght;
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Editable editable = editText.getText();

       if(editable.length() >= lenght)
        {
            listener.callback(true);
        }else{
            listener.callback(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}