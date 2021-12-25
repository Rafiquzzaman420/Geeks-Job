package com.defenderstudio.geeksjob;

import static com.defenderstudio.geeksjob.Curriculum.competitionList;
import static com.defenderstudio.geeksjob.Curriculum.hscList;
import static com.defenderstudio.geeksjob.Curriculum.medicalList;
import static com.defenderstudio.geeksjob.Curriculum.sscList;
import static com.defenderstudio.geeksjob.Curriculum.universityList;
import static com.defenderstudio.geeksjob.QuizActivity.tournamentList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class QuestionAnsActivity extends AppCompatActivity implements OnUserEarnedRewardListener {

    private static long START_TIME_IN_MILLIS;
    private final String unityGameID = "4478761";
    private final String bannerPlacement = "Banner_Android";
    private final String interstitialPlacement = "Interstitial_Android";
    private final String rewardedPlacement = "Rewarded_Android";
    TextView topicName, question, score;
    Button option1,
            option2,
            option3,
            option4,
            leaveButton,
            submitButton,
            rewardTimer,
            rewardedAdButton;
    boolean TESTMODE = true;
    BannerView.IListener bannerListener;
    List<TournamentQuestions> tournamentQuestionsList;
    List<HSC> hscArrayList;
    List<SSC> sscArrayList;
    List<Medical> medicalArrayList;
    List<University> universityArrayList;
    List<Competition> competitionArrayList;
    TournamentQuestions tournamentQuestions;
    ProgressDialog progressDialog;
    HSC hsc;
    SSC ssc;
    Medical medical;
    University university;
    Competition competition;
    BannerView bannerAdView;
    LinearLayout bannerViewLayout;
    IUnityAdsListener unityAdsListener;
    IUnityAdsShowListener unityAdsShowListener;
    int index = 0;
    long correctCount = 0;
    private Handler handler;

    Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                interstitialAdLoader();
            } finally {
                int interval = 180000;
                handler.postDelayed(statusChecker, interval);
            }
        }
    };
    private boolean dialogShown = false;
    Runnable connectionStatusChecker = () -> {
        try {
            Dialog dialog = new Dialog(QuestionAnsActivity.this, R.style.dialogue);
            dialog.setContentView(R.layout.connection_alert);
            dialog.setCancelable(false);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            internetConnectionCheckerWithServer(connection -> {
                if (isOnline() && connection) {
                    dialog.findViewById(R.id.connection_retry).setOnClickListener(v -> {
                        if (dialogShown) {
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

    //==============================================================================================
//                                      onCreate() activity
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_ans_activity);

        startConnectionRepeatingTask();
        // TODO: NEED TO INITIALIZE AS MAIN AD HERE
        // Initializing Unity Ad
        UnityAds.initialize(this, unityGameID, TESTMODE, null);

        handler = new Handler();
        startRepeatingTask();

        rewardedAdButton = findViewById(R.id.rewardButton);
        rewardedAdButton.setTextSize(convertFromDp(30));
        submitButton = findViewById(R.id.submit);
        submitButton.setTextSize(convertFromDp(30));
        leaveButton = findViewById(R.id.leave);
        leaveButton.setTextSize(convertFromDp(30));
        leaveButton.setBackgroundColor(getResources().getColor(R.color.red));

        rewardedAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(QuestionAnsActivity.this, R.style.ProgressDialogStyle);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading. Please wait...");
                progressDialog.show();
                new Handler().postDelayed(() -> {
                    rewardedInterstitialAd();
                    progressDialog.dismiss();
                }, 3000);
            }
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

        topicName.setText(getIntent().getStringExtra("topicName"));

        ProgressDialog dialog = new ProgressDialog(QuestionAnsActivity.this, R.style.ProgressDialogStyle);
        dialog.setMessage("Loading. Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        userTimerInformationCallBack(value -> {
            // Getting the current time in MILLIS
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if ((currentTime - value) >= 0) {
                rewardTimer = findViewById(R.id.rewardTimer);
                rewardTimer.setVisibility(View.GONE);
                rewardedAdButton = findViewById(R.id.rewardButton);
                rewardedAdButton.setVisibility(View.VISIBLE);
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
                START_TIME_IN_MILLIS = 180000;
                dialog.dismiss();
            }
        });

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        bannerAdView = new BannerView(this, bannerPlacement, new UnityBannerSize(320, 70));
        bannerAdView.setListener(bannerListener);
        bannerViewLayout = findViewById(R.id.bannerAdView);
        bannerViewLayout.addView(bannerAdView);
        bannerAdView.load();


        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


        //==============================================================================================
        // Calling important methods from here
        //==============================================================================================

// Takes action when leave button or back button is clicked
        onLeaveButtonClicked();

        //==============================================================================================
        // Getting Quiz from the firebase server
        //==============================================================================================

// Assigning list (Question) into ArrayList
        tournamentQuestionsList = tournamentList;
        hscArrayList = hscList;
        sscArrayList = sscList;
        medicalArrayList = medicalList;
        universityArrayList = universityList;
        competitionArrayList = competitionList;

// Shuffling all the questions taken from the server

        Collections.shuffle(tournamentQuestionsList);
        String intentInfo = getIntent().getStringExtra("topicName");
        switch (intentInfo) {
            case "HSC":
                Collections.shuffle(hscArrayList);
                break;
            case "SSC":
                Collections.shuffle(sscArrayList);
                break;
            case "Medical":
                Collections.shuffle(medicalArrayList);
                break;
            case "University":
                Collections.shuffle(universityArrayList);
                break;
            case "Competition":
                Collections.shuffle(competitionArrayList);
                break;
        }
// Trying to get information from the server. If connection is slow then it'll show error Toast to
//        the user.
        // Most important thing in this project I think
        String topicInfo = getIntent().getStringExtra("topicName");
        switch (topicInfo) {
            case "Tournament":
                tournamentQuestionCall(tournamentList);
                break;
            case "HSC":
                hscQuestionCall(hscList);
                break;
            case "SSC":
                sscQuestionCall(sscList);
                break;
            case "Medical":
                medicalQuestionCall(medicalList);
                break;
            case "University":
                universityQuestionCall(universityList);
                break;
            case "Competition":
                competitionQuestionCall(competitionList);
                break;
        }

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

    private void interstitialAdLoader() {
        unityAdsListener = new IUnityAdsListener() {
            @Override
            public void onUnityAdsReady(String s) {
            }

            @Override
            public void onUnityAdsStart(String s) {
            }

            @Override
            public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {

            }

            @Override
            public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
            }
        };

        UnityAds.setListener(unityAdsListener);
        UnityAds.load(interstitialPlacement);

        if (UnityAds.isReady()) {
            UnityAds.show(QuestionAnsActivity.this, interstitialPlacement, unityAdsShowListener);
        }

    }

    private void rewardedInterstitialAd() {
        unityAdsListener = new IUnityAdsListener() {
            @Override
            public void onUnityAdsReady(String s) {

            }

            @Override
            public void onUnityAdsStart(String s) {

            }

            @Override
            public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
                if (finishState.equals(UnityAds.FinishState.COMPLETED)) {
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
                    Toast.makeText(getApplicationContext(), "Rewards received!",
                            Toast.LENGTH_SHORT).show();
                    startTimer(START_TIME_IN_MILLIS);
                }else if (finishState.equals(UnityAds.FinishState.SKIPPED)){
                    Toast.makeText(getApplicationContext(), "Reward not received for skipping ad!",
                            Toast.LENGTH_SHORT).show();
                }
                else if (finishState.equals(UnityAds.FinishState.ERROR)){
                    Toast.makeText(getApplicationContext(), "Something's not right!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {

            }
        };

        UnityAds.setListener(unityAdsListener);
        UnityAds.load(rewardedPlacement);

        if (UnityAds.isReady()) {
            UnityAds.show(QuestionAnsActivity.this, rewardedPlacement, unityAdsShowListener);
        }else{
            Toast.makeText(getApplicationContext(), "Ad not loaded yet!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void tournamentQuestionCall(ArrayList<TournamentQuestions> arrayList) {
        try {
            tournamentQuestions = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            tournamentClickMethod(arrayList);
            tournamentClickMethod(arrayList);
            tournamentClickMethod(arrayList);
            tournamentClickMethod(arrayList);


            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


    private void hscQuestionCall(ArrayList<HSC> arrayList) {
        try {
            hsc = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            hscClickMethod(arrayList);
            hscClickMethod(arrayList);
            hscClickMethod(arrayList);
            hscClickMethod(arrayList);

            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


    private void sscQuestionCall(ArrayList<SSC> arrayList) {
        try {
            ssc = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            sscClickMethod(arrayList);
            sscClickMethod(arrayList);
            sscClickMethod(arrayList);
            sscClickMethod(arrayList);

            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


    private void universityQuestionCall(ArrayList<University> arrayList) {
        try {
            university = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            universityClickMethod(arrayList);
            universityClickMethod(arrayList);
            universityClickMethod(arrayList);
            universityClickMethod(arrayList);

            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


    private void competitionQuestionCall(ArrayList<Competition> arrayList) {
        try {
            competition = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            competitionClickMethod(arrayList);
            competitionClickMethod(arrayList);
            competitionClickMethod(arrayList);
            competitionClickMethod(arrayList);

            // If fails to get data from server, then it'll try again and after that show
            // connection error message
        } catch (Exception ignored) {
        }
    }


    private void medicalQuestionCall(ArrayList<Medical> arrayList) {
        try {
            medical = arrayList.get(index);

            // Calling the optionClick methods after assigning them with information from the server
            medicalClickMethod(arrayList);
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
        pointsValue.setValue(ServerValue.increment(10));
        score.setText(String.valueOf(correctCount));
        score.invalidate();
        //==============================================================================================

    }

    private void internetConnectionCheckerWithServer(internetConnectionCheck internetConnectionCheck) {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference AnsQuizAmountReference = firebaseDatabase.child("/.info/connected");
        AnsQuizAmountReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                try {
                    internetConnectionCheck.connectionInfo(connected);
                } catch (Exception e) {
                    internetConnectionCheck.connectionInfo(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //==============================================================================================
    // When leave button or back button is clicked, this button will invoke
    //==============================================================================================
    private void onLeaveButtonClicked() {
        leaveButton.setOnClickListener(v -> new AlertDialog.Builder(QuestionAnsActivity.this)
                .setMessage("Are you sure you want to go back?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(QuestionAnsActivity.this,
                            QuizActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show());
    }

    //==============================================================================================
    // When Hint button is clicked, this will open a side browser from the drawer layout
    //==============================================================================================
    //==============================================================================================
    // Takes action when back button is pressed. <Same as the leave button above>
    //==============================================================================================
    @Override
    public void onBackPressed() {
        String topicInfo = getIntent().getStringExtra("topicName");
        if (topicInfo.equals("Tournament")) {
            try {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to go back?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(QuestionAnsActivity.this,
                                    QuizActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();

            } catch (Exception ignored) {
            }
        } else {
            try {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to go back?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(QuestionAnsActivity.this,
                                    Curriculum.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();

            } catch (Exception ignored) {
            }
        }
    }

    private void setHSCQuestionData() {
        question.setText(hsc.getQuestion());
        option1.setText(hsc.getOption1());
        option2.setText(hsc.getOption2());
        option3.setText(hsc.getOption3());
        option4.setText(hsc.getOption4());
        submitButton.setClickable(false);

    }


    private void setSSCQuestionData() {
        question.setText(ssc.getQuestion());
        option1.setText(ssc.getOption1());
        option2.setText(ssc.getOption2());
        option3.setText(ssc.getOption3());
        option4.setText(ssc.getOption4());
        submitButton.setClickable(false);

    }


    private void setMedicalQuestionData() {
        question.setText(medical.getQuestion());
        option1.setText(medical.getOption1());
        option2.setText(medical.getOption2());
        option3.setText(medical.getOption3());
        option4.setText(medical.getOption4());
        submitButton.setClickable(false);

    }


    private void setUniversityQuestionData() {
        question.setText(university.getQuestion());
        option1.setText(university.getOption1());
        option2.setText(university.getOption2());
        option3.setText(university.getOption3());
        option4.setText(university.getOption4());
        submitButton.setClickable(false);

    }


    private void setCompetitionQuestionData() {
        question.setText(competition.getQuestion());
        option1.setText(competition.getOption1());
        option2.setText(competition.getOption2());
        option3.setText(competition.getOption3());
        option4.setText(competition.getOption4());
        submitButton.setClickable(false);

    }

    private void setTournamentQuestions() {
        question.setText(tournamentQuestions.getQuestion());
        option1.setText(tournamentQuestions.getOption1());
        option2.setText(tournamentQuestions.getOption2());
        option3.setText(tournamentQuestions.getOption3());
        option4.setText(tournamentQuestions.getOption4());
        submitButton.setClickable(false);

    }

    //==============================================================================================
    // If answer is correct, then this method will invoke
    //==============================================================================================
    public void setHscCorrectAnswer(Button button, ArrayList<HSC> arrayList) {
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
                    hsc = arrayList.get(index);
                    resetButtonColor();
                    setHSCQuestionData();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    public void setSscCorrectAnswer(Button button, ArrayList<SSC> arrayList) {
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
                    ssc = arrayList.get(index);
                    resetButtonColor();
                    setSSCQuestionData();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    public void setMedicalCorrectAnswer(Button button, ArrayList<Medical> arrayList) {
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
                    medical = arrayList.get(index);
                    resetButtonColor();
                    setMedicalQuestionData();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    public void setUniversityCorrectAnswer(Button button, ArrayList<University> arrayList) {
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
                    university = arrayList.get(index);
                    resetButtonColor();
                    setUniversityQuestionData();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    public void setCompetitionCorrectAnswer(Button button, ArrayList<Competition> arrayList) {
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
                    competition = arrayList.get(index);
                    resetButtonColor();
                    setCompetitionQuestionData();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    public void tournamentCorrectAnswer(Button button, ArrayList<TournamentQuestions> arrayList) {
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
                    tournamentQuestions = arrayList.get(index);
                    resetButtonColor();
                    setTournamentQuestions();
                    enableButton();
                    scoreUpdate();
                    progressDialog.dismiss();
                    // If Internet connection is gone
                    // If Internet connection is gone
                }, 1000);

            } else {
                disableButton();
            }
        });
    }

    //==============================================================================================
    // Method to show Penalty Dialog when answer is wrong.
    //==============================================================================================
    private void penaltyDialogOnWrongAnswer() {
        ProgressDialog penaltyDialog = new ProgressDialog(QuestionAnsActivity.this,
                R.style.ProgressDialogStyle);
        penaltyDialog.setMessage("Penalty started...");
        penaltyDialog.show();
        penaltyDialog.setCancelable(false);
        new Handler().postDelayed(penaltyDialog::dismiss, 10000);
    }

    public void tournamentWrongAnswer(Button button, ArrayList<TournamentQuestions> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                tournamentQuestions = arrayList.get(index);
                resetButtonColor();
                setTournamentQuestions();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }


    public void setHscWrongAnswer(Button button, ArrayList<HSC> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                hsc = arrayList.get(index);
                resetButtonColor();
                setHSCQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }

    public void setSscWrongAnswer(Button button, ArrayList<SSC> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                ssc = arrayList.get(index);
                resetButtonColor();
                setSSCQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }

    public void setMedicalWrongAnswer(Button button, ArrayList<Medical> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                medical = arrayList.get(index);
                resetButtonColor();
                setMedicalQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }

    public void setUniversityWrongAnswer(Button button, ArrayList<University> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                university = arrayList.get(index);
                resetButtonColor();
                setUniversityQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }

    public void setCompetitionWrongAnswer(Button button, ArrayList<Competition> arrayList) {
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setClickable(false);
        button.setTextColor(getResources().getColor(R.color.white));
        submitButton.setOnClickListener(v -> {
            correctCount++;
            if ((index < arrayList.size() - 1)) {
                index++;
                competition = arrayList.get(index);
                resetButtonColor();
                setCompetitionQuestionData();
                enableButton();
                scoreUpdate();

            } else {
                disableButton();
            }
        });
    }

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

    public void disableButton() {
        option1.setClickable(false);
        option2.setClickable(false);
        option3.setClickable(false);
        option4.setClickable(false);
    }

    //==============================================================================================


    //==============================================================================================
    // When false, this method will disable all the question buttons
    //==============================================================================================

    //==============================================================================================
    // Method to reset all the buttons background and text color when new question is called
    //==============================================================================================
    public void resetButtonColor() {
        option1.setBackgroundColor(Color.WHITE);
        option1.setTextColor(getResources().getColor(R.color.blue));
        option2.setBackgroundColor(Color.WHITE);
        option2.setTextColor(getResources().getColor(R.color.blue));
        option3.setBackgroundColor(Color.WHITE);
        option3.setTextColor(getResources().getColor(R.color.blue));
        option4.setBackgroundColor(Color.WHITE);
        option4.setTextColor(getResources().getColor(R.color.blue));
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
            finish();
        });
    }

    //==============================================================================================

    public void hscClickMethod(ArrayList<HSC> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setHSCQuestionData();
        option1.setOnClickListener(v -> {
            if (hsc.getOption1().equals(hsc.getAnswer())) {
                setHscButtonClickMethod(option1, arrayList);
            } else {
                setHscWrongAnswer(option1, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option2.setOnClickListener(view -> {
            if (hsc.getOption2().equals(hsc.getAnswer())) {
                setHscButtonClickMethod(option2, arrayList);

            } else {
                setHscWrongAnswer(option2, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option3.setOnClickListener(view -> {
            if (hsc.getOption3().equals(hsc.getAnswer())) {
                setHscButtonClickMethod(option3, arrayList);

            } else {
                setHscWrongAnswer(option3, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option4.setOnClickListener(view -> {
            if (hsc.getOption4().equals(hsc.getAnswer())) {
                setHscButtonClickMethod(option4, arrayList);

            } else {
                setHscWrongAnswer(option4, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }


    public void sscClickMethod(ArrayList<SSC> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setSSCQuestionData();
        option1.setOnClickListener(v -> {
            if (ssc.getOption1().equals(ssc.getAnswer())) {
                setSscButtonClickMethod(option1, arrayList);
            } else {
                setSscWrongAnswer(option1, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option2.setOnClickListener(view -> {
            if (ssc.getOption2().equals(ssc.getAnswer())) {
                setSscButtonClickMethod(option2, arrayList);

            } else {
                setSscWrongAnswer(option2, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option3.setOnClickListener(view -> {
            if (ssc.getOption3().equals(ssc.getAnswer())) {
                setSscButtonClickMethod(option3, arrayList);

            } else {
                setSscWrongAnswer(option3, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option4.setOnClickListener(view -> {
            if (ssc.getOption4().equals(ssc.getAnswer())) {
                setSscButtonClickMethod(option4, arrayList);

            } else {
                setSscWrongAnswer(option4, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }


    public void medicalClickMethod(ArrayList<Medical> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setMedicalQuestionData();
        // TODO : NEED TO DO SOME WORK HERE!
        option1.setOnClickListener(v -> {
            if (medical.getOption1().equals(medical.getAnswer())) {
                setMedicalButtonClickMethod(option1, arrayList);
            } else {
                setMedicalWrongAnswer(option1, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option2.setOnClickListener(view -> {
            if (medical.getOption2().equals(medical.getAnswer())) {
                setMedicalButtonClickMethod(option2, arrayList);

            } else {
                setMedicalWrongAnswer(option2, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option3.setOnClickListener(view -> {
            if (medical.getOption3().equals(medical.getAnswer())) {
                setMedicalButtonClickMethod(option3, arrayList);

            } else {
                setMedicalWrongAnswer(option3, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option4.setOnClickListener(view -> {
            if (medical.getOption4().equals(medical.getAnswer())) {
                setMedicalButtonClickMethod(option4, arrayList);

            } else {
                setMedicalWrongAnswer(option4, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }


    public void universityClickMethod(ArrayList<University> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setUniversityQuestionData();
        option1.setOnClickListener(v -> {
            if (university.getOption1().equals(university.getAnswer())) {
                setUniversityButtonClickMethod(option1, arrayList);
            } else {
                setUniversityWrongAnswer(option1, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option2.setOnClickListener(view -> {
            if (university.getOption2().equals(university.getAnswer())) {
                setUniversityButtonClickMethod(option2, arrayList);

            } else {
                setUniversityWrongAnswer(option2, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option3.setOnClickListener(view -> {
            if (university.getOption3().equals(university.getAnswer())) {
                setUniversityButtonClickMethod(option3, arrayList);

            } else {
                setUniversityWrongAnswer(option3, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option4.setOnClickListener(view -> {
            if (university.getOption4().equals(university.getAnswer())) {
                setUniversityButtonClickMethod(option4, arrayList);

            } else {
                setUniversityWrongAnswer(option4, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }


    public void competitionClickMethod(ArrayList<Competition> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setCompetitionQuestionData();
        option1.setOnClickListener(v -> {
            if (competition.getOption1().equals(competition.getAnswer())) {
                setCompetitionButtonClickMethod(option1, arrayList);
            } else {
                setCompetitionWrongAnswer(option1, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option2.setOnClickListener(view -> {
            if (competition.getOption2().equals(competition.getAnswer())) {
                setCompetitionButtonClickMethod(option2, arrayList);

            } else {
                setCompetitionWrongAnswer(option2, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option3.setOnClickListener(view -> {
            if (competition.getOption3().equals(competition.getAnswer())) {
                setCompetitionButtonClickMethod(option3, arrayList);

            } else {
                setCompetitionWrongAnswer(option3, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option4.setOnClickListener(view -> {
            if (competition.getOption4().equals(competition.getAnswer())) {
                setCompetitionButtonClickMethod(option4, arrayList);

            } else {
                setCompetitionWrongAnswer(option4, arrayList);
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
        long timerTime = currentTime + 180000;

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
                String duration = String.format(Locale.ENGLISH, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                rewardedAdButton = findViewById(R.id.rewardButton);
                rewardedAdButton.setVisibility(View.GONE);
                rewardTimer = findViewById(R.id.rewardTimer);
                rewardTimer.setVisibility(View.VISIBLE);
                rewardTimer.setText(duration);
                rewardTimer.invalidate();
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
        rewardedAdButton.setVisibility(View.GONE);
        rewardTimer = findViewById(R.id.rewardTimer);
        rewardTimer.setClickable(false);
        rewardTimer.setBackgroundColor(getResources().getColor(R.color.red));
    }

    private void freeRewardButton() {
        rewardTimer = findViewById(R.id.rewardTimer);
        rewardTimer.setVisibility(View.GONE);
        rewardedAdButton = findViewById(R.id.rewardButton);
        rewardedAdButton.setVisibility(View.VISIBLE);
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
                START_TIME_IN_MILLIS = 180000;
            }
        });

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

        String photo = Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString();

        DatabaseReference userImageStringValue = firebaseDatabase.child("AllUsers").
                child("Competition").child("UserList").
                child(firebaseUser.getDisplayName().replace(".", " ").
                        replace("#", " ").
                        replace("$", " ").
                        replace("[", " ").
                        replace("]", " ") + " " +
                        firebaseUser.getUid().substring(firebaseUser.getUid().length() - 4)).child("imageUrl");

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


        userImageStringValue.setValue(photo);

        pointsValue.setValue(ServerValue.increment(30));
        EarnedPointAmount.setValue(ServerValue.increment(30));

        //==============================================================================================
    }

    public void tournamentClickMethod(ArrayList<TournamentQuestions> arrayList) {
        submitButton.setClickable(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.red));
        setTournamentQuestions();
        option1.setOnClickListener(v -> {
            if (tournamentQuestions.getOption1().equals(tournamentQuestions.getAnswer())) {
                setTournamentButtonClickMethod(option1, arrayList);
            } else {
                tournamentWrongAnswer(option1, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option2.setOnClickListener(view -> {
            if (tournamentQuestions.getOption2().equals(tournamentQuestions.getAnswer())) {
                setTournamentButtonClickMethod(option2, arrayList);

            } else {
                tournamentWrongAnswer(option2, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option3.setOnClickListener(view -> {
            if (tournamentQuestions.getOption3().equals(tournamentQuestions.getAnswer())) {
                setTournamentButtonClickMethod(option3, arrayList);

            } else {
                tournamentWrongAnswer(option3, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
        option4.setOnClickListener(view -> {
            if (tournamentQuestions.getOption4().equals(tournamentQuestions.getAnswer())) {
                setTournamentButtonClickMethod(option4, arrayList);

            } else {
                tournamentWrongAnswer(option4, arrayList);
                submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                penaltyDialogOnWrongAnswer();
                interstitialAdLoader();
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopRepeatingTask();
        super.onDestroy();
    }

    public float convertFromDp(int input) {
        final float scale = getResources().getDisplayMetrics().density;
        return ((input - 0.8f) / scale);
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(statusChecker);
    }

    private void setMedicalButtonClickMethod(Button button, ArrayList<Medical> arrayList) {

        button.setBackgroundColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundColor(getResources().getColor(R.color.green));

        if (index < arrayList.size() - 1) {
            submitButton.setClickable(false);
            setMedicalCorrectAnswer(button, arrayList);
            disableButton();

        } else {
            // This will work only when the user reaches the last Quiz section
            button.setTextColor(getResources().getColor(R.color.white));
            submitButton.setClickable(false);
            submitButton.setBackgroundColor(getResources().getColor(R.color.red));
            setMedicalCorrectAnswer(button, arrayList);
            disableButton();
            submitButton.setClickable(true);
            submitButton.setBackgroundColor(getResources().getColor(R.color.green));
            Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                    Toast.LENGTH_LONG).show();
            backToQuizActivity(submitButton);
        }
    }

    private void setUniversityButtonClickMethod(Button button, ArrayList<University> arrayList) {

        button.setBackgroundColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundColor(getResources().getColor(R.color.green));

        if (index < arrayList.size() - 1) {
            submitButton.setClickable(false);
            setUniversityCorrectAnswer(button, arrayList);
            disableButton();

        } else {
            // This will work only when the user reaches the last Quiz section
            button.setTextColor(getResources().getColor(R.color.white));
            submitButton.setClickable(false);
            submitButton.setBackgroundColor(getResources().getColor(R.color.red));
            setUniversityCorrectAnswer(button, arrayList);
            disableButton();
            submitButton.setClickable(true);
            submitButton.setBackgroundColor(getResources().getColor(R.color.green));
            Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                    Toast.LENGTH_LONG).show();
            backToQuizActivity(submitButton);
        }
    }

    private void setSscButtonClickMethod(Button button, ArrayList<SSC> arrayList) {

        button.setBackgroundColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundColor(getResources().getColor(R.color.green));

        if (index < arrayList.size() - 1) {
            submitButton.setClickable(false);
            setSscCorrectAnswer(button, arrayList);
            disableButton();

        } else {
            // This will work only when the user reaches the last Quiz section
            button.setTextColor(getResources().getColor(R.color.white));
            submitButton.setClickable(false);
            submitButton.setBackgroundColor(getResources().getColor(R.color.red));
            setSscCorrectAnswer(button, arrayList);
            disableButton();
            submitButton.setClickable(true);
            submitButton.setBackgroundColor(getResources().getColor(R.color.green));
            Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                    Toast.LENGTH_LONG).show();
            backToQuizActivity(submitButton);
        }
    }

    private void setHscButtonClickMethod(Button button, ArrayList<HSC> arrayList) {

        button.setBackgroundColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundColor(getResources().getColor(R.color.green));

        if (index < arrayList.size() - 1) {
            submitButton.setClickable(false);
            setHscCorrectAnswer(button, arrayList);
            disableButton();

        } else {
            // This will work only when the user reaches the last Quiz section
            button.setTextColor(getResources().getColor(R.color.white));
            submitButton.setClickable(false);
            submitButton.setBackgroundColor(getResources().getColor(R.color.red));
            setHscCorrectAnswer(button, arrayList);
            disableButton();
            submitButton.setClickable(true);
            submitButton.setBackgroundColor(getResources().getColor(R.color.green));
            Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                    Toast.LENGTH_LONG).show();
            backToQuizActivity(submitButton);
        }
    }

    private void setCompetitionButtonClickMethod(Button button, ArrayList<Competition> arrayList) {

        button.setBackgroundColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundColor(getResources().getColor(R.color.green));

        if (index < arrayList.size() - 1) {
            submitButton.setClickable(false);
            setCompetitionCorrectAnswer(button, arrayList);
            disableButton();

        } else {
            // This will work only when the user reaches the last Quiz section
            button.setTextColor(getResources().getColor(R.color.white));
            submitButton.setClickable(false);
            submitButton.setBackgroundColor(getResources().getColor(R.color.red));
            setCompetitionCorrectAnswer(button, arrayList);
            disableButton();
            submitButton.setClickable(true);
            submitButton.setBackgroundColor(getResources().getColor(R.color.green));
            Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                    Toast.LENGTH_LONG).show();
            backToQuizActivity(submitButton);
        }
    }


    private void setTournamentButtonClickMethod(Button button, ArrayList<TournamentQuestions> arrayList) {

        button.setBackgroundColor(getResources().getColor(R.color.green));
        submitButton.setBackgroundColor(getResources().getColor(R.color.green));

        if (index < arrayList.size() - 1) {
            submitButton.setClickable(false);
            tournamentCorrectAnswer(button, arrayList);
            disableButton();

        } else {
            // This will work only when the user reaches the last Quiz section
            button.setTextColor(getResources().getColor(R.color.white));
            submitButton.setClickable(false);
            submitButton.setBackgroundColor(getResources().getColor(R.color.red));
            tournamentCorrectAnswer(button, arrayList);
            disableButton();
            submitButton.setClickable(true);
            submitButton.setBackgroundColor(getResources().getColor(R.color.green));
            Toast.makeText(getApplicationContext(), "Congrats! Press \"Next\"",
                    Toast.LENGTH_LONG).show();
            backToQuizActivity(submitButton);
        }
    }


    private interface internetConnectionCheck {
        void connectionInfo(Boolean connection);
    }


    private interface userTimerInformation {
        void timerInfo(Long value);
    }

}