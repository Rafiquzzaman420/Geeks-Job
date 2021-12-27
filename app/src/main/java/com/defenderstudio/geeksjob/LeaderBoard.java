package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LeaderBoard extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    LeaderBoardAdapter leaderBoardAdapter;
    ArrayList<LeaderBoardUser> leaderBoardUserArrayList;
    ImageView firstUser, secondUser, thirdUser;
    TextView leaderBoardTimer;
    ConstraintLayout constraintLayout;
    ValueEventListener eventListener;
    private boolean dialogShown = false;
    Handler handler;

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

        handler = new Handler();

        startConnectionRepeatingTask();
        firstUser = findViewById(R.id.first_user);
        secondUser = findViewById(R.id.second_user);
        thirdUser = findViewById(R.id.third_user);
        leaderBoardTimer = findViewById(R.id.leaderBoardTimer);

        long currentTime = Calendar.getInstance().getTimeInMillis();
        getTimerInformation(time -> {
            if (time <= currentTime) {
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(LeaderBoard.this, SignInActivity.class);
                startActivity(signOutIntent);
                finish();
                Toast.makeText(getApplicationContext(), "Calculating Result. Please try again later...", Toast.LENGTH_LONG).show();
            } else {
                startTimer((time - currentTime));
            }
        });

        constraintLayout = findViewById(R.id.constraint_layout_leader_board);

        firstUser.getLayoutParams().height = (int) convertFromDp(200);
        secondUser.getLayoutParams().height = (int) convertFromDp(200);
        thirdUser.getLayoutParams().height = (int) convertFromDp(200);

        firstUser.getLayoutParams().width = (int) convertFromDp(200);
        secondUser.getLayoutParams().width = (int) convertFromDp(200);
        thirdUser.getLayoutParams().width = (int) convertFromDp(200);

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
                    progressDialog.dismiss();
                }
            });
            try {
                progressDialog.dismiss();
            } catch (Exception ignored) {
            }
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

    private void stopRepeatingTask(){
        handler.removeCallbacks(statusChecker);
    }

    private void getTimerInformation(getTimerInfo getTimerInfo) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("Tournament Timer").child("Time left");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long value = snapshot.getValue(Long.class);
                try {
                    getTimerInfo.getInfo(value);
                } catch (Exception e) {
                    getTimerInfo.getInfo((long) 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseReference.addValueEventListener(eventListener);
    }

    private void startTimer(long timeLeftInMillis) {

        new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String duration = String.format(Locale.ENGLISH, "%02d days %02d Hours %02d Minutes %02d Seconds",
                        TimeUnit.MILLISECONDS.toDays(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished) -
                                TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                leaderBoardTimer.setText(duration);
            }

            @Override
            public void onFinish() {
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(LeaderBoard.this, SignInActivity.class);
                startActivity(signOutIntent);
                finish();
                Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            }
        }.start();

    }

    @Override
    protected void onStop() {
        stopRepeatingTask();
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
        recyclerView.setAdapter(null);
        ProgressDialog progressDialog = new ProgressDialog(LeaderBoard.this, R.style.ProgressDialogStyle);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopRepeatingTask();
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
        recyclerView.setAdapter(null);
        ProgressDialog progressDialog = new ProgressDialog(LeaderBoard.this, R.style.ProgressDialogStyle);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    private interface getTimerInfo {
        void getInfo(Long time);
    }
}