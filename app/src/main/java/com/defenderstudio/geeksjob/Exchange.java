package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Exchange#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Exchange extends Fragment {


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText bkashAccount, paypalAccount;
    private TextView pointAmount;
    private Button tenThousandButton, twentyThousandButton, fiftyThousandButton;
    private CardView submitButton;

    public Exchange() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Exchange.
     */
    public static Exchange newInstance(String param1, String param2) {
        Exchange fragment = new Exchange();
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        bkashAccount = view.findViewById(R.id.bkashAccountNumber);
        paypalAccount = view.findViewById(R.id.paypalEmail);
        tenThousandButton = view.findViewById(R.id.tenThousand);
        twentyThousandButton = view.findViewById(R.id.twentyThousand);
        fiftyThousandButton = view.findViewById(R.id.fiftyThousand);
        submitButton = view.findViewById(R.id.submit_exchange);
        pointAmount = view.findViewById(R.id.pointAmount);

        TextView withdrawalAmountText = view.findViewById(R.id.withdrawal_amount_text);
        TextView coinsAmount = view.findViewById(R.id.coins_amount_text);
        TextView submitButtonText = view.findViewById(R.id.submit_button_in_exchange);

        setSubmitButton(view);

        bkashAccount.setTextSize(convertFromDp(25));
        paypalAccount.setTextSize(convertFromDp(25));
        tenThousandButton.setTextSize(convertFromDp(30));
        twentyThousandButton.setTextSize(convertFromDp(30));
        fiftyThousandButton.setTextSize(convertFromDp(30));
        pointAmount.setTextSize(convertFromDp(30));
        submitButtonText.setTextSize(convertFromDp(35));
        coinsAmount.setTextSize(convertFromDp(30));
        withdrawalAmountText.setTextSize(convertFromDp(30));

        return view;
    }

    private void setSubmitButton(View view) {
        submitButton.setClickable(false);
        userPointInfoCallBack(value -> {
            if (value != null) {
                pointAmount.setText(String.valueOf(value));
                setTenThousandButton(value);

                setTwentyThousandButton(value);

                setFiftyThousandButton(value);

                if (value < 10000) {
                    submitButton.setClickable(false);
                }
            } else {
                pointAmount.setText("N/A");
            }
        });

        submitButton.setOnClickListener(v -> {
            ProgressBar progressBar = view.findViewById(R.id.progress_exchange);
            progressBar.setVisibility(View.VISIBLE);

            String paypalAccountEmail = paypalAccount.getText().toString().trim();

            String bkashAccountNumber = bkashAccount.getText().toString().trim();

            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            assert firebaseUser != null;
            DatabaseReference bkashNumber = firebaseDatabase.child("Winning Persons").
                    child("User").child(firebaseUser.getUid()).child("UserInformation").child("BkashAccount");

            DatabaseReference paypalEmail = firebaseDatabase.child("Winning Persons").
                    child("User").child(firebaseUser.getUid()).child("UserInformation").child("PaypalEmail");

            new Handler().postDelayed(() -> {

                userPointInfoCallBack(value -> {
                    if (value >= 10000 && (!bkashAccountNumber.isEmpty() || !paypalAccountEmail.isEmpty())) {

                        if (!bkashAccountNumber.isEmpty()) {
                            bkashNumber.setValue(bkashAccountNumber);
                        }
                        if (!paypalAccountEmail.isEmpty()) {
                            paypalEmail.setValue(paypalAccountEmail);
                        } else {
                            paypalEmail.setValue(paypalAccountEmail);
                            bkashNumber.setValue(bkashAccountNumber);
                        }
                        DatabaseReference tenThousandPoints = firebaseDatabase.child("Winning Persons").
                                child("User").child(firebaseUser.getUid()).child("UserInformation").child("Amount");
                        DatabaseReference name = firebaseDatabase.child("Winning Persons").
                                child("User").child(firebaseUser.getUid()).child("UserInformation").child("Name");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (tenThousandButton.isSelected()) {
                                name.setValue(user.getDisplayName());
                                tenThousandPoints.setValue(10000);
                                Toast.makeText(requireActivity().getApplicationContext(),
                                        "Submitted Successfully",
                                        Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            } else if (twentyThousandButton.isSelected()) {
                                DatabaseReference twentyThousandPoints = firebaseDatabase.child("Winning Persons").
                                        child("User").child(firebaseUser.getUid()).child("UserInformation").child("Amount");
                                name.setValue(user.getDisplayName());
                                twentyThousandPoints.setValue(20000);
                                Toast.makeText(requireActivity().getApplicationContext(),
                                        "Submitted Successfully",
                                        Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            } else if (fiftyThousandButton.isSelected()) {
                                DatabaseReference fiftyThousandPoints = firebaseDatabase.child("Winning Persons").
                                        child("User").child(firebaseUser.getUid()).child("UserInformation").child("Amount");
                                name.setValue(user.getDisplayName());
                                fiftyThousandPoints.setValue(50000);
                                Toast.makeText(requireActivity().getApplicationContext(),
                                        "Submitted Successfully",
                                        Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }else{
                                Toast.makeText(requireActivity().getApplicationContext(),
                                        "Please select your amount",
                                        Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }


                    } else {
                        paypalAccount.setError("Enter an email address");
                        paypalAccount.requestFocus();
                        bkashAccount.setError("Enter a phone number");
                        bkashAccount.requestFocus();
                        Toast.makeText(requireActivity().getApplicationContext(),
                                "Complete the required fields!",
                                Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }, 3000);
        });
    }


    private void setTenThousandButton(long value) {
        if (value < 10000) {
            Toast.makeText(requireActivity().getApplicationContext(),
                    "Sorry you're not yet eligible for withdrawal",
                    Toast.LENGTH_LONG).show();
            tenThousandButton.setClickable(false);
            twentyThousandButton.setClickable(false);
            fiftyThousandButton.setClickable(false);
        } else {
            tenThousandButton.setOnClickListener(v -> {
                tenThousandButton.setSelected(true);
                twentyThousandButton.setSelected(false);
                fiftyThousandButton.setSelected(false);
                tenThousandButton.setBackgroundColor(getResources().getColor(R.color.blue));
                tenThousandButton.setTextColor(Color.WHITE);
                fiftyThousandButton.setBackgroundColor(Color.WHITE);
                fiftyThousandButton.setTextColor(getResources().getColor(R.color.blue));
                twentyThousandButton.setBackgroundColor(Color.WHITE);
                twentyThousandButton.setTextColor(getResources().getColor(R.color.blue));

            });
        }
    }


    private void setTwentyThousandButton(long value) {
        if (value < 20000) {
            twentyThousandButton.setClickable(false);
            fiftyThousandButton.setClickable(false);
        } else {
            twentyThousandButton.setOnClickListener(v -> {
                twentyThousandButton.setSelected(true);
                tenThousandButton.setSelected(false);
                fiftyThousandButton.setSelected(false);
                twentyThousandButton.setBackgroundColor(getResources().getColor(R.color.blue));
                twentyThousandButton.setTextColor(Color.WHITE);
                fiftyThousandButton.setBackgroundColor(Color.WHITE);
                tenThousandButton.setBackgroundColor(Color.WHITE);
                fiftyThousandButton.setTextColor(getResources().getColor(R.color.blue));
                tenThousandButton.setTextColor(getResources().getColor(R.color.blue));

            });
        }

    }

    private void setFiftyThousandButton(long value) {
        if (value < 50000) {
            fiftyThousandButton.setClickable(false);
        } else {
            fiftyThousandButton.setOnClickListener(v -> {
                fiftyThousandButton.setSelected(true);
                tenThousandButton.setSelected(false);
                twentyThousandButton.setSelected(false);
                twentyThousandButton.setBackgroundColor(Color.WHITE);
                tenThousandButton.setBackgroundColor(Color.WHITE);
                twentyThousandButton.setTextColor(getResources().getColor(R.color.blue));
                tenThousandButton.setTextColor(getResources().getColor(R.color.blue));
                fiftyThousandButton.setBackgroundColor(getResources().getColor(R.color.blue));
                fiftyThousandButton.setTextColor(Color.WHITE);

            });
        }
    }

    private void userPointInfoCallBack(userInfoCallBack userInfoCallBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        DatabaseReference userPointReference = databaseReference.
                child("Winning Persons").child(firebaseUser.getUid()).child("Coin Amount");
        userPointReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                try {
                    userInfoCallBack.userInfoCall(longValue);
                } catch (Exception ignored) {}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            "Loaded successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public float convertFromDp(int input) {
        final float scale = requireActivity().getResources().getDisplayMetrics().density;
        return ((input - 0.7f) / scale);
    }


    public interface userInfoCallBack {
        void userInfoCall(Long value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}