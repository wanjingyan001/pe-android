package com.netease.nim.uikit.business.session.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;

import java.util.Locale;

/**
 * Created by admin on 2018/1/18.
 */

public class MsgViewHolderFile extends MsgViewHolderBase {
    private ImageView fileIcon;
    private TextView fileNameLabel;
    private TextView fileSize;
    private TextView download;
    private FileAttachment msgAttachment;

    public MsgViewHolderFile(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_message_file;
    }

    @Override
    protected void inflateContentView() {
        fileIcon = (ImageView) view.findViewById(R.id.message_item_file_icon_image);
        fileNameLabel = (TextView) view.findViewById(R.id.message_item_file_name_label);
        fileSize = (TextView) view.findViewById(R.id.message_item_file_size);
        download = (TextView) view.findViewById(R.id.download_file);
    }

    @Override
    protected void bindContentView() {
        msgAttachment = (FileAttachment) message.getAttachment();
        fileNameLabel.setText(msgAttachment.getDisplayName());
        String path = msgAttachment.getPath();
        if (!TextUtils.isEmpty(path)) {
            fileSize.setText(formatFileSize(msgAttachment.getSize(), SizeUnit.Auto));
        } else {
            AttachStatusEnum status = message.getAttachStatus();
            switch (status) {
                case def:
                    updateFileStatusLabel();
                    break;
                case transferring:
                    progressBar.setVisibility(View.VISIBLE);
                    int percent = (int) (getMsgAdapter().getProgress(message) * 100);
                    progressBar.setProgress(percent);
                    break;
                case transferred:
                case fail:
                    updateFileStatusLabel();
                    break;
                default:
                    break;
            }
        }
    }


    private void updateFileStatusLabel() {
        progressBar.setVisibility(View.GONE);
        fileSize.setText(formatFileSize(msgAttachment.getSize(), SizeUnit.Auto));
        // 下载状态
        String savePath = msgAttachment.getPathForSave();
        if (AttachmentStore.isFileExist(savePath)) {
            download.setText("已下载");
        } else {
            download.setText("下载");
        }
    }



    public static String formatFileSize(long size, SizeUnit unit) {
        if (size < 0) {
            return NimUIKit.getContext().getString(R.string.unknow_size);
        }

        final double KB = 1024;
        final double MB = KB * 1024;
        final double GB = MB * 1024;
        final double TB = GB * 1024;
        if (unit == SizeUnit.Auto) {
            if (size < KB) {
                unit = SizeUnit.Byte;
            } else if (size < MB) {
                unit = SizeUnit.KB;
            } else if (size < GB) {
                unit = SizeUnit.MB;
            } else if (size < TB) {
                unit = SizeUnit.GB;
            } else {
                unit = SizeUnit.TB;
            }
        }

        switch (unit) {
            case Byte:
                return size + "B";
            case KB:
                return String.format(Locale.US, "%.2fKB", size / KB);
            case MB:
                return String.format(Locale.US, "%.2fMB", size / MB);
            case GB:
                return String.format(Locale.US, "%.2fGB", size / GB);
            case TB:
                return String.format(Locale.US, "%.2fPB", size / TB);
            default:
                return size + "B";
        }
    }


    public enum SizeUnit {
        Byte,
        KB,
        MB,
        GB,
        TB,
        Auto,
    }
}
