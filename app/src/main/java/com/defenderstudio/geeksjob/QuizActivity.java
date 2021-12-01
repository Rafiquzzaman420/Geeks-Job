package com.defenderstudio.geeksjob;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static ArrayList<MoviesQuestion> moviesList;
    public static ArrayList<CurriculumQuestion> curriculumList;
    public static ArrayList<ReligionQuestion> religionList;
    public static ArrayList<ScienceQuestion> scienceList;

    private final int index = 0;

    List<ReligionQuestion> religionQuestionList = new ArrayList<>();
    List<CurriculumQuestion> curriculumQuestionList = new ArrayList<>();
    List<MoviesQuestion> moviesQuestionList = new ArrayList<>();
    List<ScienceQuestion> scienceQuestionList = new ArrayList<>();

    MoviesQuestion moviesQuestion;
    ScienceQuestion scienceQuestion;
private scienceDatabaseLoadWithAsyncTask scienceDatabaseLoadWithAsyncTask;
    private moviesDatabaseLoadWithAsyncTask moviesDatabaseLoadWithAsyncTask;

    private boolean dialogShown = false;

    Runnable statusChecker = () -> {
        try {
            Dialog dialog = new Dialog(QuizActivity.this, R.style.dialogue);
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

    //==================================================================================================
    // onCreate() activity starts here
//==================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_section_activity);

        startConnectionRepeatingTask();

        MobileAds.initialize(
                this,
                initializationStatus -> {
                });


        curriculumList = new ArrayList<>();
        religionList = new ArrayList<>();
        moviesList = new ArrayList<>();
        scienceList = new ArrayList<>();


        religionQuestionList = religionList;
        curriculumQuestionList = curriculumList;
        moviesQuestionList = moviesList;
        scienceQuestionList = scienceList;

        moviesDatabaseLoadWithAsyncTask moviesDatabaseLoadWithAsyncTask = new moviesDatabaseLoadWithAsyncTask();
        moviesDatabaseLoadWithAsyncTask.execute();
        scienceDatabaseLoadWithAsyncTask scienceDatabaseLoadWithAsyncTask = new scienceDatabaseLoadWithAsyncTask();
        scienceDatabaseLoadWithAsyncTask.execute();




        curriculumDatabaseLoadWithAsyncTask curriculumDatabaseLoadWithAsyncTask = new curriculumDatabaseLoadWithAsyncTask();
        // This will be activated in the next couple of Updates Inshaallah
        //        curriculumDatabaseLoadWithAsyncTask.execute();

        religionDatabaseLoadWithAsyncTask religionDatabaseLoadWithAsyncTask = new religionDatabaseLoadWithAsyncTask();
        // This will be activated in the next couple of Updates Inshaallah
//        religionDatabaseLoadWithAsyncTask.execute();


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

        curriculumButton.setOnClickListener(v ->
                Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        );

        religionButton.setOnClickListener(v ->
                Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        );

        moviesButton.setOnClickListener(v -> {
            moviesQuestionAnswerLoadingIntent("Movies & Sports", "Movies & Sports", moviesList);
        });


        //==============================================================================================
        scienceButton.setOnClickListener(v -> {
            ScienceQuestionAnswerLoadingIntent("Science & History", "Science & History", scienceList);
        });
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

    public void moviesQuestionAnswerLoadingIntent(String message, String topicName, ArrayList<MoviesQuestion> arrayList) {
        ProgressDialog dialog = new ProgressDialog(QuizActivity.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                moviesQuestion = arrayList.get(index);
                Intent intent = new Intent(QuizActivity.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
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

    public void ScienceQuestionAnswerLoadingIntent(String message, String topicName, ArrayList<ScienceQuestion> arrayList) {
        ProgressDialog dialog = new ProgressDialog(QuizActivity.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                scienceQuestion = arrayList.get(index);
                Intent intent = new Intent(QuizActivity.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }


    class curriculumDatabaseLoadWithAsyncTask extends AsyncTask<CurriculumQuestion, Void, Void> {
        DatabaseReference curriculumReference;

        @Override
        protected Void doInBackground(CurriculumQuestion... lists) {

            curriculumReference = FirebaseDatabase.getInstance().getReference("Questions/CurriculumQuestion/Curriculum");
            curriculumReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    curriculumList = new ArrayList<>();
                    curriculumQuestionList = curriculumList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CurriculumQuestion curriculumQuestion = dataSnapshot.getValue(CurriculumQuestion.class);
                        curriculumList.add(curriculumQuestion);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }

    class scienceDatabaseLoadWithAsyncTask extends AsyncTask<ScienceQuestion, Void, Void> {
        DatabaseReference scienceReference;

        @Override
        protected Void doInBackground(ScienceQuestion... lists) {

            scienceReference = FirebaseDatabase.getInstance().getReference("Questions/ScienceQuestion/Science");

            scienceReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    scienceList = new ArrayList<>();
                    scienceQuestionList = scienceList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ScienceQuestion scienceQuestion = dataSnapshot.getValue(ScienceQuestion.class);
                        scienceList.add(scienceQuestion);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }

    class religionDatabaseLoadWithAsyncTask extends AsyncTask<ReligionQuestion, Void, Void> {
        DatabaseReference religionReference;

        @Override
        protected Void doInBackground(ReligionQuestion... lists) {
            religionReference = FirebaseDatabase.getInstance().getReference("Questions/ReligionQuestion/Religion");
            religionReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    religionList = new ArrayList<>();
                    religionQuestionList = religionList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ReligionQuestion religionQuestion = dataSnapshot.getValue(ReligionQuestion.class);
                        religionList.add(religionQuestion);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            return null;
        }
    }

    class moviesDatabaseLoadWithAsyncTask extends AsyncTask<MoviesQuestion, Void, Void> {
        DatabaseReference moviesReference;

        @Override
        protected Void doInBackground(MoviesQuestion... lists) {

            moviesReference = FirebaseDatabase.getInstance().getReference("Questions/MoviesQuestion/Movies");
            moviesReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    moviesList = new ArrayList<>();
                    moviesQuestionList = moviesList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        MoviesQuestion moviesQuestion = dataSnapshot.getValue(MoviesQuestion.class);
                        moviesList.add(moviesQuestion);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scienceDatabaseLoadWithAsyncTask != null){
            scienceDatabaseLoadWithAsyncTask.cancel(true);
        }
        if (moviesDatabaseLoadWithAsyncTask != null){
            moviesDatabaseLoadWithAsyncTask.cancel(true);
        }
    }
}
