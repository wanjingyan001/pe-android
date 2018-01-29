package com.sogukj.pe.ui.IM;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sogukj.pe.R;
import com.sogukj.pe.util.Utils;

import java.util.ArrayList;

import qdx.stickyheaderdecoration.GridDecoration;

public class TeamPictureActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView pictureList;
    private ArrayList<TeamPic> teamPics;

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
        pictureList = (RecyclerView) findViewById(R.id.pictureList);
        teamPics = new ArrayList<>();
        int month = 1;
        for (int i = 0; i < 50; i++) {
            TeamPic tp = new TeamPic();
            if (i % 5 == 0) {
                month += 1;
            }
            tp.setDate("2017年" + month + "月");
            tp.setUrl("https://www.baidu.com/img/bd_logo1.png");
            teamPics.add(tp);
        }
        PictureAdapter adapter = new PictureAdapter();
        GridDecoration gridDecoration = new GridDecoration(teamPics.size(),3) {
            @Override
            public String getHeaderName(int i) {
                return teamPics.get(i).getDate();
            }
        };
        pictureList.addItemDecoration(gridDecoration);
        pictureList.setLayoutManager(new GridLayoutManager(this,3));
        pictureList.setAdapter(adapter);

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


    class TeamPic {
        private String date;
        private String url;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
