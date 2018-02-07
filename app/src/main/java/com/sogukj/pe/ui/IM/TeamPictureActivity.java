package com.sogukj.pe.ui.IM;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
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

public class TeamPictureActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView pictureList;
    private ArrayList<ChatFileBean> teamPics;
    private PictureAdapter adapter;
    private ImageView iv_empty;
    private int tid;

    public static void start(Context context, int tid){
        Intent intent = new Intent(context, TeamPictureActivity.class);
        intent.putExtra(Extras.INSTANCE.getID(), tid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_picture);
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff);
        toolbar = (Toolbar) findViewById(R.id.team_toolbar);
        toolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tid = getIntent().getIntExtra(Extras.INSTANCE.getID(), 0);
        pictureList = (RecyclerView) findViewById(R.id.pictureList);
        iv_empty = ((ImageView) findViewById(R.id.iv_empty));
        teamPics = new ArrayList<>();
        adapter = new PictureAdapter();
        GridDecoration gridDecoration = new GridDecoration(teamPics.size(),3) {
            @Override
            public String getHeaderName(int i) {
                String s = teamPics.get(i).getTime().substring(0, 8);
                s = s.replaceFirst("/", "年");
                s = s.replace("/", "月");
                return s;
            }
        };
        pictureList.addItemDecoration(gridDecoration);
        pictureList.setLayoutManager(new GridLayoutManager(this,3));
        pictureList.setAdapter(adapter);
        requestChatFile();
    }


    private void requestChatFile() {
        SoguApi.Companion.getService(getApplication())
                .chatFile(1, tid)
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
                            teamPics.clear();
                            teamPics.addAll(listPayload.getPayload());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (adapter.getItemCount() == 0) {
                            iv_empty.setVisibility(View.VISIBLE);
                        }else {
                            iv_empty.setVisibility(View.GONE);
                        }
                    }
                });
    }


    class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureHolder> {


        @Override
        public PictureHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PictureHolder(LayoutInflater.from(TeamPictureActivity.this).inflate(R.layout.item_team_picture_list, parent, false));
        }

        @Override
        public void onBindViewHolder(PictureHolder holder, int position) {
            Glide.with(TeamPictureActivity.this)
                    .load(teamPics.get(position).getUrl())
                    .into(holder.picImg);
        }

        @Override
        public int getItemCount() {
            return teamPics.size();
        }

        class PictureHolder extends RecyclerView.ViewHolder {
            private ImageView picImg;

            public PictureHolder(View itemView) {
                super(itemView);
                picImg = (ImageView) itemView.findViewById(R.id.team_picture);
            }
        }
    }
}
