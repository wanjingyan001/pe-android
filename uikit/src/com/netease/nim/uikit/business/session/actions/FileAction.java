package com.netease.nim.uikit.business.session.actions;

import android.content.Intent;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;

/**
 * Created by admin on 2018/2/5.
 */

public class FileAction extends BaseAction {

    /**
     * 构造函数
     *
     * @param iconResId 图标 res id
     * @param titleId   图标标题的string res id
     */
    public FileAction(int iconResId, int titleId) {
        super(iconResId, titleId);
    }

    @Override
    public void onClick() {
        MaterialFilePicker picker = new MaterialFilePicker();
        picker.withActivity(getActivity())
                .withTitle("内部存储")
                .withFilterDirectories(true)
                .withHiddenFiles(true)
                .withCloseMenu(false)
                .withRequestCode( makeRequestCode(RequestCode.GET_LOCAL_FILE))
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.GET_LOCAL_FILE:
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                File file = new File(path);
                IMMessage message = MessageBuilder.createFileMessage(getAccount(),
                        getSessionType(), file, file.getName());
                sendMessage(message);
                break;
            default:
                break;
        }
    }
}
