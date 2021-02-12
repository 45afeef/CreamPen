package com.parayada.creampen.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Topic {


    private String name;
    private ArrayList<Topic> subTopics;

    // Empty Constructor
    public Topic(){}

    // Constructor with topic name
    public Topic(String name) {
        this.name = name;
    }


    // Getter and Setter for values
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Topic> getSubTopics() {
        return subTopics;
    }

    private void setSubTopics(ArrayList<Topic> subTopics) {
        this.subTopics = subTopics;
    }

    // Method to add new subTopic in mainTopic
    public void addNewSubTopic(Topic topic){
        if (this.subTopics == null)
            this.subTopics = new ArrayList<>();
        this.subTopics.add(topic);
    }

    // Convert a syllabus String into ArrayList of Topic
    public ArrayList<Topic> stringToList(String string){

        ArrayList<String> catList = new ArrayList<>();

        int curlyCount = -1;
        int start = 0;
        for (int i = 0; i < string.length(); i++) {

            switch (string.charAt(i)) {
                case '{':

                    if (curlyCount < 0) curlyCount = 0;

                    curlyCount++;

                    break;
                case '}':

                    curlyCount--;

                    break;
                default:

                    break;
            }

            if (curlyCount == 0) {
                String cat = string.substring(start, i + 1);
                catList.add(cat);
                start = i + 1;
                curlyCount = -1;
            }

        }


        ArrayList<Topic> topicArrayList = new ArrayList<>();
        for (String s : catList) {

            topicArrayList.add(stringToTopic(s));

        }
        Collections.sort(topicArrayList, (t1, t2) -> t1.getName().compareTo(t2.getName()));
        return topicArrayList;
    }

    // Return Topic object created from topic String
    private Topic stringToTopic(String topicString) {

        int index = topicString.indexOf('{');

        Topic topic = new Topic();
        topic.setName(topicString.substring(0,index).trim());
        topic.setSubTopics(stringToList(topicString.substring(index + 1, topicString.length()-1)));

        return topic;
    }

    // Convert a ArrayList of Topic into single syllabus String
    public String topicListToString(ArrayList<Topic> topics){
        StringBuilder builder = new StringBuilder();

        for (Topic topic:topics) {
            builder.append(topic.getName());
            builder.append("{");
            if (topic.getSubTopics() != null)
                builder.append(topicListToString(topic.getSubTopics()));
            builder.append("}");

        }

        return builder.toString();
    }

    public int getTopicLevel(String syllabusString) {
        int topicLevel = 0;
        int curlyCount = 0;
        for (Character c:syllabusString.toCharArray()) {
            switch (c) {
                case '{':
                    curlyCount++;
                    break;
                case '}':
                    curlyCount--;
                    break;
            }
            if (curlyCount>topicLevel) topicLevel = curlyCount;
        }
        return  topicLevel;
    }
}
