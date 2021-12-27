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
import android.widget.TextView;
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

    public static ArrayList<TournamentQuestions> tournamentList;

    private final int index = 0;
    ProgressDialog dialog;
    List<TournamentQuestions> tournamentQuestionsList = new ArrayList<>();

    TournamentQuestions tournamentQuestions;
    private tournamentDatabaseLoadWithAsyncTask tournamentDatabaseLoadWithAsyncTask;

    String TotalUser = "Total Users";
    String OnlineUser = "Online Users";

    DatabaseReference databaseReference;
    ValueEventListener listener;


    public QuizActivity() {
    }

    //==================================================================================================
    // onCreate() activity starts here
//==================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_section_activity);

        tournamentList = new ArrayList<>();
        tournamentQuestionsList = tournamentList;

        tournamentDatabaseLoadWithAsyncTask = new tournamentDatabaseLoadWithAsyncTask();
        tournamentDatabaseLoadWithAsyncTask.execute();


//==================================================================================================
// Hooking up all the views
//==================================================================================================

        CardView curriculumButton = findViewById(R.id.curriculum);
        CardView tournamentButton = findViewById(R.id.tournament);
        TextView totalUsers = findViewById(R.id.total_users);
        TextView onlineUsers = findViewById(R.id.online_users);


//==================================================================================================
        // Setting all the OnClickListener for all the buttons
//==================================================================================================

        UserInfo(TotalUser, info -> {
            if (info != 0){
                totalUsers.setText(String.valueOf(info));
            }
        });

        UserInfo(OnlineUser, info -> {
            if (info != 0){
                onlineUsers.setText(String.valueOf(info));
            }
        });

       curriculumButton.setOnClickListener(view -> {
//           Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(QuizActivity.this, Curriculum.class);
           intent.putExtra("Curriculum", "Curriculum");
           startActivity(intent);
           finish();
       });
        tournamentButton.setOnClickListener(v ->
                tournamentQuestionLoadingIntent("Tournament", "Tournament", tournamentList)
        );


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

    public void tournamentQuestionLoadingIntent(String message, String topicName, ArrayList<TournamentQuestions> arrayList) {
        dialog = new ProgressDialog(QuizActivity.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                tournamentQuestions = arrayList.get(index);
                Intent intent = new Intent(QuizActivity.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        closingCodes();
        super.onDestroy();
    }

    private void closingCodes(){
        if (tournamentDatabaseLoadWithAsyncTask != null) {
            tournamentDatabaseLoadWithAsyncTask.cancel(true);
        }
        if (databaseReference != null && listener != null) {
            databaseReference.removeEventListener(listener);
        }
        dialog = new ProgressDialog(QuizActivity.this, R.style.ProgressDialogStyle);
        if (dialog.isShowing()){
            dialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        closingCodes();
        super.onStop();
    }

    class tournamentDatabaseLoadWithAsyncTask extends AsyncTask<TournamentQuestions, Void, Void> {

        @Override
        protected Void doInBackground(TournamentQuestions... lists) {

            databaseReference = FirebaseDatabase.getInstance().getReference("Questions/Tournament/Questions");
            listener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    tournamentList = new ArrayList<>();
                    tournamentQuestionsList = tournamentList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        TournamentQuestions tournamentQuestions = dataSnapshot.getValue(TournamentQuestions.class);
                        tournamentList.add(tournamentQuestions);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            databaseReference.addListenerForSingleValueEvent(listener);
            return null;
        }
    }

    private void UserInfo(String path, totalUsers users) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Total Users").child(path);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Long info = snapshot.getValue(Long.class);
                    users.info(info);
                } catch (Exception e) {
                    users.info(0);
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
            }
        };
        databaseReference.addListenerForSingleValueEvent(listener);

    }
    private interface totalUsers {
        void info(long info);
    }
}
