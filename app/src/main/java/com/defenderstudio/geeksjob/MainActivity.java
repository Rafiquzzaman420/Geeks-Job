package com.defenderstudio.geeksjob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    TextView user_name_text_view;
    ImageView user_image;
    GoogleSignInClient mGoogleSignInClient;
    String earningPointAmount;
    TextView total_earning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);
        total_earning = findViewById(R.id.total_earning);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Home()).commit();
            navigationView.setCheckedItem(R.id.home);

        }
        try {
            View headerView = navigationView.getHeaderView(0);
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> MainActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
        overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Home())
                        .addToBackStack(null).commit();
                break;
            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                        new Profile()).addToBackStack(null).commit();
                break;
            case R.id.exchange:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Exchange()).
                        addToBackStack(null).commit();
                break;
            case R.id.faq:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new Faq())
                        .addToBackStack(null).commit();
                break;
            case R.id.share:
                Toast.makeText(this, R.string.share, Toast.LENGTH_SHORT).show();
                break;
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        task -> {
                            Intent signOutIntent = new Intent(MainActivity.this, SignInActivity.class);
                            startActivity(signOutIntent);
                        });


                Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


}