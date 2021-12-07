package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// TODO : NEED TO ADD USER MESSAGE SYSTEM DIRECTLY
public class NoticeBoard extends AppCompatActivity {

    private final String child1 = "Card 1";
    private final String child2 = "Card 2";
    CardView noticeBoardCardView1, noticeBoardCardView2, userNotice;
    TextView noticeHeader1, noticeHeader2, userNoticeHeader,
            noticeBody1, noticeBody2, userNoticeBody;
    private String userNoticeChildNode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_board);

        noticeBoardCardView1 = findViewById(R.id.notice_board_card_1);
        noticeBoardCardView2 = findViewById(R.id.notice_board_card_2);
        userNotice = findViewById(R.id.user_notice_card_view);


        noticeHeader1 = findViewById(R.id.notice_board_title_1);
        noticeHeader2 = findViewById(R.id.notice_board_title_2);
        userNoticeHeader = findViewById(R.id.user_notice_title);


        noticeBody1 = findViewById(R.id.notice_board_text_1);
        noticeBody2 = findViewById(R.id.notice_board_text_2);
        userNoticeBody = findViewById(R.id.user_notice_body);


        FirebaseUser userInfo = FirebaseAuth.getInstance().getCurrentUser();
        assert userInfo != null;
        userNoticeChildNode = userInfo.getUid();


        noticeBoardInfoChecker(info -> {
            switch (info) {
                case "1":
                    noticeBoardCardView1.setVisibility(View.VISIBLE);
                    getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                    userNotice.setVisibility(View.VISIBLE);
                    getNoticeBoardCardViewInfo(userNoticeChildNode, userNoticeHeader, userNoticeBody);
                    break;
                case "12":
                    noticeBoardCardView1.setVisibility(View.VISIBLE);
                    noticeBoardCardView2.setVisibility(View.VISIBLE);
                    getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                    getNoticeBoardCardViewInfo(child2, noticeHeader2, noticeBody2);
                    break;
            }
        });

    }

    private void noticeboardInfoCollector(String child, noticeOneInfoCollector collector) {
        DatabaseReference noticeHeader = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child(child).
                child("Notice Header");

        DatabaseReference noticeBody = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child(child).
                child("Notice Body");

        noticeHeader.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String noticeHeader = snapshot.getValue(String.class);
                collector.noticeHeader(noticeHeader);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        noticeBody.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String noticeBody = snapshot.getValue(String.class);
                collector.noticeBody(noticeBody);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void noticeBoardInfoChecker(noticeInfoChecker info) {
        DatabaseReference noticeInfo = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child("Cards");
        noticeInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String infoValue = snapshot.getValue(String.class);
                try {
                    info.noticeInfo(infoValue);
                } catch (Exception e) {
                    info.noticeInfo("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getNoticeBoardCardViewInfo(String child, TextView noticeViewHead, TextView noticeViewBody) {
        noticeboardInfoCollector(child, new noticeOneInfoCollector() {
            @Override
            public void noticeHeader(String noticeHeader) {
                if (child.equals("Card 1") || child.equals("Card 2")) {
                    noticeViewHead.setText(noticeHeader);
                } else {
                    userNotice.setVisibility(View.GONE);
                }
            }

            @Override
            public void noticeBody(String noticeBody) {
                if (child.equals("Card 1") || child.equals("Card 2")) {
                    noticeViewBody.setText(noticeBody);
                } else {
                    userNotice.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NoticeBoard.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private interface noticeOneInfoCollector {
        void noticeHeader(String noticeHeader);

        void noticeBody(String noticeBody);
    }

    private interface noticeInfoChecker {
        void noticeInfo(String info);
    }
}
