package com.defenderstudio.geeksjob;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Curriculum extends AppCompatActivity {

    public static ArrayList<HSC> hscList;
    public static ArrayList<SSC> sscList;
    public static ArrayList<Medical> medicalList;
    public static ArrayList<University> universityList;
    public static ArrayList<Competition> competitionList;
    private final int index = 0;
    List<HSC> hscArrayList = new ArrayList<>();
    List<SSC> sscArrayList = new ArrayList<>();
    List<Medical> medicalArrayList = new ArrayList<>();
    List<University> universityArrayList = new ArrayList<>();
    List<Competition> competitionArrayList = new ArrayList<>();
    CardView hscButton, sscButton, medicalButton, universityButton, competitionButton;
    ImageView hscStatus, sscStatus, medicalStatus, universityStatus, competitionStatus;
    DatabaseReference databaseReference;
    ValueEventListener listener;
    HSC hsc;
    SSC ssc;
    Medical medical;
    University university;
    Competition competition;
    ProgressDialog dialog;
    private hscDataLoader hscDataLoader;
    private sscDataLoader sscDataLoader;
    private medicalDataLoader medicalDataLoader;
    private universityDataLoader universityDataLoader;
    private competitionDataLoader competitionDataLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.curriculum_activity);

        hscButton = findViewById(R.id.hsc_preparation);
        sscButton = findViewById(R.id.ssc_preparation);
        medicalButton = findViewById(R.id.medical);
        universityButton = findViewById(R.id.university);
        competitionButton = findViewById(R.id.competition);

        hscStatus = findViewById(R.id.hsc_online);
        sscStatus = findViewById(R.id.ssc_online);
        medicalStatus = findViewById(R.id.medical_online);
        universityStatus = findViewById(R.id.university_online);
        competitionStatus = findViewById(R.id.competition_online);

        dialog = new ProgressDialog(Curriculum.this, R.style.ProgressDialogStyle);
        dialog.setMessage("Loading. Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            quizSectionOnlineInfo("HSC", info -> {
                if (info.equals("true")) {
                    hscStatus.setBackgroundResource(R.drawable.online);
                }
            });
            quizSectionOnlineInfo("SSC", info -> {
                if (info.equals("true")) {
                    sscStatus.setBackgroundResource(R.drawable.online);
                }
            });
            quizSectionOnlineInfo("Medical", info -> {
                if (info.equals("true")) {
                    medicalStatus.setBackgroundResource(R.drawable.online);
                }
            });
            quizSectionOnlineInfo("University", info -> {
                if (info.equals("true")) {
                    universityStatus.setBackgroundResource(R.drawable.online);
                }
            });
            quizSectionOnlineInfo("Competition", info -> {
                if (info.equals("true")) {
                    competitionStatus.setBackgroundResource(R.drawable.online);
                }
            });
            dialog.dismiss();
        }, 2000);


        hscList = new ArrayList<>();
        sscList = new ArrayList<>();
        medicalList = new ArrayList<>();
        universityList = new ArrayList<>();
        competitionList = new ArrayList<>();

        hscArrayList = hscList;
        sscArrayList = sscList;
        medicalArrayList = medicalList;
        universityArrayList = universityList;
        competitionArrayList = competitionList;

        hscDataLoader = new hscDataLoader();
        sscDataLoader = new sscDataLoader();
        universityDataLoader = new universityDataLoader();
        medicalDataLoader = new medicalDataLoader();
        competitionDataLoader = new competitionDataLoader();

        hscDataLoader.execute();
        sscDataLoader.execute();
        medicalDataLoader.execute();
        universityDataLoader.execute();
        competitionDataLoader.execute();

        hscButton.setOnClickListener(view ->
                hscQuestionLoader("HSC", "HSC", hscList));
        sscButton.setOnClickListener(view ->
                sscQuestionLoader("SSC", "SSC", sscList));
        medicalButton.setOnClickListener(view ->
                medicalQuestionLoader("Medical", "Medical", medicalList));
        universityButton.setOnClickListener(view ->
                universityQuestionLoader("University", "University", universityList));
        competitionButton.setOnClickListener(view ->
                competitionQuestionLoader("Competition", "Competition", competitionList));
    }

    @Override
    public void onBackPressed() {
        Intent backPressIntent = new Intent(Curriculum.this, QuizActivity.class);
        startActivity(backPressIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (hscDataLoader != null) {
            hscDataLoader.cancel(true);
        }
        if (sscDataLoader != null) {
            sscDataLoader.cancel(true);
        }
        if (medicalDataLoader != null) {
            medicalDataLoader.cancel(true);
        }
        if (universityDataLoader != null) {
            universityDataLoader.cancel(true);
        }
        if (competitionDataLoader != null) {
            competitionDataLoader.cancel(true);
        }
        super.onDestroy();
    }

    public void hscQuestionLoader(String message, String topicName, ArrayList<HSC> arrayList) {
        ProgressDialog dialog = new ProgressDialog(Curriculum.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                hsc = arrayList.get(index);
                Intent intent = new Intent(Curriculum.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    public void sscQuestionLoader(String message, String topicName, ArrayList<SSC> arrayList) {
        ProgressDialog dialog = new ProgressDialog(Curriculum.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                ssc = arrayList.get(index);
                Intent intent = new Intent(Curriculum.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    public void medicalQuestionLoader(String message, String topicName, ArrayList<Medical> arrayList) {
        ProgressDialog dialog = new ProgressDialog(Curriculum.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                medical = arrayList.get(index);
                Intent intent = new Intent(Curriculum.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    public void universityQuestionLoader(String message, String topicName, ArrayList<University> arrayList) {
        ProgressDialog dialog = new ProgressDialog(Curriculum.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                university = arrayList.get(index);
                Intent intent = new Intent(Curriculum.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    public void competitionQuestionLoader(String message, String topicName, ArrayList<Competition> arrayList) {
        ProgressDialog dialog = new ProgressDialog(Curriculum.this, R.style.ProgressDialogStyle);
        dialog.setMessage(message + " Quiz loading...");
        dialog.setCancelable(false);
        dialog.show();
        new Handler().postDelayed(() -> {
            try {
                competition = arrayList.get(index);
                Intent intent = new Intent(Curriculum.this, QuestionAnsActivity.class);
                intent.putExtra("topicName", topicName);
                startActivity(intent);
            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }, 3000);
    }

    private void quizSectionOnlineInfo(String child, onlineInfo getOnlineInfo) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Quiz Servers").child(child);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String info = snapshot.getValue(String.class);
                    getOnlineInfo.info(info);
                } catch (Exception e) {
                    getOnlineInfo.info("false");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        };
        databaseReference.addListenerForSingleValueEvent(listener);

    }

    private interface onlineInfo {
        void info(String info);
    }

    class hscDataLoader extends AsyncTask<HSC, Void, Void> {

        @Override
        protected Void doInBackground(HSC... lists) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Questions").child("HSC");
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    hscList = new ArrayList<>();
                    hscArrayList = hscList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        HSC hsc = dataSnapshot.getValue(HSC.class);
                        hscList.add(hsc);
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

    class sscDataLoader extends AsyncTask<SSC, Void, Void> {

        @Override
        protected Void doInBackground(SSC... lists) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Questions").child("SSC");
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    sscList = new ArrayList<>();
                    sscArrayList = sscList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SSC ssc = dataSnapshot.getValue(SSC.class);
                        sscList.add(ssc);
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

    class medicalDataLoader extends AsyncTask<Medical, Void, Void> {

        @Override
        protected Void doInBackground(Medical... lists) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Questions").child("Medical");
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    medicalList = new ArrayList<>();
                    medicalArrayList = medicalList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Medical medical = dataSnapshot.getValue(Medical.class);
                        medicalList.add(medical);
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

    class universityDataLoader extends AsyncTask<University, Void, Void> {

        @Override
        protected Void doInBackground(University... lists) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Questions").child("University");
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    universityList = new ArrayList<>();
                    universityArrayList = universityList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        University university = dataSnapshot.getValue(University.class);
                        universityList.add(university);
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

    class competitionDataLoader extends AsyncTask<Competition, Void, Void> {

        @Override
        protected Void doInBackground(Competition... lists) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Questions").child("Competition");
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    competitionList = new ArrayList<>();
                    competitionArrayList = competitionList;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Competition competition = dataSnapshot.getValue(Competition.class);
                        competitionList.add(competition);
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

}
