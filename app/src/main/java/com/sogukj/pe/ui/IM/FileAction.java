package com.sogukj.pe.ui.IM;

import android.content.Intent;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sogukj.pe.Extras;
import com.sogukj.pe.ui.approve.BuildSealActivity;
import com.sogukj.pe.ui.fileSelector.FileMainActivity;

import java.io.File;
import java.util.ArrayList;

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
//        MaterialFilePicker picker = new MaterialFilePicker();
//        picker.withActivity(getActivity())
//                .withTitle("内部存储")
//                .withFilterDirectories(true)
//                .withHiddenFiles(true)
//                .withCloseMenu(false)
//                .withRequestCode( makeRequestCode(RequestCode.GET_LOCAL_FILE))
//                .start();
        FileMainActivity.Companion.start(getActivity(),9,false,makeRequestCode(RequestCode.GET_LOCAL_FILE));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.GET_LOCAL_FILE:
//                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

                ArrayList<String> paths = data.getStringArrayListExtra(Extras.INSTANCE.getLIST());
                for (String path : paths) {
                    File file = new File(path);
                    IMMessage message = MessageBuilder.createFileMessage(getAccount(),
                            getSessionType(), file, file.getName());
                    sendMessage(message);
                }
                break;
            default:
                break;
        }
    }
}
