package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderBoard extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    LeaderBoardAdapter leaderBoardAdapter;
    ArrayList<LeaderBoardUser> leaderBoardUserArrayList;

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

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        LeaderBoardUser leaderBoardUser = dataSnapshot.getValue(LeaderBoardUser.class);
                        leaderBoardUserArrayList.add(leaderBoardUser);
                    }
                    Collections.reverse(leaderBoardUserArrayList);
                    leaderBoardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            progressDialog.dismiss();
        }, 5000);

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
    }
}