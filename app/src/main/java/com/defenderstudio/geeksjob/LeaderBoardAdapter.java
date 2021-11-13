package com.defenderstudio.geeksjob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder> {

    Context context;

    public LeaderBoardAdapter(Context context, ArrayList<LeaderBoardUser> list) {
        this.context = context;
        this.list = list;
    }

    ArrayList<LeaderBoardUser> list;

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

    }

    @Override
    public int getItemCount() {

        return list.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName, pointsInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name_competition);
            pointsInfo = itemView.findViewById(R.id.user_points_competition);

        }
    }
}
