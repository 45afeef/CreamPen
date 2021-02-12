package com.parayada.creampen.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;

public class Lesson implements Parcelable {


    private String id;
    private String title;
    private ArrayList<String > SlideArrayList;
    private ArrayList<String> SlideChangeList;
    private String audioLink;
    private String courseId;
    private String courseTitle;
    private String userName;
    private String userId;
    private String userPic;
    private ArrayList<String> topics;
    @ServerTimestamp
    public Timestamp timestamp;


    public Lesson(){}

    protected Lesson(Parcel in) {
        id = in.readString();
        title = in.readString();
        SlideArrayList = in.createStringArrayList();
        SlideChangeList = in.createStringArrayList();
        audioLink = in.readString();
        courseId = in.readString();
        courseTitle = in.readString();
        userName = in.readString();
        userId = in.readString();
        userPic = in.readString();
        topics = in.createStringArrayList();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeStringList(SlideArrayList);
        dest.writeStringList(SlideChangeList);
        dest.writeString(audioLink);
        dest.writeString(courseId);
        dest.writeString(courseTitle);
        dest.writeString(userName);
        dest.writeString(userId);
        dest.writeString(userPic);
        dest.writeStringList(topics);
        dest.writeParcelable(timestamp, flags);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getSlideArrayList() {
        return SlideArrayList;
    }

    public void setSlideArrayList(ArrayList<String> slideArrayList) {
        SlideArrayList = slideArrayList;
    }

    public ArrayList<String> getSlideChangeList() {
        return SlideChangeList;
    }

    public void setSlideChangeList(ArrayList<String> slideChangeList) {
        SlideChangeList = slideChangeList;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String coureseTitle) {
        this.courseTitle = coureseTitle;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<String> topics) {
        this.topics = topics;
    }
}