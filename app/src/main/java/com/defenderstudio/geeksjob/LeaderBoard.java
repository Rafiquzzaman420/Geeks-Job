package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderBoard extends AppCompatActivity {
    // TODO : NEED TO DO SOME WORK IN HERE
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    LeaderBoardAdapter leaderBoardAdapter;
    ArrayList<LeaderBoardUser> leaderBoardUserArrayList;
    ImageView firstUser, secondUser, thirdUser;
    ConstraintLayout constraintLayout;
    private boolean dialogShown = false;
    Runnable statusChecker = () -> {
        try {
            Dialog dialog = new Dialog(LeaderBoard.this, R.style.dialogue);
            dialog.setContentView(R.layout.connection_alert);
            dialog.setCancelable(false);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

            dialog.findViewById(R.id.connection_retry).setOnClickListener(v -> {
                if (isOnline() && dialogShown) {
                    dialog.dismiss();
                    dialogShown = false;
                }
            });
            // If Internet connection is gone
            if (!isOnline()) {
                if (!dialogShown) {
                    dialogShown = true;
                    dialog.show();
                }
            }

        } catch (Exception ignored) {
        } finally {
            int interval = 1000;
            new Handler().postDelayed(this::startConnectionRepeatingTask, interval);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_activity);

        startConnectionRepeatingTask();
        firstUser = findViewById(R.id.first_user);
        secondUser = findViewById(R.id.second_user);
        thirdUser = findViewById(R.id.third_user);

        constraintLayout = findViewById(R.id.constraint_layout_leader_board);
        parameterSetter(constraintLayout);
try {
    firstUser.getLayoutParams().height = (int) convertFromDp(200);
    secondUser.getLayoutParams().height = (int) convertFromDp(200);
    thirdUser.getLayoutParams().height = (int) convertFromDp(200);

    firstUser.getLayoutParams().width = (int) convertFromDp(200);
    secondUser.getLayoutParams().width = (int) convertFromDp(200);
    thirdUser.getLayoutParams().width = (int) convertFromDp(200);
}catch (Exception ignored){}

        SharedPreferences infoGetter = getSharedPreferences("position", MODE_PRIVATE);
        String firstPosition = infoGetter.getString("FIRST_URL", null);
        String secondPosition = infoGetter.getString("SECOND_URL", null);
        String thirdPosition = infoGetter.getString("THIRD_URL", null);

        recyclerView = findViewById(R.id.competition_recycler_view);
        databaseReference = FirebaseDatabase.getInstance().
                getReference("AllUsers/Competition/UserList");

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ProgressDialog progressDialog = new ProgressDialog(LeaderBoard.this, R.style.ProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading. Please wait...");
        progressDialog.show();
        new Handler().postDelayed(() -> {
            leaderBoardUserArrayList = new ArrayList<>();

            leaderBoardAdapter = new LeaderBoardAdapter(LeaderBoard.this, leaderBoardUserArrayList);
            recyclerView.setAdapter(leaderBoardAdapter);

            databaseReference.orderByChild("pointsValue").addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    leaderBoardUserArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        LeaderBoardUser leaderBoardUser = dataSnapshot.getValue(LeaderBoardUser.class);
                        leaderBoardUserArrayList.add(leaderBoardUser);
                        assert leaderBoardUser != null;
                    }
                    Glide.with(getApplicationContext()).load(firstPosition).
                            apply(RequestOptions.circleCropTransform()).into(firstUser);
                    Glide.with(getApplicationContext()).load(secondPosition).
                            apply(RequestOptions.circleCropTransform()).into(secondUser);
                    Glide.with(getApplicationContext()).load(thirdPosition).
                            apply(RequestOptions.circleCropTransform()).into(thirdUser);

                    Collections.reverse(leaderBoardUserArrayList);
                    leaderBoardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            progressDialog.dismiss();
        }, 3000);

    }


    void startConnectionRepeatingTask() {
        statusChecker.run();
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }


    @Override
    public void onBackPressed() {
        Intent goBackIntent = new Intent(LeaderBoard.this, MainActivity.class);
        startActivity(goBackIntent);
        finish();
    }

    public float convertFromDp(int input) {
        final float scale = getResources().getDisplayMetrics().density;
        return ((input - 0.8f) / scale);
    }

    private void parameterSetter(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) convertFromDp(700);
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

}