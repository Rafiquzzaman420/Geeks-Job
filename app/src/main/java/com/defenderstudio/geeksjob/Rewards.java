package com.defenderstudio.geeksjob;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Rewards extends AppCompatActivity {

    private static long START_TIME_IN_MILLIS;
    int initialPointValue = 0;
    private long chancesLeft;
    private RewardedAd rewardedAd;

    private boolean dialogShown = false;

    Runnable statusChecker = () -> {
        try {
            Dialog dialog = new Dialog(Rewards.this, R.style.dialogue);
            dialog.setContentView(R.layout.connection_alert);
            dialog.setCancelable(false);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            internetConnectionCheckerWithServer(connection -> {
                if (connection) {
                    dialog.findViewById(R.id.connection_retry).setOnClickListener(v -> {
                        if (isOnline() && dialogShown) {
                            dialog.dismiss();
                            dialogShown = false;
                        }
                    });
                    // If Internet connection is gone
                }
                if (!isOnline() && !connection) {
                    if (!dialogShown) {
                        dialogShown = true;
                        dialog.show();
                    }
                }
            });


        } catch (Exception ignored) {
        } finally {
            int interval = 1000;
            new Handler().postDelayed(this::startConnectionRepeatingTask, interval);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        startConnectionRepeatingTask();

        ProgressDialog dialog = new ProgressDialog(Rewards.this,
                R.style.ProgressDialogStyle);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            rewardedAdLoader();
            dialog.dismiss();
        }, 5000);

        blockRewardButton();
        TextView chancesLeftText = findViewById(R.id.chancesLeft);
        userChancesLeftCallBack(value -> {
            chancesLeftText.setText(String.valueOf(value));
            chancesLeft = value;
            if (value == 0) {
                informationValidationAfterGettingZero();
            } else {
                START_TIME_IN_MILLIS = 28800000;

                // Loading the Rewarded Ads
                rewardedAdLoader();
                dialog.dismiss();
                freeRewardButton();
            }
        });

        MobileAds.initialize(this, initializationStatus -> rewardedAdLoader());

        Button rewardButton = findViewById(R.id.rewardedAdButton);
        rewardButton.setBackgroundColor(getResources().getColor(R.color.green));
        rewardButton.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(Rewards.this, R.style.ProgressDialogStyle);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading. Please wait...");

            try {
                progressDialog.show();
                new Handler().postDelayed(() -> {
                    adShow();
                    progressDialog.dismiss();
                }, 3000);
            } catch (Exception ignored) {
            }
        });
    }
    //==============================================================================================================================

    //==============================================================================================================================
    // Method to start the "Timer"
    //==============================================================================================================================
    // Method for validating information with server
    private void informationValidationAfterGettingZero() {
        userTimerInformationCallBack(value -> {
            ProgressDialog dialog = new ProgressDialog(Rewards.this, R.style.ProgressDialogStyle);
            TextView chancesLeftText = findViewById(R.id.chancesLeft);

            // Getting the current time in MILLIS
            long currentTime = Calendar.getInstance().getTimeInMillis();

            // If current time is less than the value from the server
            if ((currentTime - value) >= 0) {
                Button rewardButton = findViewById(R.id.rewardedAdButton);

                // Setting Reward button as Clickable
                rewardButton.setClickable(true);

                // Setting the Reward button's background color GREEN
                rewardButton.setBackgroundColor(getResources().getColor(R.color.green));

                // Sending the user chance left information to the server
                userChancesLeftSendToServer(20);

                // Setting the value of chances left to the textview
                chancesLeftText.setText(String.valueOf(20));
                chancesLeftText.invalidate();

                // Dismissing the Progress Dialog Window
                dialog.dismiss();

                // If current time is more than the value from the server
            } else if ((currentTime - value) < 0) {
                // Assigning the value to START_TIME_IN_MILLIS
                START_TIME_IN_MILLIS = value - currentTime;
                chancesLeftText.setText(String.valueOf(0));
                chancesLeftText.invalidate();
                new Handler().postDelayed(() -> {

                    // Starting the timer
                    startTimer(START_TIME_IN_MILLIS);

                    // Resetting the value of the timer
                    resetTimer();

                    // Dismissing the Progress Dialog Window
                    dialog.dismiss();

                }, 3000);
            } else {
                START_TIME_IN_MILLIS = 28800000;

                // Loading the Rewarded Ads
                rewardedAdLoader();
                dialog.dismiss();
            }
        });
    }


    private void startTimer(long timeLeftInMillis) {
        blockRewardButton();

        new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String duration = String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                TextView timerFunction = findViewById(R.id.timer);
                timerFunction.setText(duration);
            }

            @Override
            public void onFinish() {

                // Reset the timer
                resetTimer();
                // Setting the Reward button to "Clickable"
                freeRewardButton();

            }
        }.start();

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

    //==============================================================================================================================


    //==============================================================================================================================
    // Resetting the timer
    //==============================================================================================================================
    private void resetTimer() {
        userTimerInformationCallBack(value -> {
            // Getting the current time
            long currentTime = Calendar.getInstance().getTimeInMillis();
            // Checking if current time is greater than the stored time in server
            // If time is greater than 0, then it will set the timer start value to START_TIME_IN_MILLIS
            if ((value - currentTime) > 0) {
                START_TIME_IN_MILLIS = value - currentTime;
            }
            // Otherwise it'll set the starting time to 24 Hours or 86400000 milliseconds
            else {
                START_TIME_IN_MILLIS = 28800000;
            }
        });

    }
    //==============================================================================================================================

    //==============================================================================================================================
    // Updating Users point information in Client Side
    //==============================================================================================================================
    private void UserPointsValueUpdate() {
        TextView rewardsPointText = findViewById(R.id.rewards_text_view_point);
        rewardsPointText.setText(String.valueOf(initialPointValue));
        scoreUpdate();
        rewardsPointText.invalidate();
    }
    //==============================================================================================================================

    //==============================================================================================================================
    // Blocks only the Reward Ad Button
    //==============================================================================================================================
    private void blockRewardButton() {
        Button rewardButton = findViewById(R.id.rewardedAdButton);
        rewardButton.setClickable(false);
        rewardButton.setBackgroundColor(getResources().getColor(R.color.red));
    }
    //==============================================================================================================================


    //==============================================================================================================================
    // Frees only the Reward Ad Button
    //==============================================================================================================================

    private void freeRewardButton() {
        Button rewardButton = findViewById(R.id.rewardedAdButton);
        rewardButton.setClickable(true);
        rewardButton.setBackgroundColor(getResources().getColor(R.color.green));
    }
