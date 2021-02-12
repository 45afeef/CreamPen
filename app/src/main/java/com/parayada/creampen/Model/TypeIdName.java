package com.parayada.creampen.Model;



import java.util.ArrayList;

public class TypeIdName {

    private int type;
    private String id;
    private String name;

    public TypeIdName(){}

    public TypeIdName(int type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    private TypeIdName getTypeIdName(String firebaseString){
        String sep = "%4#@!";// Found R.string.mySeparator
        //Split firebaseStin into string array
        String[] strings = firebaseString.split(sep);

        type = Integer.parseInt(strings[0]);
        id = strings[1];
        name = strings[2];

        return this;
    }

    public ArrayList<TypeIdName> toTypeIdNameArrayList(ArrayList<String> strings) {
        if (strings == null) return  null;

        ArrayList<TypeIdName> idNameArrayList = new ArrayList<>();
        for (String string : strings)
            idNameArrayList.add(new TypeIdName().getTypeIdName(string));
        return idNameArrayList;

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
