package com.defenderstudio.geeksjob;

import android.content.Context;
import android.util.Log;
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
        holder.userName.setText(leaderBoardUser.getUserName());
        holder.pointsInfo.setText(String.valueOf(leaderBoardUser.getPointsValue()));
        String photoUrl = leaderBoardUser.getImageUrl();
        Log.d("a", "user//// Got photo url from firebase. Url is : "+photoUrl);
        Glide.with(context).load(photoUrl).
                apply(RequestOptions.circleCropTransform()).into(holder.userImage);
        Log.d("a", "user//// Setting the image to user.");
    }

    @Override
    public int getItemCount() {
        return list.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, pointsInfo;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name_competition);
            pointsInfo = itemView.findViewById(R.id.user_points_competition);
            userImage = itemView.findViewById(R.id.LeaderBoardUserImage);
        }
    }
}
