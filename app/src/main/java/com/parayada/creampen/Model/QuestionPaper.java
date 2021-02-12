package com.parayada.creampen.Model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;

public class QuestionPaper implements Parcelable {

    private String name, instruction;
    private int maxMark;
    private int maxTime = 0;
    private long date = 0L;
    private ArrayList<String> questions;
    // String educator consist of 0-educatorId 1-educatorName
    private ArrayList<String> educatorNames;
    private ArrayList<String> educatorIds;
    @ServerTimestamp
    public Timestamp timestamp;


    //Constructor
    public QuestionPaper(){}

    protected QuestionPaper(Parcel in) {
        name = in.readString();
        instruction = in.readString();
        maxMark = in.readInt();
        maxTime = in.readInt();
        date = in.readLong();
        questions = in.createStringArrayList();
        educatorIds = in.createStringArrayList();
        educatorNames = in.createStringArrayList();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(instruction);
        dest.writeInt(maxMark);
        dest.writeInt(maxTime);
        dest.writeLong(date);
        dest.writeStringList(questions);
        dest.writeStringList(educatorIds);
        dest.writeStringList(educatorNames);
        dest.writeParcelable(timestamp, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QuestionPaper> CREATOR = new Creator<QuestionPaper>() {
        @Override
        public QuestionPaper createFromParcel(Parcel in) {
            return new QuestionPaper(in);
        }

        @Override
        public QuestionPaper[] newArray(int size) {
            return new QuestionPaper[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public int getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(int maxMark) {
        this.maxMark = maxMark;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public ArrayList<String> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<String> questions) {
        this.questions = questions;
    }

    public ArrayList<String> getEducatorNames() {
        return educatorNames;
    }

    public void setEducatorNames(ArrayList<String> educatorNames) {
        this.educatorNames = educatorNames;
    }

    public ArrayList<String> getEducatorIds() {
        return educatorIds;
    }

    public void setEducatorIds(ArrayList<String> educatorIds) {
        this.educatorIds = educatorIds;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

}
