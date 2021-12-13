package com.defenderstudio.geeksjob;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Locale;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder> {

    Context context;
    ArrayList<LeaderBoardUser> list;

    public LeaderBoardAdapter(Context context, ArrayList<LeaderBoardUser> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_info_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LeaderBoardUser leaderBoardUser = list.get(position);
        holder.userName.setText(leaderBoardUser.getUserName().toUpperCase());
        holder.pointsInfo.setText(String.valueOf(leaderBoardUser.getPointsValue()));
        String photoUrl = leaderBoardUser.getImageUrl();
        Glide.with(context).load(photoUrl).
                apply(RequestOptions.circleCropTransform()).into(holder.userImage);
        position++;
        holder.userPosition.setText(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return list.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, pointsInfo, userPosition;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name_competition);
            pointsInfo = itemView.findViewById(R.id.user_points_competition);
            userImage = itemView.findViewById(R.id.LeaderBoardUserImage);
            userPosition = itemView.findViewById(R.id.user_position);
        }
    }
}
