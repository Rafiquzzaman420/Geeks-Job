package com.defenderstudio.geeksjob;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

    public static ArrayList<Questions>  curriculumList, moviesList, scienceList, religionList;
    private final int index = 0;
    List<Questions> allQuestionsList = new ArrayList<>();
    Questions questions;

    //==================================================================================================
    // onCreate() activity starts here
//==================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_section_activity);
        overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);
        MobileAds.initialize(
                this,
                initializationStatus -> {
                });


        curriculumList = new ArrayList<>();
        religionList = new ArrayList<>();
        moviesList = new ArrayList<>();
        scienceList = new ArrayList<>();


        allQuestionsList = curriculumList;
        allQuestionsList = religionList;
        allQuestionsList = moviesList;
        allQuestionsList = scienceList;

        DatabaseLoadWithAsyncTask databaseLoadWithAsyncTask = new DatabaseLoadWithAsyncTask();
        databaseLoadWithAsyncTask.execute();


//==================================================================================================
// Hooking up all the views
//==================================================================================================

        CardView curriculumButton = findViewById(R.id.curriculum);
        CardView religionButton = findViewById(R.id.religion);
        CardView moviesButton = findViewById(R.id.movies);
        CardView scienceButton = findViewById(R.id.science);

//==================================================================================================
        // Common intent for all the question buttons
//==================================================================================================


//==================================================================================================
        // Setting all the OnClickListener for all the buttons
//==================================================================================================

        curriculumButton.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show());

        religionButton.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show());

        moviesButton.setOnClickListener(v -> questionAnswerLoadingIntent("Movies & Sports", "Movies & Sports", moviesList));

        scienceButton.setOnClickListener(v -> questionAnswerLoadingIntent("Science & History", "Science & History", scienceList));

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

            curriculumReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Curriculum");
            religionReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Religion");
            moviesReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Movies");
            scienceReference = FirebaseDatabase.getInstance().getReference("Questions/AllQuestions/Science");

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

            return null;
        }
    }
}
