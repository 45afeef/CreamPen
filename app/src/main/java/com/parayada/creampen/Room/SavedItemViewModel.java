package com.parayada.creampen.Room;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.parayada.creampen.Model.SavedItem;

import java.util.List;

public class SavedItemViewModel extends AndroidViewModel {

    private SavedItemRepository mRepository;

    private LiveData<List<SavedItem>> mAllItems;

    public SavedItemViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SavedItemRepository(application);
        mAllItems = mRepository.getmAllItems();
    }

    public LiveData<List<SavedItem>> getAllItems() { return mAllItems; }

    public void insert(SavedItem item) { mRepository.insert(item); }

    public void deleteAll() { mRepository.deleteAll();}


    public LiveData<SavedItem> getItemByIdAndType(SavedItem item) {
        return  mRepository.getItemByidAndType(item);
    }

    public void delete(SavedItem item) {
        mRepository.delete(item);
    }
}