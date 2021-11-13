package com.defenderstudio.geeksjob;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    List<Questions> allQuestionsList = new ArrayList<>();
    Questions questions;

    public static ArrayList<Questions> historyList, curriculumList, sportsList, moviesList, scienceList
            , religionList, iotList, economyList;

    private final int index = 0;


    //==================================================================================================
    // onCreate() activity starts here
//==================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_activity);
        overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {}
                });



        historyList = new ArrayList<>();
        economyList = new ArrayList<>();
        curriculumList = new ArrayList<>();
        religionList = new ArrayList<>();
        sportsList = new ArrayList<>();
        moviesList = new ArrayList<>();
        scienceList = new ArrayList<>();
        iotList = new ArrayList<>();


        allQuestionsList = historyList;
        allQuestionsList = economyList;
        allQuestionsList = curriculumList;
        allQuestionsList = religionList;
        allQuestionsList = sportsList;
        allQuestionsList = moviesList;
        allQuestionsList = scienceList;
        allQuestionsList = iotList;

        DatabaseLoadWithAsyncTask databaseLoadWithAsyncTask = new DatabaseLoadWithAsyncTask();
        databaseLoadWithAsyncTask.execute();


//==================================================================================================
// Hooking up all the views
//==================================================================================================

        CardView curriculumButton = findViewById(R.id.curriculum);
        CardView iotButton = findViewById(R.id.iot);
        CardView historyButton = findViewById(R.id.history);
        CardView religionButton = findViewById(R.id.religion);
        CardView sportsButton = findViewById(R.id.sports);
        CardView moviesButton = findViewById(R.id.movies);
        CardView scienceButton = findViewById(R.id.science);
        CardView economyButton = findViewById(R.id.economy);

//==================================================================================================
        // Common intent for all the question buttons
//==================================================================================================


//==================================================================================================
        // Setting all the OnClickListener for all the buttons
//==================================================================================================

        curriculumButton.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show());

        historyButton.setOnClickListener(v -> questionAnswerLoadingIntent("History", "History", historyList));

        religionButton.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show());

        iotButton.setOnClickListener(v -> questionAnswerLoadingIntent("IOT", "IOT", iotList));

        sportsButton.setOnClickListener(v -> questionAnswerLoadingIntent("Sports", "Sports", sportsList));

        moviesButton.setOnClickListener(v -> questionAnswerLoadingIntent("Movies", "Movies", moviesList));

        scienceButton.setOnClickListener(v -> questionAnswerLoadingIntent("Science", "Science", scienceList));

        economyButton.setOnClickListener(v -> questionAnswerLoadingIntent("Economy", "Economy", economyList));

    //==============================================================================================

    }

    //==============================================================================================
    // onBackPressed activity
    //==============================================================================================
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent quizIntent = new Intent(QuizActivity.this, MainActivity.class);
        startActivity(quizIntent);
    }

    //==============================================================================================

    public void questionAnswerLoadingIntent(String message, String topicName, ArrayList<Questions> arrayList) {
        ProgressDialog dialog = new ProgressDialog(QuizActivity.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                questions = arrayList.get(index);
                Intent intent = new Intent(QuizActivity.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    class DatabaseLoadWithAsyncTask extends AsyncTask<Questions, Void, Void> {
        DatabaseReference historyReference, curriculumReference,
                sportsReference,
                iotReference, religionReference, moviesReference,
                economyReference, scienceReference;

        @Override
        protected Void doInBackground(Questions... lists) {

            historyReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/History");
            curriculumReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Curriculum");
            sportsReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Sports");
            iotReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/IOT");
            religionReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Religion");
            moviesReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Movies");
            economyReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Economy");
            scienceReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Science");

            historyReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    historyList = new ArrayList<>();
                    allQuestionsList = historyList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        historyList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            economyReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    economyList = new ArrayList<>();
                    allQuestionsList = economyList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        economyList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


            scienceReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    scienceList = new ArrayList<>();
                    allQuestionsList = scienceList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        scienceList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            curriculumReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    curriculumList = new ArrayList<>();
                    allQuestionsList = curriculumList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        curriculumList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            moviesReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    moviesList = new ArrayList<>();
                    allQuestionsList = moviesList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        moviesList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            religionReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    religionList = new ArrayList<>();
                    allQuestionsList = religionList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        religionList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            sportsReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    sportsList = new ArrayList<>();
                    allQuestionsList = sportsList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        sportsList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            iotReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    iotList = new ArrayList<>();
                    allQuestionsList = iotList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Questions questions = dataSnapshot.getValue(Questions.class);
                        iotList.add(questions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }
}
