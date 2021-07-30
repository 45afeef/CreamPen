package com.parayada.creampen.Model;


import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class McqSet implements Serializable {

    @NonNull
    String question;
    @NonNull
    String answer;

    String option1;
    String option2;
    String option3;
    String option4;

    String topics;

    public McqSet(){}

    public McqSet(String fireBaseString){
        // Prefix Code 101 is for questionSet
        //Split firebaseStin into strin array

        // mySeparator is saved in sting.xml with mySeparator id
        // mySeparator = %4#@!
        String mySeparator= "%4#@!";

        String[] strings = fireBaseString.split(mySeparator);

        question = strings[1];
        option1 = strings[2];
        option2 = strings[3];
        option3 = strings[4];
        option4 = strings[5];
        answer = strings[6];
        topics = strings[7];

    }

    public String getQuestionAsString (){
        // Prefix code 100 is for path of pic slides
        // Prefix Code 101 is for questionSet
        // Prefix Code 102 is for html Text

        // mySeparator is saved in sting.xml with mySeparator id
        // mySeparator = %4#@!
        String mySeparator= "%4#@!";

        if (question.isEmpty()) return null;

        return "101" + mySeparator+
                question + mySeparator+
                option1 + mySeparator+
                option2 + mySeparator+
                option3 + mySeparator+
                option4 + mySeparator+
                answer + mySeparator+
                topics;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4){
        this.option4 = option4;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public ArrayList<String> getTopics() {
        ArrayList<String> topicList = new ArrayList<>();
        topicList.addAll(Arrays.asList(topics.split(",")));
        return topicList;
    }

    public String getTopicsAsString(){
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }
}