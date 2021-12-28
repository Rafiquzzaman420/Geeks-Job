package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Objects;

public class Exchange extends Fragment {

    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    View view;
    FirebaseUser firebaseUser;
    private EditText bkashAccount;
    private TextView pointAmount;
    private CardView submitButton;
    ProgressBar progressBar;

    public Exchange() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_exchange, container, false);
        bkashAccount = view.findViewById(R.id.bkashAccountNumber);
        submitButton = view.findViewById(R.id.submit_exchange);
        pointAmount = view.findViewById(R.id.pointAmount);
        TextView userName = view.findViewById(R.id.user_name_in_exchange);
        TextView userEmail = view.findViewById(R.id.user_email_in_exchange);

        userName.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
        userEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        setSubmitButton(view);
        if (bkashAccount.isSuggestionsEnabled()) {
            bkashAccount.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {

                }
            });

            bkashAccount.setOnLongClickListener(view -> false);
            bkashAccount.setTextIsSelectable(false);
        }
        return view;
    }

    private void setSubmitButton(View view) {
        submitButton.setClickable(false);
        userPointInfoCallBack(value -> {
            if (value != null) {
                pointAmount.setText(String.valueOf(value));
                if (value < 10000) {
                    submitButton.setClickable(false);
                }
            } else {
                submitButton.setClickable(false);
                pointAmount.setText("0");
                Toast.makeText(requireActivity().getApplicationContext(),
                        "You don't have enough coins!",
                        Toast.LENGTH_LONG).show();
            }
        });

        submitButton.setOnClickListener(v -> {
            progressBar = view.findViewById(R.id.progress_exchange);
            progressBar.setVisibility(View.VISIBLE);

            String bkashAccountNumber = bkashAccount.getText().toString().trim();

            databaseReference = FirebaseDatabase.getInstance().getReference();
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            assert firebaseUser != null;
            DatabaseReference bkashNumber = databaseReference.child("Winning Persons").
                    child("User").child(firebaseUser.getUid()).child("UserInformation").child("BkashAccount");

            new Handler().postDelayed(() -> userPointInfoCallBack(value -> {
                if (value >= 10000 && (!bkashAccountNumber.isEmpty())) {

                    // TODO : NEED TO DO SOME WORK HERE

                    bkashNumber.setValue(bkashAccountNumber);
                    databaseReference = FirebaseDatabase.getInstance().getReference("Winning Persons").
                            child("User").child(firebaseUser.getUid()).child("UserInformation").child("Amount");
                    DatabaseReference name = FirebaseDatabase.getInstance().getReference("Winning Persons").
                            child("User").child(firebaseUser.getUid()).child("UserInformation").child("Name");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    name.setValue(user.getDisplayName());

                } else {
                    bkashAccount.setError("Enter a phone number");
                    bkashAccount.requestFocus();
                    Toast.makeText(requireActivity().getApplicationContext(),
                            "Complete the required fields!",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }), 3000);
        });
    }

    private void userPointInfoCallBack(userInfoCallBack userInfoCallBack) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().getReference().
                child("Winning Persons").child(firebaseUser.getUid()).child("Coin Amount");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long longValue = snapshot.getValue(Long.class);
                try {
                    userInfoCallBack.userInfoCall(longValue);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            "Loaded successfully", Toast.LENGTH_SHORT).show();
                }
            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListener);
    }

    @Override
    public void onDestroyView() {
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
        view = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
        view = null;
        super.onDestroy();
    }

    public interface userInfoCallBack {
        void userInfoCall(Long value);
    }

}