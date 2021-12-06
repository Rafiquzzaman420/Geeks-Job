package com.defenderstudio.geeksjob;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ImageView profile_user_image;
    TextView profile_user_name, profile_user_email, correct_answer, wrong_answer, total_earning, quiz_answered;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profile_user_name = view.findViewById(R.id.profile_user_name);
        profile_user_email = view.findViewById(R.id.profile_user_email);
        quiz_answered = view.findViewById(R.id.quiz_completed);
        profile_user_image = view.findViewById(R.id.profile_user_photo);
        total_earning = view.findViewById(R.id.total_earning);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {

                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                String email = profile.getEmail();
                Uri photo = profile.getPhotoUrl();

                profile_user_name.setText(name);
                profile_user_email.setText(email);

                readQuizInformation(value -> {
                    if (value != null) {
                        quiz_answered.setText(String.valueOf(value));
                    } else {
                        quiz_answered.setText("N/A");
                    }
                });

                readDataFromFirebase(value -> {
                    if (value != null) {
                        total_earning.setText(String.valueOf(value));
                    } else {
                        total_earning.setText("N/A");
                    }
                });

                try {
                    Glide.with(this).load(photo).
                            apply(RequestOptions.circleCropTransform()).into(profile_user_image);
                } catch (Exception e) {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            "Image not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return view;
    }

    public void readDataFromFirebase(databaseInfoCallBack databaseInfoCallBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseEarningReference;
        assert firebaseUser != null;
        databaseEarningReference = FirebaseDatabase.getInstance().
                getReference("AllUsers/User/" + firebaseUser.getUid() + "/Earned_Point_Amount");
        databaseEarningReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                databaseInfoCallBack.onCallback(longValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void readQuizInformation(quizInformationCallBack quizInformationCallBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseEarningReference;
        assert firebaseUser != null;
        databaseEarningReference = FirebaseDatabase.getInstance().
                getReference("AllUsers/User/" + firebaseUser.getUid() + "/Ans_Quiz_Amount");
        databaseEarningReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                quizInformationCallBack.quizInfoCall(longValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface databaseInfoCallBack {
        void onCallback(Long value);
    }

    public interface quizInformationCallBack {
        void quizInfoCall(Long value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}