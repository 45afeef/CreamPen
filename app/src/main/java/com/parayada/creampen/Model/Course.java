package com.parayada.creampen.Model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;


public class Course implements Parcelable {

    private String id;
    // Title of Course
    private String title;
    // Description for the course
    private String description;
    // Syllabus containing categories,main topic and sub topics
    private String syllabus;
    // Saprate array for educatorId and educatorName
    private ArrayList<String> educatorIds;
    private ArrayList<String> educatorNames;
    // String lesson consist of 0-lessonId 1-lessonName
    private ArrayList<String> lessons;
    // String saved consist of user ids
    private ArrayList<String> savedBy;
    @ServerTimestamp
    public Timestamp timestamp;


    public Course() { }

    protected Course(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        syllabus = in.readString();
        educatorIds = in.createStringArrayList();
        educatorNames = in.createStringArrayList();
        lessons = in.createStringArrayList();
        savedBy = in.createStringArrayList();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(syllabus);
        dest.writeStringList(educatorIds);
        dest.writeStringList(educatorNames);
        dest.writeStringList(lessons);
        dest.writeStringList(savedBy);
        dest.writeParcelable(timestamp, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public ArrayList<String> getEducatorIds() {
        return educatorIds;
    }

    public void setEducatorIds(ArrayList<String> educatorIds) {
        this.educatorIds = educatorIds;
    }

    public ArrayList<String> getEducatorNames() {
        return educatorNames;
    }

    public void setEducatorNames(ArrayList<String> educatorNames) {
        this.educatorNames = educatorNames;
    }

    public ArrayList<String> getLessons() {
        return lessons;
    }

    public void setLessons(ArrayList<String> lessons) {
        this.lessons = lessons;
    }

    public ArrayList<String> getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(ArrayList<String> savedBy) {
        this.savedBy = savedBy;
    }

    public void addEducatorId(String educatorId) {
        if (educatorIds ==  null)
            educatorIds = new ArrayList<>();
        educatorIds.add(educatorId);
    }

    public void addEducatorName(String educatorName) {
        if (educatorNames ==  null)
            educatorNames = new ArrayList<>();
        educatorNames.add(educatorName);
    }

    public void setLesson(int index, String typeIdName) {
        if (this.lessons != null && index >= 0)
            this.lessons.set(index,typeIdName);
    }
}
