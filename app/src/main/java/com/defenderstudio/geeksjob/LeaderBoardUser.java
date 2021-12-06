package com.defenderstudio.geeksjob;

import android.util.Log;

public class LeaderBoardUser {

    String userName;
    long pointsValue;
    String imageUrl;

    public LeaderBoardUser() {
    }

    public LeaderBoardUser(String userName, long pointsValue, String imageUrl) {
        this.userName = userName;
        this.pointsValue = pointsValue;
        this.imageUrl = imageUrl;

    }

    public String getUserName() {
        return userName;
    }

    public long getPointsValue() {
        return pointsValue;
    }

    public String getImageUrl(){
        Log.d("a","user//// Got user image : "+imageUrl);
        return imageUrl;

    }

}
