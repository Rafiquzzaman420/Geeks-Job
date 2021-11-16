package com.defenderstudio.geeksjob;

import static com.defenderstudio.geeksjob.QuizActivity.curriculumList;
import static com.defenderstudio.geeksjob.QuizActivity.economyList;
import static com.defenderstudio.geeksjob.QuizActivity.historyList;
import static com.defenderstudio.geeksjob.QuizActivity.iotList;
import static com.defenderstudio.geeksjob.QuizActivity.moviesList;
import static com.defenderstudio.geeksjob.QuizActivity.religionList;
import static com.defenderstudio.geeksjob.QuizActivity.scienceList;
import static com.defenderstudio.geeksjob.QuizActivity.sportsList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    List<Questions> historyQuestionsList, curriculumQuestionsList, sportsQuestionsList, moviesQuestionsList,
            scienceQuestionsList, economyQuestionsList, religionQuestionsList, iotQuestionsList;
    Questions questions;
    int index = 0;
    long correctCount = 0;
    private InterstitialAd mInterstitialAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    private Handler adHandler;
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

    //==============================================================================================
//                                      onCreate() activity
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_ans_activity);
        overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);


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
        interstitialAdLoader();
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
        historyQuestionsList = historyList;
        economyQuestionsList = economyList;
        curriculumQuestionsList = curriculumList;
        religionQuestionsList = religionList;
        sportsQuestionsList = sportsList;
        moviesQuestionsList = moviesList;
        scienceQuestionsList = scienceList;
        iotQuestionsList = iotList;

// Shuffling all the questions taken from the server
        Collections.shuffle(historyQuestionsList);
        Collections.shuffle(economyQuestionsList);
        Collections.shuffle(curriculumQuestionsList);
        Collections.shuffle(sportsQuestionsList);
        Collections.shuffle(religionQuestionsList);
        Collections.shuffle(moviesQuestionsList);
        Collections.shuffle(scienceQuestionsList);
        Collections.shuffle(iotQuestionsList);

// Trying to get information from the server. If connection is slow then it'll show error Toast to
//        the user.
        quizCallFromFirebase(historyList);
        quizCallFromFirebase(economyList);
        quizCallFromFirebase(religionList);
        quizCallFromFirebase(sportsList);
        quizCallFromFirebase(scienceList);
        quizCallFromFirebase(iotList);
        quizCallFromFirebase(moviesList);
        quizCallFromFirebase(curriculumList);

    }

    void startRepeatingTask() {
        statusChecker.run();
    }


    private void interstitialAdLoader() {
        AdRequest adRequest = new AdRequest.Builder().build();
        MobileAds.initialize(this, initializationStatus -> {
            // TODO : Need to change the Ad ID here
            InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
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
        RewardedInterstitialAd.load(QuestionAnsActivity.this, "ca-app-pub-3940256099942544/5354046379",
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
                    }
                });
    }

    private void quizCallFromFirebase(ArrayList<Questions> arrayList) {
        try {
            questions = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            option1ClickMethod(arrayList);
            option2ClickMethod(arrayList);
            option3ClickMethod(arrayList);
            option4ClickMethod(arrayList);

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
    //==============================================================================================


    //==============================================================================================
    // Setting all the question taken from the server with the questions textview in the application
    //==============================================================================================
    private void setQuestionData() {

        question.setText(questions.getQuestion());
        option1.setText(questions.getOption1());
        option2.setText(questions.getOption2());
        option3.setText(questions.getOption3());
        option4.setText(questions.getOption4());
        submitButton.setClickable(false);

    }
    //==============================================================================================


    //==============================================================================================
    // If answer is correct, then this method will invoke
    //==============================================================================================
    public void correctAnswer(Button button, ArrayList<Questions> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                questions = arrayList.get(index);
                resetButtonColor();
                setQuestionData();
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

    public void wrongAnswer(Button button, ArrayList<Questions> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                questions = arrayList.get(index);
                resetButtonColor();
                setQuestionData();
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

    public void option1ClickMethod(ArrayList<Questions> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setQuestionData();
        option1.setOnClickListener(v -> {
            if (questions.getOption1().equals(questions.getAnswer())) {

                option1.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    correctAnswer(option1, arrayList);
                    disableButton();

                } else {
                    // This will work only when the user reaches the last Quiz section
                    option1.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    correctAnswer(option1, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                wrongAnswer(option1, arrayList);
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

    public void option2ClickMethod(ArrayList<Questions> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setQuestionData();
        option2.setOnClickListener(v -> {
            if (questions.getOption2().equals(questions.getAnswer())) {

                option2.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    correctAnswer(option2, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option2.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    correctAnswer(option2, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                wrongAnswer(option2, arrayList);
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

    public void option3ClickMethod(ArrayList<Questions> arrayList) {
        submitButton.setClickable(false);
        submitButton.getResources().getColor(R.color.red);
        setQuestionData();
        option3.setOnClickListener(v -> {

            if (questions.getOption3().equals(questions.getAnswer())) {

                option3.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    correctAnswer(option3, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option3.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    correctAnswer(option3, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                wrongAnswer(option3, arrayList);
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

    public void option4ClickMethod(ArrayList<Questions> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setQuestionData();
        option4.setOnClickListener(v -> {

            if (questions.getOption4().equals(questions.getAnswer())) {

                option4.setBackgroundColor(getResources().getColor(R.color.green));
                submitButton.setBackgroundColor(getResources().getColor(R.color.green));

                if (index < arrayList.size() - 1) {
                    submitButton.setClickable(false);
                    correctAnswer(option4, arrayList);
                    disableButton();


                } else {
                    // This will work only when the user reaches the last Quiz section
                    option4.setTextColor(getResources().getColor(R.color.white));
                    submitButton.setClickable(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                    correctAnswer(option4, arrayList);
                    disableButton();
                    submitButton.setClickable(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.green));
                    Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                            Toast.LENGTH_LONG).show();
                    backToQuizActivity(submitButton);
                }

            } else {
                wrongAnswer(option4, arrayList);
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

}