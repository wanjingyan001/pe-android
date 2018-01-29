package com.sogukj.pe.ui.IM;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.sogukj.pe.R;
import com.sogukj.pe.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class TeamHistoryFileActivity extends AppCompatActivity {
    public static int Month = 1;
    public static int File = 2;
    private Toolbar toolbar;
    private RecyclerView historyList;
    private TeamMenuWindow window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_history_file);
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff);
        toolbar = (Toolbar) findViewById(R.id.team_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
        window = new TeamMenuWindow(this);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                window.showAsDropDown(toolbar);
                return true;
            }
        });

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add("");
        }
        historyList = (RecyclerView) findViewById(R.id.history_file_list);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        HistoryFileAdapter adapter = new HistoryFileAdapter(list);
        adapter.setListener(new onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CustomAlertDialog dialog = new CustomAlertDialog(TeamHistoryFileActivity.this);
                dialog.setTitle("海通创新.apk");
                dialog.addItem("发送给联系人", new CustomAlertDialog.onSeparateItemClickListener() {
                    @Override
                    public void onClick() {

                    }
                });
                dialog.addItem("分享链接", new CustomAlertDialog.onSeparateItemClickListener() {
                    @Override
                    public void onClick() {

                    }
                });
                dialog.addItem("取消", new CustomAlertDialog.onSeparateItemClickListener() {
                    @Override
                    public void onClick() {

                    }
                });
                dialog.show();
            }
        });
        historyList.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_filter, menu);
        return true;
    }

    class HistoryFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<String> datas;
        private onItemClickListener listener;

        public HistoryFileAdapter(List<String> datas) {
            this.datas = datas;
        }

        public void setListener(onItemClickListener listener) {
            this.listener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == Month) {
                return new HisHolder(LayoutInflater.from(TeamHistoryFileActivity.this)
                        .inflate(R.layout.item_history_parent, parent, false));
            } else {
                return new FileHolder(LayoutInflater.from(TeamHistoryFileActivity.this)
                        .inflate(R.layout.item_history_child, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FileHolder) {
                if (listener != null) {
                    ((FileHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClick(v, position);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 5 == 0) {
                return Month;
            } else {
                return File;
            }
        }

        class HisHolder extends RecyclerView.ViewHolder {

            public HisHolder(View itemView) {
                super(itemView);
            }
        }

        class FileHolder extends RecyclerView.ViewHolder {
            private View view;

            public FileHolder(View itemView) {
                super(itemView);
                view = itemView;
            }
        }
    }

    interface onItemClickListener {
        void onItemClick(View view, int position);
    }

}
