package com.defenderstudio.geeksjob;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderBoard extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    LeaderBoardAdapter leaderBoardAdapter;
    ArrayList<LeaderBoardUser> leaderBoardUserArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_activity);

        recyclerView = findViewById(R.id.competition_recycler_view);
        databaseReference = FirebaseDatabase.getInstance().
                getReference("AllUsers/Competition/UserList");

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            leaderBoardUserArrayList = new ArrayList<>();

            leaderBoardAdapter = new LeaderBoardAdapter(this, leaderBoardUserArrayList);
            recyclerView.setAdapter(leaderBoardAdapter);

            databaseReference.orderByChild("pointsValue").addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        LeaderBoardUser leaderBoardUser = dataSnapshot.getValue(LeaderBoardUser.class);
                        leaderBoardUserArrayList.add(leaderBoardUser);
                    }
                    Collections.reverse(leaderBoardUserArrayList);
                    leaderBoardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
    }

    @Override
    public void onBackPressed() {
        Intent goBackIntent = new Intent(LeaderBoard.this, MainActivity.class);
        startActivity(goBackIntent);
    }
}