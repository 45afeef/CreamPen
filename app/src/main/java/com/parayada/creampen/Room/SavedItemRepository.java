package com.parayada.creampen.Room;

        import android.app.Application;

        import androidx.lifecycle.LiveData;

        import com.parayada.creampen.Model.SavedItem;

        import java.util.List;

public class SavedItemRepository {


    private SaveItemDao mSaveItemDao;
    private LiveData<List<SavedItem>> mAllItems;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    SavedItemRepository(Application application) {
        CreampenRoomDataBase db = CreampenRoomDataBase.getDatabase(application);
        mSaveItemDao = db.saveItemDao();
        mAllItems = mSaveItemDao.getAllSavedItem();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<SavedItem>> getmAllItems() {
        return mAllItems;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(final SavedItem item) {
        CreampenRoomDataBase.databaseWriteExecutor.execute(() -> {
            mSaveItemDao.insert(item);
        });
    }

    void deleteAll(){
        CreampenRoomDataBase.databaseWriteExecutor.execute(() -> {
            mSaveItemDao.deleteAll();
        });
    }

    LiveData<SavedItem> getItemByidAndType(SavedItem item) {
        return mSaveItemDao.getItemByidAndType(item.getItemId(),item.getItemType());
    }

    public void delete(SavedItem item) {
        CreampenRoomDataBase.databaseWriteExecutor.execute(() -> {
            mSaveItemDao.delete(item);
        });
    }
}
