package com.parayada.creampen.Model;

import java.util.ArrayList;

public class Stream {


    String name,
            syllabus;
    ArrayList<String> pyqs;

    public Stream(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public ArrayList<String> getPyqs() {
        return pyqs;
    }

    public void setPyqs(ArrayList<String> pyqs) {
        this.pyqs = pyqs;
    }
}
