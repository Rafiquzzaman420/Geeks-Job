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
        TextView communityTitle = findViewById(R.id.community_group_title);
        TextView headerText = findViewById(R.id.header_text);
        TextView footerText = findViewById(R.id.footer_text);
        TextView facebookLink = findViewById(R.id.facebook_link);
        TextView telegramLink = findViewById(R.id.telegram_link);
        TextView youtubeLink = findViewById(R.id.youtube_link);


        communityTitle.setTextSize(convertFromDp(38));
        headerText.setTextSize(convertFromDp(32));
        footerText.setTextSize(convertFromDp(32));
        facebookLink.setTextSize(convertFromDp(28));
        telegramLink.setTextSize(convertFromDp(28));
        youtubeLink.setTextSize(convertFromDp(28));

        facebookLink.setMovementMethod(LinkMovementMethod.getInstance());
        telegramLink.setMovementMethod(LinkMovementMethod.getInstance());
        youtubeLink.setMovementMethod(LinkMovementMethod.getInstance());
    }


    public float convertFromDp(int input) {
        final float scale = getResources().getDisplayMetrics().density;
        return ((input - 0.7f) / scale);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CommunityGroup.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
