package com.parayada.creampen.Room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.parayada.creampen.Model.SavedItem;

import java.util.List;


@Dao
public interface SaveItemDao {

    // allowing the insert of the same question multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(SavedItem item);

    @Query("DELETE FROM savedItem_table")
    void deleteAll();

    @Query("SELECT * from savedItem_table ORDER BY id DESC")
    LiveData<List<SavedItem>> getAllSavedItem();

    @Query("SELECT EXISTS(SELECT * from savedItem_table WHERE itemId= :itemId & itemType= :itemType)")
    LiveData<Boolean> isSaved(String itemId, String itemType);

    @Query("SELECT * from savedItem_table WHERE itemId= :itemId AND itemType = :itemType")
    LiveData<SavedItem> getItemByidAndType(String itemId,String itemType);

    @Delete()
    void delete(SavedItem item);
}