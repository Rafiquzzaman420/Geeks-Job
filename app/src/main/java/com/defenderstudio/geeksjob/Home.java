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

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        CardView leaderBoardActivity = view.findViewById(R.id.leader_board_activity);
        CardView quizActivity = view.findViewById(R.id.quiz_intent);
        ImageView userImageView = view.findViewById(R.id.userImageInHomeFragment);
        TextView userName = view.findViewById(R.id.userNameInHomeFragment);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            for (UserInfo profile : firebaseUser.getProviderData()) {

                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                Uri photo = profile.getPhotoUrl();
                userName.setText(name);
                Glide.with(this).load(photo).apply(RequestOptions.circleCropTransform()).into(userImageView);
            }
        }
        quizActivity.setOnClickListener(v -> {
            Intent quizActivityIntent = new Intent(Home.this.getActivity(), QuizActivity.class);
            startActivity(quizActivityIntent);
        });

        CardView rewards = view.findViewById(R.id.rewards);
        rewards.setOnClickListener(v -> {
            Intent rewardsIntent = new Intent(Home.this.getActivity(), Rewards.class);
            startActivity(rewardsIntent);
        });


        leaderBoardActivity.setOnClickListener(v -> {
            try {
                Intent leaderBoardIntent = new Intent(Home.this.getActivity(), LeaderBoard.class);
                startActivity(leaderBoardIntent);
            } catch (Exception exception) {
                Toast.makeText(getActivity(), "Please try again!", Toast.LENGTH_SHORT).show();
            }

        });
        return view;
    }

}