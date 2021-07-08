package com.parayada.creampen.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;

public class AnswerPaper {

    private String studentId;
    private String studentName;
    private String questionPaperId;
    private String questionPaperName;
    private ArrayList<String> questions;
    private ArrayList<String> answers;
    private long timeLeft;
    private int maxTime;
    @ServerTimestamp
    public Timestamp timestamp;

    public AnswerPaper() {}

    public AnswerPaper(String studentId, String studentName, String questionPaperId, String getQuestionPaperName, ArrayList<String> questions, ArrayList<String> answers, int timeLeft, int maxTime) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.questionPaperId = questionPaperId;
        this.questionPaperName = getQuestionPaperName;
        this.questions = questions;
        this.answers = answers;
        this.timeLeft = timeLeft;
        this.maxTime = maxTime;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getQuestionPaperId() {
        return questionPaperId;
    }

    public void setQuestionPaperId(String questionPaperId) {
        this.questionPaperId = questionPaperId;
    }

    public String getQuestionPaperName() {
        return questionPaperName;
    }

    public void setQuestionPaperName(String getQuestionPaperName) {
        this.questionPaperName = getQuestionPaperName;
    }

    public ArrayList<String> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<String> questions) {
        this.questions = questions;
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<String> answers) {
        this.answers = answers;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

}
