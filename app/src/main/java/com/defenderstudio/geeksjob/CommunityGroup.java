package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CommunityGroup extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_group);

        TextView facebookLink = findViewById(R.id.facebook_link);
        TextView telegramLink = findViewById(R.id.telegram_link);
        TextView youtubeLink = findViewById(R.id.youtube_link);

        facebookLink.setMovementMethod(LinkMovementMethod.getInstance());
        telegramLink.setMovementMethod(LinkMovementMethod.getInstance());
        youtubeLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CommunityGroup.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
