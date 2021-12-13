package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


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

    // This callback will only be called when MyFragment is at least Started.


    // The callback can be enabled or disabled here or in handleOnBackPressed()


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
        TextView userPointReference = view.findViewById(R.id.userPointReference);
        CardView groupChat = view.findViewById(R.id.groupChat);
        TextView groupChatText = view.findViewById(R.id.group_chat_text);
        TextView rewardsText = view.findViewById(R.id.rewards_text);
        TextView leaderBoardText = view.findViewById(R.id.Leader_board_text);
        TextView quizText = view.findViewById(R.id.quiz_text);
        TextView pointsText = view.findViewById(R.id.points_text);
        ImageView quizImage = view.findViewById(R.id.quiz_image);
        ImageView rewardsImage = view.findViewById(R.id.rewards_image);
        ImageView chatImage = view.findViewById(R.id.chat_image);
        ImageView leaderBoardImage = view.findViewById(R.id.leader_board_image);


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
                Glide.with(this).load(photo).apply(RequestOptions.circleCropTransform()).into(userImageView);
            }
        }

        quizText.setTextSize(convertFromDp(40));
        groupChatText.setTextSize(convertFromDp(40));
        rewardsText.setTextSize(convertFromDp(40));
        leaderBoardText.setTextSize(convertFromDp(40));
        userPointReference.setTextSize(convertFromDp(30));
        pointsText.setTextSize(convertFromDp(30));

        quizImage.getLayoutParams().height = (int) convertFromDp(250);
        rewardsImage.getLayoutParams().height = (int) convertFromDp(250);
        chatImage.getLayoutParams().height = (int) convertFromDp(250);
        leaderBoardImage.getLayoutParams().height = (int) convertFromDp(250);

        quizImage.getLayoutParams().width = (int) convertFromDp(250);
        rewardsImage.getLayoutParams().width = (int) convertFromDp(250);
        chatImage.getLayoutParams().width = (int) convertFromDp(250);
        leaderBoardImage.getLayoutParams().width = (int) convertFromDp(250);

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
        DatabaseReference databaseEarningReference;
        assert firebaseUser != null;
        databaseEarningReference = FirebaseDatabase.getInstance().
                getReference("AllUsers/User/" + firebaseUser.getUid() + "/Earned_Point_Amount");
        databaseEarningReference.addValueEventListener(new ValueEventListener() {
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
        });
    }

    public interface userPointInfoCallBack {
        void userPointInfo(Long value);
    }

    public float convertFromDp(int input) {
        final float scale = requireActivity().getResources().getDisplayMetrics().density;
        return ((input - 0.7f) / scale);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}