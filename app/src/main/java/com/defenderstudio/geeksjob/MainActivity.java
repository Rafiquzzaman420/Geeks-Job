package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView user_name_text_view;
    ImageView user_image;
    GoogleSignInClient googleSignInClient;
    FirebaseUser firebaseUser;
    String BREAK = "BREAK";
    String updating = "Updating";
    DatabaseReference databaseReference, userSignInInfoReference;
    ValueEventListener eventListener;
    ProgressDialog progressDialog;
    Handler handler = new Handler();
    Dialog dialog;
    View headerView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth.getInstance().getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        readVersionInformationFromFirebase(value -> {
            // Always use application BuildConfig from package
            long appVersion = 9;
            if (value != appVersion) {
                progressDialog = new ProgressDialog(MainActivity.this, R.style.ProgressDialogStyle);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Checking version...");
                progressDialog.show();
                handler.postDelayed(() -> {
                    userSignInInformationSendToServer(BREAK);
                    userSignOutInformationSendToServer();
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),
                            "New version found! Please update.", Toast.LENGTH_LONG).show();
                    Intent logOut = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(logOut);
                    finish();
                }, 2000);
            }
        });

        readBannedInformationFromFirebase(value -> {
            if (value != null) {
                if (value) {
                    progressDialog = new ProgressDialog(MainActivity.this, R.style.ProgressDialogStyle);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Checking validity of your account...");
                    progressDialog.show();
                    handler.postDelayed(() -> {
                        userSignInInformationSendToServer(BREAK);
                        userSignOutInformationSendToServer();
                        FirebaseAuth.getInstance().signOut();
                        googleSignInClient.signOut();
                        Toast.makeText(getApplicationContext(),
                                "Your Account has been banned!",
                                Toast.LENGTH_LONG).show();
                        Intent logOut = new Intent(getApplicationContext(), SignInActivity.class);
                        startActivity(logOut);
                        finish();
                    }, 2000);
                }
            }
        });


        readUpdateInformationFromFirebase(value -> handler.postDelayed(() -> {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (value != null) {
                if (value.equals(updating)) {
                    developerAccess(stringValue -> {
                        if (!stringValue.equals(firebaseUser.getUid())){
                            progressDialog = new ProgressDialog(MainActivity.this, R.style.ProgressDialogStyle);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Checking app status...");
                            progressDialog.show();
                            handler.postDelayed(() -> {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(getApplicationContext(),
                                        "App is being updated. Please wait and try again later...",
                                        Toast.LENGTH_LONG).show();
                                Intent logOut = new Intent(MainActivity.this, SignInActivity.class);
                                startActivity(logOut);
                                finish();
                            }, 3000);

                        }
                    });
                }
            }
        }, 3000));

        @SuppressLint("HardwareIds")
        String ANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        userSignInInformationCallBack(value -> {
            // If value doesn't match with the device ID then it'll sign out the user
            if (!value.equals(ANDROID_ID) && !value.equals("NULL")) {
                FirebaseAuth.getInstance().signOut();
                googleSignInClient.signOut();
                Toast.makeText(getApplicationContext(),
                        "Use another account or sign out from other device.",
                        Toast.LENGTH_LONG).show();
                Intent logOut = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(logOut);
                finish();
            } else {
                // Otherwise it'll do nothing and send the information to the server
                if (BREAK.equals("BREAK")) {
                    BREAK = "DO NOTHING";
                    userSignInInformationSendToServer(BREAK);
                }
            }
        });

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.open, R.string.close);

        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1096631451357-nm97s6b45hllm30r8ij6935bdg40seii.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Home()).commit();
            navigationView.setCheckedItem(R.id.home);

        }
        try {
            headerView = navigationView.getHeaderView(0);
            user_name_text_view = headerView.findViewById(R.id.user_name);
            user_image = headerView.findViewById(R.id.user_image);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                for (UserInfo profile : user.getProviderData()) {

                    // Name, email address, and profile photo Url
                    String name = profile.getDisplayName();
                    Uri photo = profile.getPhotoUrl();
                    user_name_text_view.setText(name);
                    Glide.with(this).load(photo).apply(RequestOptions.circleCropTransform()).into(user_image);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error Occurred...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        String BREAK = "BREAK";
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    userSignInInformationSendToServer(BREAK);
                    userSignOutInformationSendToServer();
                    FirebaseAuth.getInstance().signOut();
                    MainActivity.super.onBackPressed();
                }).setNegativeButton("No", null)
                .show();

    }

    @Override
    protected void onPause() {
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
        super.onPause();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Home())
                        .commit();
                break;
            case R.id.exchange:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Exchange()).commit();
                break;
            case R.id.faq:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Faq())
                        .commit();
                break;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Download Geeks Job and Win Real Money. Link: https://play.google.com/store/apps/details?id=com.defenderstudio.geeksjob";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case R.id.notice_board_id:
                Intent noticeIntent = new Intent(MainActivity.this, NoticeBoard.class);
                startActivity(noticeIntent);
                finish();
                break;
            case R.id.facebook_group:
                Intent groupIntent = new Intent(MainActivity.this, CommunityGroup.class);
                startActivity(groupIntent);
                finish();
                break;
            case R.id.sign_out:
                userSignOutInformationSendToServer();
                FirebaseAuth.getInstance().signOut();
                googleSignInClient.signOut().addOnCompleteListener(this,
                        task -> {
                            Intent signOutIntent = new Intent(MainActivity.this, SignInActivity.class);
                            startActivity(signOutIntent);
                            finish();
                        });
                Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void userSignInInformationCallBack(MainActivity.userSignInInformation userSignInInformation) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        assert firebaseUser != null;
        userSignInInfoReference = databaseReference.child("User Information").
                child("Users").child("Sign In Info").child(firebaseUser.getUid()).child("Info");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String stringValue = snapshot.getValue(String.class);
                try {
                    userSignInInformation.userSignInInfo(stringValue);
                } catch (Exception e) {
                    userSignInInformation.userSignInInfo("NULL");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userSignInInfoReference.addListenerForSingleValueEvent(eventListener);
    }

    private void developerAccess(giveDeveloperAccess access){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userSignInInfoReference = databaseReference.child("Application Status").
                child("Developer Access");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String stringValue = snapshot.getValue(String.class);
                try {
                    access.access(stringValue);
                } catch (Exception e) {
                    access.access("null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userSignInInfoReference.addListenerForSingleValueEvent(eventListener);
    }

    private void userSignOutInformationSendToServer() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference signOutInfoReference = databaseReference.child("User Information").
                child("Users").child("Sign In Info").child(firebaseUser.getUid()).child("Info");

        signOutInfoReference.setValue("NULL");
    }

    private void userSignInInformationSendToServer(String endMethod) {
        if (!endMethod.equals("BREAK")) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            assert firebaseUser != null;
            DatabaseReference signInInformation = databaseReference.child("User Information").
                    child("Users").child("Sign In Info").child(firebaseUser.getUid()).child("Info");
            @SuppressLint("HardwareIds")
            String ANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            signInInformation.setValue(ANDROID_ID);
        }
    }

    public void readUpdateInformationFromFirebase(MainActivity.readUpdateInformation readUpdateInformation) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().
                getReference("Application Status").child("Status");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String stringValue = snapshot.getValue(String.class);
                try {
                    readUpdateInformation.readUpdateInfo(stringValue);
                } catch (Exception e) {
                    readUpdateInformation.readUpdateInfo("Running");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListener);
    }

    private void readVersionInformationFromFirebase(MainActivity.readVersionInformation readVersionInformation) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().
                getReference("Application Status").child("Version");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double versionValue = snapshot.getValue(Double.class);
                readVersionInformation.readVersionInfo(versionValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListener);
    }

    private void readBannedInformationFromFirebase(MainActivity.userBanInformation userBanInformation) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().
                getReference("Banned User Info").child("Users");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userBanInformation.userBanInfo(snapshot.hasChild(firebaseUser.getUid()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListener);
    }


    @Override
    protected void onDestroy() {
        closingCodes();
        super.onDestroy();
    }

    private void closingCodes() {
        handler.removeCallbacksAndMessages(null);
        dialog = new Dialog(MainActivity.this, R.style.dialogue);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (databaseReference != null && userSignInInfoReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
            userSignInInfoReference.removeEventListener(eventListener);
        }
        progressDialog = new ProgressDialog(MainActivity.this, R.style.ProgressDialogStyle);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        headerView = null;
    }

    @Override
    protected void onStop() {
        closingCodes();
        super.onStop();
    }

    private interface userBanInformation {
        void userBanInfo(Boolean value);
    }

    private interface userSignInInformation {
        void userSignInInfo(String value);
    }

    public interface giveDeveloperAccess{
        void access(String value);
    }

    public interface readUpdateInformation {
        void readUpdateInfo(String value);
    }

    public interface readVersionInformation {
        void readVersionInfo(Double value);
    }
}