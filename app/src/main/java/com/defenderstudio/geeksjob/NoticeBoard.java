package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class NoticeBoard extends AppCompatActivity {

    private final String child1 = "Card 1";
    private final String child2 = "Card 2";
    CardView noticeBoardCardView1, noticeBoardCardView2, userNotice;
    TextView noticeHeader1, noticeHeader2, userNoticeHeader,
            noticeBody1, noticeBody2, userNoticeBody;
    FirebaseUser userInfo;
    DatabaseReference noticeBody, noticeHeader, databaseReference;
    ValueEventListener eventListener, noticeBodyListener, noticeHeaderListener;
    private String googleUserId;

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


        userInfo = FirebaseAuth.getInstance().getCurrentUser();
        assert userInfo != null;
        googleUserId = userInfo.getUid();
        Log.d("a", "user//// User's ID is : " + googleUserId);

        userNoticeValidator(collector -> {
            if (!googleUserId.equals(collector)) {
                userNotice.setVisibility(View.GONE);
                noticeBoardInfoChecker(info -> {
                    switch (info) {
                        case "1":
                            noticeBoardCardView1.setVisibility(View.VISIBLE);
                            getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                            getNoticeBoardCardViewInfo(googleUserId, userNoticeHeader, userNoticeBody);
                            break;
                        case "12":
                            noticeBoardCardView1.setVisibility(View.VISIBLE);
                            noticeBoardCardView2.setVisibility(View.VISIBLE);
                            getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                            getNoticeBoardCardViewInfo(child2, noticeHeader2, noticeBody2);
                            getNoticeBoardCardViewInfo(googleUserId, userNoticeHeader, userNoticeBody);
                            break;
                    }
                });

            } else {
                noticeBoardInfoChecker(info -> {
                    switch (info) {
                        case "1":
                            noticeBoardCardView1.setVisibility(View.VISIBLE);
                            getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                            getNoticeBoardCardViewInfo(googleUserId, userNoticeHeader, userNoticeBody);
                            break;
                        case "12":
                            noticeBoardCardView1.setVisibility(View.VISIBLE);
                            noticeBoardCardView2.setVisibility(View.VISIBLE);
                            getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                            getNoticeBoardCardViewInfo(child2, noticeHeader2, noticeBody2);
                            getNoticeBoardCardViewInfo(googleUserId, userNoticeHeader, userNoticeBody);
                            break;
                    }
                });


            }
        });
    }

    private void noticeboardInfoCollector(String child, noticeOneInfoCollector collector) {
        noticeHeader = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child(child).
                child("Notice Header");

        noticeBody = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child(child).
                child("Notice Body");

        noticeHeaderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String noticeHeader = snapshot.getValue(String.class);
                collector.noticeHeader(noticeHeader);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        noticeBodyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String noticeBody = snapshot.getValue(String.class);
                collector.noticeBody(noticeBody);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        noticeHeader.addValueEventListener(noticeHeaderListener);
        noticeBody.addValueEventListener(noticeBodyListener);
    }

    private void noticeBoardInfoChecker(noticeInfoChecker info) {
        databaseReference = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child("Cards");
        eventListener = new ValueEventListener() {
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
        };
        databaseReference.addValueEventListener(eventListener);
    }

    private void userNoticeValidator(userNoticeCollector noticeCollector) {
        userInfo = FirebaseAuth.getInstance().getCurrentUser();
        assert userInfo != null;
        googleUserId = userInfo.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().
                child("Notice Board").
                child(googleUserId).child("ID");

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String value = snapshot.getValue(String.class);
                    noticeCollector.noticeCollector(value);
                } catch (Exception e) {
                    noticeCollector.noticeCollector("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseReference.addValueEventListener(eventListener);
    }

    private void getNoticeBoardCardViewInfo(String child, TextView noticeViewHead, TextView noticeViewBody) {
        noticeboardInfoCollector(child, new noticeOneInfoCollector() {
            @Override
            public void noticeHeader(String noticeHeader) {
                if (child.equals("Card 1") || child.equals("Card 2") || child.equals(userInfo.getUid())) {
                    noticeViewHead.setText(noticeHeader);
                }
            }

            @Override
            public void noticeBody(String noticeBody) {
                if (child.equals("Card 1") || child.equals("Card 2") || child.equals(userInfo.getUid())) {
                    noticeViewBody.setText(noticeBody);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (databaseReference != null
                && noticeBody != null
                && noticeHeader != null
                && eventListener != null
                && noticeHeaderListener != null
                && noticeBodyListener != null) {
            databaseReference.removeEventListener(eventListener);
            noticeHeader.removeEventListener(noticeHeaderListener);
            noticeBody.removeEventListener(noticeBodyListener);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NoticeBoard.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private interface userNoticeCollector {
        void noticeCollector(String collector);
    }

    private interface noticeOneInfoCollector {
        void noticeHeader(String noticeHeader);

        void noticeBody(String noticeBody);
    }

    private interface noticeInfoChecker {
        void noticeInfo(String info);
    }
}
