package com.defenderstudio.geeksjob;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// TODO : NEED TO ADD USER MESSAGE SYSTEM DIRECTLY
public class NoticeBoard extends AppCompatActivity {

    private final String child1 = "Card 1";
    private final String child2 = "Card 2";
    private final String child3 = "Card 3";
    CardView noticeBoardCardView1, noticeBoardCardView2, noticeBoardCardView3;
    TextView noticeHeader1, noticeHeader2, noticeHeader3,
            noticeBody1, noticeBody2, noticeBody3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_board);

        noticeBoardCardView1 = findViewById(R.id.notice_board_card_1);
        noticeBoardCardView2 = findViewById(R.id.notice_board_card_2);
        noticeBoardCardView3 = findViewById(R.id.notice_board_card_3);


        noticeHeader1 = findViewById(R.id.notice_board_title_1);
        noticeHeader2 = findViewById(R.id.notice_board_title_2);
        noticeHeader3 = findViewById(R.id.notice_board_title_3);


        noticeBody1 = findViewById(R.id.notice_board_text_1);
        noticeBody2 = findViewById(R.id.notice_board_text_2);
        noticeBody3 = findViewById(R.id.notice_board_text_3);


        noticeBoardInfoChecker(info -> {
            switch (info) {
                case "1":
                    noticeBoardCardView1.setVisibility(View.VISIBLE);
                    getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                    break;
                case "12":
                    noticeBoardCardView1.setVisibility(View.VISIBLE);
                    noticeBoardCardView2.setVisibility(View.VISIBLE);
                    getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                    getNoticeBoardCardViewInfo(child2, noticeHeader2, noticeBody2);

                    break;
                case "123":
                    noticeBoardCardView1.setVisibility(View.VISIBLE);
                    noticeBoardCardView2.setVisibility(View.VISIBLE);
                    noticeBoardCardView3.setVisibility(View.VISIBLE);
                    getNoticeBoardCardViewInfo(child1, noticeHeader1, noticeBody1);
                    getNoticeBoardCardViewInfo(child2, noticeHeader2, noticeBody2);
                    getNoticeBoardCardViewInfo(child3, noticeHeader3, noticeBody3);
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
                }catch (Exception e){
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
                noticeViewHead.setText(noticeHeader);
            }

            @Override
            public void noticeBody(String noticeBody) {
                noticeViewBody.setText(noticeBody);
            }
        });
    }

    private interface noticeOneInfoCollector {
        void noticeHeader(String noticeHeader);

        void noticeBody(String noticeBody);
    }

    private interface noticeInfoChecker {
        void noticeInfo(String info);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
