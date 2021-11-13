package com.defenderstudio.geeksjob;

public class LeaderBoardUser {

    String userName;
    long pointsValue;

    public LeaderBoardUser(){}

    public LeaderBoardUser(String userName, long pointsValue) {
        this.userName = userName;
        this.pointsValue = pointsValue;

    }

    public String getUserName() {
        return userName;
    }

    public long getPointsValue() {
        return pointsValue;
    }




}
