package com.defenderstudio.geeksjob;

import static com.defenderstudio.geeksjob.QuizActivity.curriculumList;
import static com.defenderstudio.geeksjob.QuizActivity.moviesList;
import static com.defenderstudio.geeksjob.QuizActivity.religionList;
import static com.defenderstudio.geeksjob.QuizActivity.scienceList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class QuestionAnsActivity extends AppCompatActivity implements OnUserEarnedRewardListener {

    private static long START_TIME_IN_MILLIS;
    TextView topicName, question, score, total_earning;
    Button option1,
            option2,
            option3,
            option4,
            leaveButton,
            submitButton,
            hintBrowser,
            goBackFromBrowser,
            rewardedAdButton;
    WebView browserView;
    List<ScienceQuestion> scienceQuestionList;
    List<ReligionQuestion> religionQuestionList;
    List<MoviesQuestion> moviesQuestionList;
    List<CurriculumQuestion> curriculumQuestionList;

    MoviesQuestion moviesQuestion;
    ReligionQuestion religionQuestion;
    ScienceQuestion scienceQuestion;
    CurriculumQuestion curriculumQuestion;

    int index = 0;
    long correctCount = 0;
    private InterstitialAd mInterstitialAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    private Handler adHandler;
    private boolean dialogShown = false;

    Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                interstitialAdLoader();
            } finally {
                int interval = 300000;
                adHandler.postDelayed(statusChecker, interval);
            }
        }
    };
    Runnable connectionStatusChecker = () -> {
        try {
            Dialog dialog = new Dialog(QuestionAnsActivity.this, R.style.dialogue);
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
    //==============================================================================================
//                                      onCreate() activity
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_ans_activity);

        startConnectionRepeatingTask();

        adHandler = new Handler();
        startRepeatingTask();

        MobileAds.initialize(this, initializationStatus -> {
            rewardedAdButton = findViewById(R.id.rewardButton);
            rewardedAdButton.setOnClickListener(v -> loadAd());
        });

        //==============================================================================================
        // Hooking up all the views with ID's
        //==============================================================================================
        topicName = findViewById(R.id.topic_name);
        question = findViewById(R.id.question);
        score = findViewById(R.id.score);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        leaveButton = findViewById(R.id.leave);
        submitButton = findViewById(R.id.submit);
        hintBrowser = findViewById(R.id.question_hint);
        browserView = findViewById(R.id.browser_web_view);
        total_earning = findViewById(R.id.total_earning);
        AdView adView = findViewById(R.id.bannerAdView);


        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        topicName.setText(getIntent().getStringExtra("topicName"));

        ProgressDialog dialog = new ProgressDialog(QuestionAnsActivity.this, R.style.ProgressDialogStyle);
        dialog.setMessage("Loading. Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        userTimerInformationCallBack(value -> {
            // Getting the current time in MILLIS
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if ((currentTime - value) >= 0) {
                rewardedAdButton = findViewById(R.id.rewardButton);
                rewardedAdButton.setClickable(true);
                rewardedAdButton.setBackgroundColor(getResources().getColor(R.color.green));
                dialog.dismiss();
            } else if ((currentTime - value) < 0) {
                START_TIME_IN_MILLIS = value - currentTime;
                new Handler().postDelayed(() -> {
                    startTimer(START_TIME_IN_MILLIS);
                    resetTimer();
                    dialog.dismiss();
                }, 3000);
            } else {
                // Otherwise it'll set the starting time to 5 Minutes or 300000 milliseconds
                START_TIME_IN_MILLIS = 300000;
                dialog.dismiss();
            }
        });


        //==============================================================================================
        // Calling important methods from here
        //==============================================================================================

// Takes action when leave button or back button is clicked
        onLeaveButtonClicked();
// Takes action when hint button is clicked and opens a browser window with intent
        onHintClicked();


        //==============================================================================================
        // Getting Quiz from the firebase server
        //==============================================================================================

// Assigning list (Question) into ArrayList
        curriculumQuestionList = curriculumList;
        religionQuestionList = religionList;
        moviesQuestionList = moviesList;
        scienceQuestionList = scienceList;

// Shuffling all the questions taken from the server

        Collections.shuffle(curriculumQuestionList);
        Collections.shuffle(religionQuestionList);
        Collections.shuffle(scienceQuestionList);
        Collections.shuffle(moviesQuestionList);

