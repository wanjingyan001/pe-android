package com.netease.nim.uikit.business.session.viewholder;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.Locale;

/**
 * Created by admin on 2018/1/18.
 */

public class MsgViewHolderFile extends MsgViewHolderBase implements View.OnClickListener, DialogInterface.OnCancelListener {
    private ImageView fileIcon;
    private TextView fileNameLabel;
    private TextView fileSize;
    private TextView download;
    private FileAttachment msgAttachment;
    private ProgressDialog dialog;
    private AbortableFuture<Void> future;
    private ConstraintLayout fileLayout;

    public MsgViewHolderFile(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_message_file;
    }

    @Override
    protected void inflateContentView() {
        fileLayout = ((ConstraintLayout) view.findViewById(R.id.file_item_layout));
        fileIcon = (ImageView) view.findViewById(R.id.message_item_file_icon_image);
        fileNameLabel = (TextView) view.findViewById(R.id.message_item_file_name_label);
        fileSize = (TextView) view.findViewById(R.id.message_item_file_size);
        download = (TextView) view.findViewById(R.id.download_file);
        download.setOnClickListener(this);
        registerObservers(true);
    }

    @Override
    protected void bindContentView() {
        msgAttachment = (FileAttachment) message.getAttachment();
        fileNameLabel.setText(msgAttachment.getDisplayName());
        String path = msgAttachment.getPath();
        if (!TextUtils.isEmpty(path)) {
            fileSize.setText(formatFileSize(msgAttachment.getSize(), SizeUnit.Auto));
            download.setCompoundDrawables(null, null, null, null);
            download.setText("已下载");
            download.setEnabled(false);
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
        String savePath = msgAttachment.getPathForSave() + "." + msgAttachment.getExtension();
        if (AttachmentStore.isFileExist(savePath)) {
            download.setCompoundDrawables(null, null, null, null);
            download.setText("已下载");
            download.setEnabled(false);
        } else {
            download.setText("下载");
            download.setEnabled(true);
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

    private boolean isOriginDataHasDownloaded(final IMMessage message) {
        return !TextUtils.isEmpty(((FileAttachment) message.getAttachment()).getPath());
    }

    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeAttachmentProgress(attachmentProgressObserver, register);
        service.observeMsgStatus(statusObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message)) {
                return;
            }
            if (msg.getAttachStatus() == AttachStatusEnum.transferred && isOriginDataHasDownloaded(msg)) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                download.setText("已下载");
                download.setCompoundDrawables(null, null, null, null);
                download.setEnabled(false);
                registerObservers(false);
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                download.setText("下载");
                download.setEnabled(true);
            }
        }
    };


    private Observer<AttachmentProgress> attachmentProgressObserver = new Observer<AttachmentProgress>() {

        @Override
        public void onEvent(AttachmentProgress attachmentProgress) {
            long total = attachmentProgress.getTotal();
            long progress = attachmentProgress.getTransferred();
            float percent = (float) progress / (float) total;
            if (percent > 1.0) {
                // 消息中标识的文件大小有误，小于实际大小
                percent = (float) 1.0;
                progress = total;
            }
            if (dialog != null) {
                dialog.setMax((int) total);
                dialog.setProgress((int) progress);
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (isOriginDataHasDownloaded(message)) {
            return;
        }
        dialog = ProgressDialog.show(context, "", "正在下载", false, true, this);
        future = NIMClient.getService(MsgService.class).downloadAttachment(message, false);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (future.abort()) {
            Toast.makeText(context, "下载取消", Toast.LENGTH_SHORT).show();
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