//==================================================================================================================================

    //==================================================================================================================================
    // Will load Rewards when called
//==================================================================================================================================
//    ca-app-pub-5052828179386026/8585274942
    // DUMMY ID : ca-app-pub-3940256099942544/5224354917
    private void rewardedAdLoader() {
        // TODO : NEED TO CHANGE THE REWARDED AD IN MAIN SECTION ID
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-5052828179386026/8585274942",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        rewardedAd = null;
                        Toast.makeText(getApplicationContext(), "Please try again",
                                Toast.LENGTH_LONG).show();
                    }

                    //==============================================================================================================
                    // Called when Ad is loaded successfully
                    //==============================================================================================================

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        Rewards.this.rewardedAd = rewardedAd;
                        Rewards.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                            }

                            //======================================================================================================
                            // If failed to show ad in fullscreen
                            //======================================================================================================
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            }

                            //======================================================================================================
                            // Called when Ad is dismissed
                            //======================================================================================================
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Rewards.this.rewardedAd = null;
                                rewardedAdLoader();
                                userChancesLeftCallBack(value -> chancesLeft = value);
                                TextView chancesLeftText = findViewById(R.id.chancesLeft);
                                chancesLeftText.setText(String.valueOf(chancesLeft - 1));
                                chancesLeftText.invalidate();
                                if ((chancesLeft - 1) >= 1) {
                                    chancesLeft--;
                                    userChancesLeftSendToServer(chancesLeft);
                                    ProgressDialog dialog = new ProgressDialog(Rewards.this,
                                            R.style.ProgressDialogStyle);
                                    dialog.setMessage("Loading...");
                                    dialog.setCancelable(false);
                                    dialog.show();
                                    new Handler().postDelayed(() -> {
                                        rewardedAdLoader();
                                        dialog.dismiss();
                                    }, 3000);
                                } else {
                                    userChancesLeftSendToServer(0);
                                    blockRewardButton();
                                    // Method to send timer information directly to the server
                                    timerTimeSendToServer();

                                    ProgressDialog dialog = new ProgressDialog(Rewards.this,
                                            R.style.ProgressDialogStyle);
                                    dialog.setMessage("Loading. Please wait...");
                                    dialog.setCancelable(false);
                                    dialog.show();
                                    new Handler().postDelayed(() -> {
                                        resetTimer();
                                        dialog.dismiss();
                                    }, 3000);
                                    startTimer(START_TIME_IN_MILLIS);
                                }
                            }
                        });
                    }
                });
    }

    //==============================================================================================================================
    // Shows Ad to the user
    //==============================================================================================================================
    private void adShow() {
        if (rewardedAd != null) {
            Activity activityContext = Rewards.this;
            rewardedAd.show(activityContext, rewardItem -> {

                DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                assert firebaseUser != null;

                DatabaseReference EarnedPointAmountReference = firebaseDatabase.child("AllUsers").
                        child("User").child(firebaseUser.getUid()).child("Earned_Point_Amount");

                EarnedPointAmountReference.setValue(ServerValue.increment(50));

                Toast.makeText(getApplicationContext(), "Rewards Received!",
                        Toast.LENGTH_SHORT).show();

                initialPointValue = initialPointValue + 50;
                UserPointsValueUpdate();
            });
        } else {
            Toast.makeText(getApplicationContext(), "Please try again",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //==============================================================================================================================
    // Invokes when Back button is pressed
    //==============================================================================================================================
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    super.onBackPressed();
                    Intent intent = new Intent(Rewards.this,
                            MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    //==============================================================================================================================
    // Send Ending Time information to the Firebase Server
    //==============================================================================================================================

    private void timerTimeSendToServer() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long timerTime = currentTime + 28800000;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference adTimerManagementReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference timerTimeSend = adTimerManagementReference.child("AllUsers").
                child("Users Timer").child("Users").child(firebaseUser.getUid()).child("End Time");

        timerTimeSend.setValue(timerTime);

    }

    //==============================================================================================================================
    // Retrieves Users Ending Time information from the Firebase Server
    //==============================================================================================================================
    private void userTimerInformationCallBack(Rewards.userInfoCallBack userInfoCallBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference userTimerReference = databaseReference.child("AllUsers").
                child("Users Timer").child("Users").child(firebaseUser.getUid()).child("End Time");
        userTimerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                try {
                    userInfoCallBack.userInfo(longValue);
                } catch (Exception e) {
                    int zero = 0;
                    longValue = (long) zero;
                    userInfoCallBack.userInfo(longValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userChancesLeftCallBack(Rewards.userChancesLeft userChancesLeft) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference userTimerReference = databaseReference.child("AllUsers").
                child("Users Chances Left").child("Users").child(firebaseUser.getUid()).child("Chances");
        userTimerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                try {
                    userChancesLeft.userChancesLeftMethod(longValue);
                } catch (Exception e) {
                    int twenty = 20;
                    longValue = (long) twenty;
                    userChancesLeft.userChancesLeftMethod(longValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userChancesLeftSendToServer(long chances) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference chancesLeft = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference chancesLeftReference = chancesLeft.child("AllUsers").
                child("Users Chances Left").child("Users").child(firebaseUser.getUid()).child("Chances");

        chancesLeftReference.setValue(chances);

    }

    private void scoreUpdate() {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;

        DatabaseReference userName = firebaseDatabase.child("AllUsers").
                child("Competition").child("UserList").
                child(Objects.requireNonNull((firebaseUser.getDisplayName() + " " +
                        firebaseUser.getUid().substring(firebaseUser.getUid().length() - 4)).
                        replace(".", " ").
                        replace("#", " ").
                        replace("$", " ").
                        replace("[", " ").
                        replace("]", " "))).child("userName");


        userName.setValue(Objects.requireNonNull(firebaseUser.getDisplayName()).replace(".", " ").
                replace("#", " ").
                replace("$", " ").
                replace("[", " ").
                replace("]", " "));

        DatabaseReference pointsValue = firebaseDatabase.child("AllUsers").
                child("Competition").child("UserList").
                child(Objects.requireNonNull(firebaseUser.getDisplayName()).replace(".", " ").
                        replace("#", " ").
                        replace("$", " ").
                        replace("[", " ").
                        replace("]", " ") + " " +
                        firebaseUser.getUid().substring(firebaseUser.getUid().length() - 4)).child("pointsValue");

        String photo = Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString();

        DatabaseReference userImageStringValue = firebaseDatabase.child("AllUsers").
                child("Competition").child("UserList").
                child(firebaseUser.getDisplayName().replace(".", " ").
                        replace("#", " ").
                        replace("$", " ").
                        replace("[", " ").
                        replace("]", " ") + " " +
                        firebaseUser.getUid().substring(firebaseUser.getUid().length() - 4)).child("imageUrl");


        userImageStringValue.setValue(photo);

        pointsValue.setValue(ServerValue.increment(50));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void internetConnectionCheckerWithServer(Rewards.internetConnectionCheck internetConnectionCheck) {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference AnsQuizAmountReference = firebaseDatabase.child("/.info/connected");
        AnsQuizAmountReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                try {
                    internetConnectionCheck.connectionInfo(connected);
                }catch (Exception e){
                    internetConnectionCheck.connectionInfo(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private interface userInfoCallBack {
        void userInfo(Long value);
    }


    private interface userChancesLeft {
        void userChancesLeftMethod(Long value);
    }

    private interface internetConnectionCheck {
        void connectionInfo(Boolean connection);
    }

}