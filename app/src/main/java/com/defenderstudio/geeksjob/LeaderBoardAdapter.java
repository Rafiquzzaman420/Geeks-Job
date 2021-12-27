package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder> {

    Context context;
    ArrayList<LeaderBoardUser> list;
    View view;

    public LeaderBoardAdapter(Context context, ArrayList<LeaderBoardUser> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.leader_board, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        @SuppressLint("WorldReadableFiles")
        SharedPreferences shareInfo = this.context.getSharedPreferences("position", Context.MODE_PRIVATE);
        LeaderBoardUser leaderBoardUser = list.get(position);
        holder.userName.setText(leaderBoardUser.getUserName().toUpperCase());
        holder.pointsInfo.setText(String.valueOf(leaderBoardUser.getPointsValue()));
        String photoUrl = leaderBoardUser.getImageUrl();
        Glide.with(context).load(photoUrl).
                apply(RequestOptions.circleCropTransform()).into(holder.userImage);
        position++;

        if (position == 1) {
            trophyImageSetter(holder, R.drawable.gold_trophy);
            shareInfo.edit().putString("FIRST_URL", photoUrl).apply();
        }
        else if (position == 2) {
            trophyImageSetter(holder, R.drawable.silver_trophy);
            shareInfo.edit().putString("SECOND_URL", photoUrl).apply();
        }
        else if (position == 3) {
            trophyImageSetter(holder, R.drawable.bronze_trophy);
            shareInfo.edit().putString("THIRD_URL", photoUrl).apply();
        }
        else {
            holder.trophyImage.setVisibility(View.GONE);
            holder.userPosition.setVisibility(View.VISIBLE);
            holder.userPosition.setText(String.valueOf(position));
        }
    }

    private void trophyImageSetter(ViewHolder holder, int drawableResID) {
        holder.userPosition.setVisibility(View.GONE);
        holder.trophyImage.setVisibility(View.VISIBLE);
        Glide.with(context).load(drawableResID).
                apply(RequestOptions.circleCropTransform()).into(holder.trophyImage);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, pointsInfo, userPosition;
        ImageView userImage, trophyImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name_competition);
            pointsInfo = itemView.findViewById(R.id.user_points_competition);
            userImage = itemView.findViewById(R.id.LeaderBoardUserImage);
            userPosition = itemView.findViewById(R.id.user_position);
            trophyImage = itemView.findViewById(R.id.trophy);
        }
    }
}