// Trying to get information from the server. If connection is slow then it'll show error Toast to
//        the user.
//        religionQuizCallFromFirebase(religionList);
        // TODO: NEED TO PUT STRING EXTRA TO CALL THESE INFORMATION DAH!
        // Most important thing in this project I think
        String topicInfo = getIntent().getStringExtra("topicName");
        if (topicInfo.equals("Science & History")){
            scienceQuizCallFromFirebase(scienceList, topicInfo);
        }else if (topicInfo.equals("Movies & Sports")) {
            moviesQuizCallFromFirebase(moviesList, topicInfo);
        }
//        curriculumQuizCallFromFirebase(curriculumList);

    }

    void startConnectionRepeatingTask() {
        connectionStatusChecker.run();
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }


    void startRepeatingTask() {
        statusChecker.run();
    }
//ca-app-pub-5052828179386026/7359645574

    private void interstitialAdLoader() {
        AdRequest adRequest = new AdRequest.Builder().build();
        MobileAds.initialize(this, initializationStatus -> {
            // TODO : Need to change the Ad ID here
            InterstitialAd.load(this, "ca-app-pub-5052828179386026/7359645574", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            // TODO : Need to show the Ad only when the user makes a mistake
                            mInterstitialAd = interstitialAd;
                            if (mInterstitialAd != null) {
                                mInterstitialAd.show(QuestionAnsActivity.this);
                            } else {
                                Toast.makeText(getApplicationContext(), "Ad wasn't ready yet!",
                                        Toast.LENGTH_SHORT).show();
                            }
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when fullscreen content is dismissed.
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    // Called when fullscreen content failed to show.
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when fullscreen content is shown.
                                    // Make sure to set your reference to null so you don't
                                    // show it a second time.
                                    mInterstitialAd = null;
                                }
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            mInterstitialAd = null;
                        }
                    });
        });

    }

    public void loadAd() {
        // Use the test ad unit ID to load an ad.
        RewardedInterstitialAd.load(QuestionAnsActivity.this, "ca-app-pub-5052828179386026/3242847727",
                new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                        rewardedInterstitialAd = ad;
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            /** Called when the ad failed to show full screen content. */
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            }

                            /** Called when ad showed the full screen content. */
                            @Override
                            public void onAdShowedFullScreenContent() {
                            }

                            /** Called when full screen content is dismissed. */
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                blockRewardButton();
                                rewardInformationToServer();
                                timerTimeSendToServer();
                                ProgressDialog dialog = new ProgressDialog(QuestionAnsActivity.this,
                                        R.style.ProgressDialogStyle);
                                dialog.setMessage("Loading...");
                                dialog.setCancelable(false);
                                dialog.show();
                                new Handler().postDelayed(() -> {
                                    resetTimer();
                                    dialog.dismiss();
                                }, 3000);
                                startTimer(START_TIME_IN_MILLIS);
                            }
                        });
                        Activity activityContext = QuestionAnsActivity.this;
                        rewardedInterstitialAd.show(
                                activityContext,
                                rewardItem -> {
                                    // Handle the reward.
                                    Toast.makeText(getApplicationContext(), "Rewards Received!", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Dialog adNotAvailableDialog = new Dialog(QuestionAnsActivity.this, R.style.dialogue);
                        adNotAvailableDialog.setCancelable(false);
                        adNotAvailableDialog.setContentView(R.layout.ad_not_available_layout);
                        adNotAvailableDialog.show();
                        adNotAvailableDialog.findViewById(R.id.adNotAvailableButton).setOnClickListener(v ->
                                adNotAvailableDialog.dismiss());
                    }
                });
    }

    private void scienceQuizCallFromFirebase(ArrayList<ScienceQuestion> arrayList, String extraInfo) {
        try {
            scienceQuestion = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            scienceOption1ClickMethod(arrayList);
            scienceOption2ClickMethod(arrayList);
            scienceOption3ClickMethod(arrayList);
            scienceOption4ClickMethod(arrayList);

            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


//    private void religionQuizCallFromFirebase(ArrayList<ReligionQuestion> arrayList) {
//        try {
//            religionQuestion = arrayList.get(index);
//
//            // Calling the optionClick methods after assigning them with information from the server
//            religionOption1ClickMethod(arrayList);
//            religionOption2ClickMethod(arrayList);
//            religionOption3ClickMethod(arrayList);
//            religionOption4ClickMethod(arrayList);
//
//            // If fails to get data from server, then it'll try again and after that show
//            // connection error message
//        } catch (Exception ignored) {
//        }
//    }
//
//    private void curriculumQuizCallFromFirebase(ArrayList<CurriculumQuestion> arrayList) {
//        try {
//            curriculumQuestion = arrayList.get(index);
//
//            // Calling the optionClick methods after assigning them with information from the server
//            curriculumOption1ClickMethod(arrayList);
//            curriculumOption2ClickMethod(arrayList);
//            curriculumOption3ClickMethod(arrayList);
//            curriculumOption4ClickMethod(arrayList);
//
//            // If fails to get data from server, then it'll try again and after that show
//            // connection error message
//        } catch (Exception ignored) {
//        }
//    }


    private void moviesQuizCallFromFirebase(ArrayList<MoviesQuestion> arrayList, String extraInfo) {
        try {
            moviesQuestion = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            moviesOption1ClickMethod(arrayList);
            moviesOption2ClickMethod(arrayList);
            moviesOption3ClickMethod(arrayList);
            moviesOption4ClickMethod(arrayList);

            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


    //==============================================================================================
    // Method to update score directly to the server
    //==============================================================================================
    private void scoreUpdate() {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;

// Getting the reference of AnsQuizAmount from the firebase server
        DatabaseReference AnsQuizAmountReference = firebaseDatabase.child("AllUsers").
                child("User").child(firebaseUser.getUid()).child("Ans_Quiz_Amount");

        AnsQuizAmountReference.setValue(ServerValue.increment(1));

        DatabaseReference EarnedPointAmount = firebaseDatabase.child("AllUsers").
                child("User").child(firebaseUser.getUid()).child("Earned_Point_Amount");
        EarnedPointAmount.setValue(ServerValue.increment(10));
        //==============================================================================================

        // Firebase Database paths must not contain '.', '#', '$', '[', or ']'

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
                child(firebaseUser.getDisplayName().replace(".", " ").
                        replace("#", " ").
                        replace("$", " ").
                        replace("[", " ").
                        replace("]", " ") + " " +
                        firebaseUser.getUid().substring(firebaseUser.getUid().length() - 4)).child("pointsValue");

        pointsValue.setValue(ServerValue.increment(10));

        //==============================================================================================
        score.setText(String.valueOf(correctCount));
        score.invalidate();
    }

    //==============================================================================================
    // When leave button or back button is clicked, this button will invoke
    //==============================================================================================
    private void onLeaveButtonClicked() {
        leaveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(QuestionAnsActivity.this)
                    .setMessage("Are you sure you want to go back?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(QuestionAnsActivity.this,
                                QuizActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
            overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);
        });
    }

    //==============================================================================================
    // When Hint button is clicked, this will open a side browser from the drawer layout
    //==============================================================================================
    @SuppressLint({"RtlHardcoded", "SetJavaScriptEnabled"})
    private void onHintClicked() {
        hintBrowser.setOnClickListener(v -> {

            DrawerLayout browserDrawer = findViewById(R.id.question_ans_drawer_layout);
            if (browserDrawer.isDrawerOpen(Gravity.RIGHT))
                browserDrawer.closeDrawer(Gravity.RIGHT);
            else {
                browserDrawer.openDrawer(Gravity.RIGHT);
                Toast.makeText(getApplicationContext(),
                        "Loading. Please wait...", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),
                        "Swipe Right to go back!", Toast.LENGTH_LONG).show();
                goBackFromBrowser = findViewById(R.id.go_back_from_browser);
                goBackFromBrowser.setOnClickListener(v1 -> browserDrawer.closeDrawer(Gravity.RIGHT));
                browserView = findViewById(R.id.browser_web_view);
                browserView.setWebViewClient(new WebViewClient());
                browserView.getSettings().setJavaScriptEnabled(true);
                browserView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                browserView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                browserView.loadUrl("https://google.com");
            }
        });
    }

    //==============================================================================================


    //==============================================================================================
    // Takes action when back button is pressed. <Same as the leave button above>
    //==============================================================================================
    @Override
    public void onBackPressed() {
        browserView = findViewById(R.id.browser_web_view);
        if (browserView.canGoBack()) {
            browserView.goBack();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to go back?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(QuestionAnsActivity.this,
                                QuizActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
            overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);
        }
    }
    //==============================================================================================


    //==============================================================================================
    // Setting all the question taken from the server with the questions textview in the application
    //==============================================================================================
    private void setMoviesQuestionData() {
        question.setText(moviesQuestion.getMoviesQuestion());
        option1.setText(moviesQuestion.getMoviesOption1());
        option2.setText(moviesQuestion.getMoviesOption2());
        option3.setText(moviesQuestion.getMoviesOption3());
        option4.setText(moviesQuestion.getMoviesOption4());
        submitButton.setClickable(false);

    }

//    public boolean internetAvailabilityCheck(){
//        try{
//            InetAddress address = InetAddress.getByName("www.google.com");
//            return !address.equals("");
//        }catch (Exception exception){
//            return false;
//        }
//    }


    public boolean internetAvailabilityCheck(){
        Runtime runtime = Runtime.getRuntime();
            try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
                if (exitValue == 1){
                Toast.makeText(getApplicationContext(),
            "Please check your network connection...", Toast.LENGTH_LONG).show();
                }
            return (exitValue == 0);

        }
        catch (IOException | InterruptedException e)
        {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void setScienceQuestionData() {
        question.setText(scienceQuestion.getQuestion());
        option1.setText(scienceQuestion.getOption1());
        option2.setText(scienceQuestion.getOption2());
        option3.setText(scienceQuestion.getOption3());
        option4.setText(scienceQuestion.getOption4());
        submitButton.setClickable(false);

    }
    //==============================================================================================


    private void internetCheckerAndHandler(){
        Dialog dialog = new Dialog(QuestionAnsActivity.this, R.style.dialogue);
        dialog.setContentView(R.layout.connection_alert);
        dialog.setCancelable(false);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.findViewById(R.id.connection_retry).setOnClickListener(view -> {
                if (internetAvailabilityCheck() && dialogShown) {
                    dialogShown = false;
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Answer Submitted.", Toast.LENGTH_SHORT).show();
                }
        });
        // If Internet connection is gone
        if (!internetAvailabilityCheck()) {
            if (!dialogShown) {
                dialogShown = true;
                dialog.show();
            }
        }
    }

    //==============================================================================================
    // If answer is correct, then this method will invoke
    //==============================================================================================
    public void moviesCorrectAnswer(Button button, ArrayList<MoviesQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                ProgressDialog progressDialog = new ProgressDialog(QuestionAnsActivity.this, R.style.ProgressDialogStyle);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Submitting Answer...");
                progressDialog.show();
                new Handler().postDelayed(() -> {
                    index++;
                    internetCheckerAndHandler();
                    moviesQuestion = arrayList.get(index);
                    resetButtonColor();
                    setMoviesQuestionData();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    public void scienceCorrectAnswer(Button button, ArrayList<ScienceQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                ProgressDialog progressDialog = new ProgressDialog(QuestionAnsActivity.this, R.style.ProgressDialogStyle);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Submitting Answer...");
                progressDialog.show();
                new Handler().postDelayed(() -> {
                    index++;
                    scienceQuestion = arrayList.get(index);
                    resetButtonColor();
                    setScienceQuestionData();
                    enableButton();
                    internetCheckerAndHandler();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }


    public void curriculumCorrectAnswer(Button button, ArrayList<CurriculumQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                curriculumQuestion = arrayList.get(index);
                resetButtonColor();
                // TODO: NEED TO CHANGE THE NAME HERE
                setMoviesQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }


    public void religionCorrectAnswer(Button button, ArrayList<ReligionQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                religionQuestion = arrayList.get(index);
                resetButtonColor();
                // TODO: NEED TO CHANGE THE NAME HERE
                setMoviesQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }


    //==============================================================================================


    //==============================================================================================
    // Method to show Penalty Dialog when answer is wrong.
    //==============================================================================================
    private void penaltyDialogOnWrongAnswer() {
        ProgressDialog penaltyDialog = new ProgressDialog(QuestionAnsActivity.this,
                R.style.ProgressDialogStyle);
        penaltyDialog.setMessage("Penalty started...");
        penaltyDialog.show();
        penaltyDialog.setCancelable(false);
        new Handler().postDelayed(penaltyDialog::dismiss, 20000);
    }

    //==============================================================================================


    //==============================================================================================
    // If answer is wrong, then this method will invoke
    //==============================================================================================

    public void moviesWrongAnswer(Button button, ArrayList<MoviesQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                moviesQuestion = arrayList.get(index);
                resetButtonColor();
                setMoviesQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }


    public void scienceWrongAnswer(Button button, ArrayList<ScienceQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                scienceQuestion = arrayList.get(index);
                resetButtonColor();
                setScienceQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }


    public void religionWrongAnswer(Button button, ArrayList<ReligionQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                religionQuestion = arrayList.get(index);
                resetButtonColor();
                // TODO: NEED TO CHANGE THE NAME HERE
                setMoviesQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }


    public void curriculumWrongAnswer(Button button, ArrayList<CurriculumQuestion> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                curriculumQuestion = arrayList.get(index);
                resetButtonColor();
                // TODO: NEED TO CHANGE THE NAME HERE
                setMoviesQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }
    //==============================================================================================


    //==============================================================================================
    // When true, this method will enable all the question buttons
    //==============================================================================================
    public void enableButton() {
        option1.setClickable(true);
        option2.setClickable(true);
        option3.setClickable(true);
        option4.setClickable(true);
    }

    //==============================================================================================


    //==============================================================================================
    // When false, this method will disable all the question buttons
    //==============================================================================================

    public void disableButton() {
        option1.setClickable(false);
        option2.setClickable(false);
        option3.setClickable(false);
        option4.setClickable(false);
    }

    //==============================================================================================

    //==============================================================================================
    // Method to reset all the buttons background and text color when new question is called
    //==============================================================================================
    public void resetButtonColor() {
        option1.setBackgroundColor(Color.WHITE);
        option1.setTextColor(getResources().getColor(R.color.orange));
        option2.setBackgroundColor(Color.WHITE);
        option2.setTextColor(getResources().getColor(R.color.orange));
        option3.setBackgroundColor(Color.WHITE);
        option3.setTextColor(getResources().getColor(R.color.orange));
        option4.setBackgroundColor(Color.WHITE);
        option4.setTextColor(getResources().getColor(R.color.orange));
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
    }

    //==============================================================================================

    //==============================================================================================
    // When all question is answered, this method will be invoked
    //==============================================================================================
    public void backToQuizActivity(Button button) {
        button.setOnClickListener(v -> {
            Intent quizIntent = new Intent(QuestionAnsActivity.this, QuizActivity.class);
            startActivity(quizIntent);
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option1 button is clicked, this method will be invoked
    //==============================================================================================

    public void moviesOption1ClickMethod(ArrayList<MoviesQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setMoviesQuestionData();
        option1.setOnClickListener(v -> {
            if (moviesQuestion.getMoviesOption1().equals(moviesQuestion.getMoviesAnswer())) {

                option1.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    moviesCorrectAnswer(option1, arrayList);
                    disableButton();

                } else {
                    // This will work only when the user reaches the last Quiz section
                    option1.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    moviesCorrectAnswer(option1, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                moviesWrongAnswer(option1, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option2 button is clicked, this method will be invoked
    //==============================================================================================

    public void moviesOption2ClickMethod(ArrayList<MoviesQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setMoviesQuestionData();
        option2.setOnClickListener(v -> {
            if (moviesQuestion.getMoviesOption2().equals(moviesQuestion.getMoviesAnswer())) {

                option2.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    moviesCorrectAnswer(option2, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option2.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    moviesCorrectAnswer(option2, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                moviesWrongAnswer(option2, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option3 button is clicked, this method will be invoked
    //==============================================================================================

    public void moviesOption3ClickMethod(ArrayList<MoviesQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.getResources().getColor(R.color.red);
        setMoviesQuestionData();
        option3.setOnClickListener(v -> {

            if (moviesQuestion.getMoviesOption3().equals(moviesQuestion.getMoviesAnswer())) {

                option3.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    moviesCorrectAnswer(option3, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option3.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    moviesCorrectAnswer(option3, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                moviesWrongAnswer(option3, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option4 button is clicked, this method will be invoked
    //==============================================================================================

    public void moviesOption4ClickMethod(ArrayList<MoviesQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setMoviesQuestionData();
        option4.setOnClickListener(v -> {

            if (moviesQuestion.getMoviesOption4().equals(moviesQuestion.getMoviesAnswer())) {

                option4.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    moviesCorrectAnswer(option4, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option4.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    moviesCorrectAnswer(option4, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                moviesWrongAnswer(option4, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
        Toast.makeText(getApplicationContext(), "Reward received!", Toast.LENGTH_SHORT).show();

    }

    //==============================================================================================
//==============================================================================================
    private void userTimerInformationCallBack(userTimerInformation timerInformation) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference userTimerReference = databaseReference.child("AllUsers").
                child("Users Reward Timer").child("Users").child(firebaseUser.getUid()).child("End Time");
        userTimerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);

                try {
                    timerInformation.timerInfo(longValue);
                } catch (Exception e) {
                    int zero = 0;
                    longValue = (long) zero;
                    timerInformation.timerInfo(longValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void timerTimeSendToServer() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long timerTime = currentTime + 300000;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference adTimerManagementReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference timerTimeSend = adTimerManagementReference.child("AllUsers").
                child("Users Reward Timer").child("Users").child(firebaseUser.getUid()).child("End Time");

        timerTimeSend.setValue(timerTime);

    }

    private void startTimer(long timeLeftInMillis) {
        blockRewardButton();

        new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//                String duration = String.format(Locale.ENGLISH, "%02d:%02d:%02d",
//                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
//                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
//                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
//                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
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

    private void blockRewardButton() {
        rewardedAdButton = findViewById(R.id.rewardButton);
        rewardedAdButton.setClickable(false);
        rewardedAdButton.setBackgroundColor(getResources().getColor(R.color.red));
    }

    private void freeRewardButton() {
        rewardedAdButton = findViewById(R.id.rewardButton);
        rewardedAdButton.setClickable(true);
        rewardedAdButton.setBackgroundColor(getResources().getColor(R.color.green));
    }

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
                START_TIME_IN_MILLIS = 300000;
            }
        });

    }

    private interface userTimerInformation {
        void timerInfo(Long value);
    }


    private void rewardInformationToServer() {

        // This method will send the rewarded point information directly to the server and merge it down with the
        // answer point value
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;

        // Firebase Database paths must not contain '.', '#', '$', '[', or ']'

        DatabaseReference pointsValue = firebaseDatabase.child("AllUsers").
                child("Competition").child("UserList").
                child(Objects.requireNonNull(firebaseUser.getDisplayName()).replace(".", " ").
                        replace("#", " ").
                        replace("$", " ").
                        replace("[", " ").
                        replace("]", " ") + " " +
                        firebaseUser.getUid().substring(firebaseUser.getUid().length() - 4)).child("pointsValue");

        DatabaseReference EarnedPointAmount = firebaseDatabase.child("AllUsers").
                child("User").child(firebaseUser.getUid()).child("Earned_Point_Amount");

        pointsValue.setValue(ServerValue.increment(30));
        EarnedPointAmount.setValue(ServerValue.increment(30));

        //==============================================================================================
    }
    
    
    public void religionOption1ClickMethod(ArrayList<ReligionQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option1.setOnClickListener(v -> {
            // TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption1().equals(moviesQuestion.getMoviesAnswer())) {

                option1.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    religionCorrectAnswer(option1, arrayList);
                    disableButton();

                } else {
                    // This will work only when the user reaches the last Quiz section
                    option1.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    religionCorrectAnswer(option1, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                religionWrongAnswer(option1, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option2 button is clicked, this method will be invoked
    //==============================================================================================

    public void religionOption2ClickMethod(ArrayList<ReligionQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option2.setOnClickListener(v -> {
            // TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption2().equals(moviesQuestion.getMoviesAnswer())) {

                option2.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    religionCorrectAnswer(option2, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option2.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    religionCorrectAnswer(option2, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                religionWrongAnswer(option2, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option3 button is clicked, this method will be invoked
    //==============================================================================================

    public void religionOption3ClickMethod(ArrayList<ReligionQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.getResources().getColor(R.color.red);
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option3.setOnClickListener(v -> {
// TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption3().equals(moviesQuestion.getMoviesAnswer())) {

                option3.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    religionCorrectAnswer(option3, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option3.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    religionCorrectAnswer(option3, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                religionWrongAnswer(option3, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option4 button is clicked, this method will be invoked
    //==============================================================================================

    public void religionOption4ClickMethod(ArrayList<ReligionQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option4.setOnClickListener(v -> {
// TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption4().equals(moviesQuestion.getMoviesAnswer())) {

                option4.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    religionCorrectAnswer(option4, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option4.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    religionCorrectAnswer(option4, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                religionWrongAnswer(option4, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    
    
    public void scienceOption1ClickMethod(ArrayList<ScienceQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setScienceQuestionData();
        option1.setOnClickListener(v -> {
            if (scienceQuestion.getOption1().equals(scienceQuestion.getAnswer())) {

                option1.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    scienceCorrectAnswer(option1, arrayList);
                    disableButton();

                } else {
                    // This will work only when the user reaches the last Quiz section
                    option1.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    scienceCorrectAnswer(option1, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                scienceWrongAnswer(option1, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option2 button is clicked, this method will be invoked
    //==============================================================================================

    public void scienceOption2ClickMethod(ArrayList<ScienceQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setScienceQuestionData();
        option2.setOnClickListener(v -> {
            if (scienceQuestion.getOption2().equals(scienceQuestion.getAnswer())) {

                option2.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    scienceCorrectAnswer(option2, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option2.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    scienceCorrectAnswer(option2, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                scienceWrongAnswer(option2, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option3 button is clicked, this method will be invoked
    //==============================================================================================

    public void scienceOption3ClickMethod(ArrayList<ScienceQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.getResources().getColor(R.color.red);
        setScienceQuestionData();
        option3.setOnClickListener(v -> {

            if (scienceQuestion.getOption3().equals(scienceQuestion.getAnswer())) {

                option3.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    scienceCorrectAnswer(option3, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option3.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    scienceCorrectAnswer(option3, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                scienceWrongAnswer(option3, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option4 button is clicked, this method will be invoked
    //==============================================================================================

    public void scienceOption4ClickMethod(ArrayList<ScienceQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setScienceQuestionData();
        option4.setOnClickListener(v -> {

            if (scienceQuestion.getOption4().equals(scienceQuestion.getAnswer())) {

                option4.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    scienceCorrectAnswer(option4, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option4.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    scienceCorrectAnswer(option4, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                scienceWrongAnswer(option4, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    
    
    public void curriculumOption1ClickMethod(ArrayList<CurriculumQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option1.setOnClickListener(v -> {
            // TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption1().equals(moviesQuestion.getMoviesAnswer())) {

                option1.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    curriculumCorrectAnswer(option1, arrayList);
                    disableButton();

                } else {
                    // This will work only when the user reaches the last Quiz section
                    option1.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    curriculumCorrectAnswer(option1, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                curriculumWrongAnswer(option1, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option2 button is clicked, this method will be invoked
    //==============================================================================================

    public void curriculumOption2ClickMethod(ArrayList<CurriculumQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option2.setOnClickListener(v -> {
            // TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption2().equals(moviesQuestion.getMoviesAnswer())) {

                option2.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    curriculumCorrectAnswer(option2, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option2.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    curriculumCorrectAnswer(option2, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                curriculumWrongAnswer(option2, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option3 button is clicked, this method will be invoked
    //==============================================================================================

    public void curriculumOption3ClickMethod(ArrayList<CurriculumQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.getResources().getColor(R.color.red);
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option3.setOnClickListener(v -> {
// TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption3().equals(moviesQuestion.getMoviesAnswer())) {

                option3.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    curriculumCorrectAnswer(option3, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option3.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    curriculumCorrectAnswer(option3, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                curriculumWrongAnswer(option3, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    //==============================================================================================

    //==============================================================================================
    // When Option4 button is clicked, this method will be invoked
    //==============================================================================================

    public void curriculumOption4ClickMethod(ArrayList<CurriculumQuestion> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        // TODO: NEED TO CHANGE THE NAME HERE
        setMoviesQuestionData();
        option4.setOnClickListener(v -> {
// TODO: NEED TO CHANGE THE NAME HERE
            if (moviesQuestion.getMoviesOption4().equals(moviesQuestion.getMoviesAnswer())) {

                option4.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    curriculumCorrectAnswer(option4, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option4.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    curriculumCorrectAnswer(option4, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                curriculumWrongAnswer(option4, arrayList);
                submitButton.setClickable(false);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }


}