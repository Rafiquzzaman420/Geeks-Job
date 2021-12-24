package com.defenderstudio.geeksjob;

public class CurriculumQuestions {

    String moviesQuestion;
    String moviesOption1;
    String moviesOption2;
    String moviesOption3;
    String moviesOption4;
    String moviesAnswer;

    //==================================================================================================
//                                      Empty constructor
//==================================================================================================
    public CurriculumQuestions() {
    }


    //==================================================================================================
//                                       Constructor
//==================================================================================================
    public CurriculumQuestions(String moviesQuestion, String moviesOption1, String moviesOption2,
                               String moviesOption3, String moviesOption4, String moviesAnswer) {
        this.moviesQuestion = moviesQuestion;
        this.moviesOption1 = moviesOption1;
        this.moviesOption2 = moviesOption2;
        this.moviesOption3 = moviesOption3;
        this.moviesOption4 = moviesOption4;
        this.moviesAnswer = moviesAnswer;
    }

    //==================================================================================================
    // Getter and setter methods starts here
//==================================================================================================
    public String getMoviesQuestion() {
        return moviesQuestion;
    }

    public void setMoviesQuestion(String moviesQuestion) {
        this.moviesQuestion = moviesQuestion;
    }

    public String getMoviesOption1() {
        return moviesOption1;
    }

    public void setMoviesOption1(String moviesOption1) {
        this.moviesOption1 = moviesOption1;
    }

    public String getMoviesOption2() {
        return moviesOption2;
    }

    public void setMoviesOption2(String moviesOption2) {
        this.moviesOption2 = moviesOption2;
    }

    public String getMoviesOption3() {
        return moviesOption3;
    }

    public void setMoviesOption3(String moviesOption3) {
        this.moviesOption3 = moviesOption3;
    }

    public String getMoviesOption4() {
        return moviesOption4;
    }

    public void setMoviesOption4(String moviesOption4) {
        this.moviesOption4 = moviesOption4;
    }

    public String getMoviesAnswer() {
        return moviesAnswer;
    }

    public void setMoviesAnswer(String moviesAnswer) {
        this.moviesAnswer = moviesAnswer;
    }

//==================================================================================================


}