package com.parayada.creampen.Model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "savedItem_table")
public class SavedItem {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    String itemType;
    String itemId;
    String itemTitle;

    public SavedItem(){}

    public String getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

}
