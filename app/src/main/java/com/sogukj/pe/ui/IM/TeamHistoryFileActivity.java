package com.sogukj.pe.ui.IM;

import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sogukj.pe.Extras;
import com.sogukj.pe.R;
import com.sogukj.pe.service.Payload;
import com.sogukj.pe.util.Utils;
import com.sogukj.service.SoguApi;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import qdx.stickyheaderdecoration.GridDecoration;
import qdx.stickyheaderdecoration.NormalDecoration;

public class TeamHistoryFileActivity extends AppCompatActivity implements TeamMenuWindow.onItemClickListener {
    public static int Month = 1;
    public static int File = 2;
    private Toolbar toolbar;
    private RecyclerView historyList;
    private TeamMenuWindow window;
    private static int type = 8;//(1=>图片，2=>视频，3=>压缩包，4=>Excel档，5=>TXT，6=>PDF，7=>DOC，8=>全部，9=>其他)
    private int tid;
    private HistoryFileAdapter adapter;
    private List<ChatFileBean> files = new ArrayList<>();

    public static void start(Context context, int tid) {
        Intent intent = new Intent(context, TeamHistoryFileActivity.class);
        intent.putExtra(Extras.INSTANCE.getID(), tid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_history_file);
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff);
        toolbar = (Toolbar) findViewById(R.id.team_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.sogu_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        window = new TeamMenuWindow(this);
        window.setListener(this);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                window.showAsDropDown(toolbar);
                return true;
            }
        });
        tid = getIntent().getIntExtra(Extras.INSTANCE.getID(), 8);
        type = 8;
        historyList = (RecyclerView) findViewById(R.id.history_file_list);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryFileAdapter(files);
        NormalDecoration decoration = new NormalDecoration() {
            @Override
            public String getHeaderName(int i) {
                String s = files.get(i).getTime().substring(0, 8);
                s = s.replaceFirst("/", "年");
                s = s.replace("/", "月");
                return s;
            }
        };
        historyList.addItemDecoration(decoration);
        adapter.setListener(new onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                IMDialog dialog = new IMDialog(TeamHistoryFileActivity.this);
                dialog.setTitle(files.get(position).getFile_name());
                dialog.setOnItemClickListener(new IMDialog.IMItemClickListener() {

                    @Override
                    public void itemClick(int position) {
                        switch (position) {
                            case 1:
                                TeamSelectActivity.Companion.startForResult(TeamHistoryFileActivity.this,
                                        true,null,null,false,null);
                                break;
                            case 2:

                                break;
                            default:
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });
        historyList.setAdapter(adapter);
        requestChatFile();
    }

    private void requestChatFile() {
        SoguApi.Companion.getService(getApplication())
                .chatFile(type, tid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Payload<List<ChatFileBean>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Payload<List<ChatFileBean>> listPayload) {
                        Log.d("WJY", new Gson().toJson(listPayload));
                        if (listPayload.getPayload() != null && !listPayload.getPayload().isEmpty()) {
                            files.clear();
                            files.addAll(listPayload.getPayload());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_filter, menu);
        return true;
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                type = 7;
                break;
            case 1:
                type = 3;
                break;
            case 2:
                type = 4;
                break;
            case 3:
                type = 5;
                break;
            case 4:
                type = 6;
                break;
            case 5:
                type = 9;
                break;
            default:
                break;
        }
        window.dismiss();
        requestChatFile();
    }

    class HistoryFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ChatFileBean> datas;
        private onItemClickListener listener;

        public HistoryFileAdapter(List<ChatFileBean> datas) {
            this.datas = datas;
        }

        public void setListener(onItemClickListener listener) {
            this.listener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FileHolder(LayoutInflater.from(TeamHistoryFileActivity.this)
                    .inflate(R.layout.item_history_child, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FileHolder) {
                ChatFileBean fileBean = datas.get(position);
                if (listener != null) {
                    ((FileHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClick(v, position);
                        }
                    });
                }
                ((FileHolder) holder).fileName.setText(fileBean.getFile_name());
                ((FileHolder) holder).fileInfo.setText(fileBean.getSize() + "   " + fileBean.getUser_name() + "   " + fileBean.getTime());
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }


        class FileHolder extends RecyclerView.ViewHolder {
            private TextView fileName;
            private TextView fileInfo;
            private ImageView fileIcon;
            private View view;

            public FileHolder(View itemView) {
                super(itemView);
                view = itemView;
                fileIcon = ((ImageView) itemView.findViewById(R.id.file_img));
                fileName = ((TextView) itemView.findViewById(R.id.file_name));
                fileInfo = ((TextView) itemView.findViewById(R.id.file_info));
            }
        }
    }

    interface onItemClickListener {
        void onItemClick(View view, int position);
    }

}
