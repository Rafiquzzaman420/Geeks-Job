package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    DatabaseReference databaseReference;
    ValueEventListener listener;
    View fragmentView;
    FirebaseUser firebaseUser;

    public Home() {
        // Required empty public constructor
    }

    // This callback will only be called when MyFragment is at least Started.


    // The callback can be enabled or disabled here or in handleOnBackPressed()


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        CardView leaderBoardActivity = view.findViewById(R.id.leader_board_activity);
        CardView quizActivity = view.findViewById(R.id.quiz_intent);
        ImageView userImageView = view.findViewById(R.id.userImageInHomeFragment);
        TextView userName = view.findViewById(R.id.userNameInHomeFragment);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        TextView userPointReference = view.findViewById(R.id.userPointReference);
        CardView groupChat = view.findViewById(R.id.groupChat);

        readPointDataFromFirebase(value -> {
            if (value != null) {
                userPointReference.setText(String.valueOf(value));
            }else{
                userPointReference.setText(String.valueOf(0));
            }
            userPointReference.invalidate();
        });

        if (firebaseUser != null) {
            for (UserInfo profile : firebaseUser.getProviderData()) {

                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                Uri photo = profile.getPhotoUrl();
                userName.setText(name);
                Glide.with(this).
                        load(photo).
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(true).
                        apply(RequestOptions.circleCropTransform()).
                        into(userImageView);
            }
        }

        groupChat.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this.getActivity(), GroupChatActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        quizActivity.setOnClickListener(v -> {
            Intent quizActivityIntent = new Intent(Home.this.getActivity(), QuizActivity.class);
            startActivity(quizActivityIntent);
            requireActivity().finish();
        });

        CardView rewards = view.findViewById(R.id.rewards);
        rewards.setOnClickListener(v -> {
            Intent rewardsIntent = new Intent(Home.this.getActivity(), Rewards.class);
            startActivity(rewardsIntent);
            requireActivity().finish();
        });


        leaderBoardActivity.setOnClickListener(v -> {
            try {
                Intent leaderBoardIntent = new Intent(Home.this.getActivity(), LeaderBoard.class);
                startActivity(leaderBoardIntent);
                requireActivity().finish();
            } catch (Exception exception) {
                Toast.makeText(getActivity(), "Please try again!", Toast.LENGTH_SHORT).show();
            }

        });
        return view;
    }


    public void readPointDataFromFirebase(Home.userPointInfoCallBack userPointInfoCallBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().
                getReference("AllUsers/User/" + firebaseUser.getUid() + "/Earned_Point_Amount");
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                try {
                    userPointInfoCallBack.userPointInfo(longValue);
                }catch (Exception ignored){}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(listener);
    }

    public interface userPointInfoCallBack {
        void userPointInfo(Long value);
    }

    @Override
    public void onDestroyView() {
        if (databaseReference != null && listener != null) {
            databaseReference.removeEventListener(listener);
        }
        fragmentView = null;
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}