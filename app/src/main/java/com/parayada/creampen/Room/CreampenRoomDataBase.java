package com.parayada.creampen.Room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.parayada.creampen.Model.SavedItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Each entity corresponds to a table
@Database(entities = {SavedItem.class}, version = 1, exportSchema = false)
public abstract class CreampenRoomDataBase extends androidx.room.RoomDatabase {

    //You make database provides its DAOs by creating an abstract "getter" method for each @Dao.
    public abstract SaveItemDao saveItemDao();
  //  public abstract LessonDao lessonDao();
  //  public abstract QuestionDao questionDao();

    private static volatile CreampenRoomDataBase INSTANCE;
    private static final int NUMBER_OF_THREADS = 2;
    //We've created an ExecutorService with a fixed thread pool that you will use to run database operations asynchronously on a background thread.
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static CreampenRoomDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CreampenRoomDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CreampenRoomDataBase.class, "creamPen_database")
                           // .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }



    private static androidx.room.RoomDatabase.Callback sRoomDatabaseCallback = new androidx.room.RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                SaveItemDao dao = INSTANCE.saveItemDao();
                //  dao.deleteAll();

                SavedItem item = new SavedItem();
                item.setItemType("Lesson");
                item.setItemTitle("Lesson Title one");
                item.setItemId("fireBase Lesson Id");
                dao.insert(item);

                item = new SavedItem();
                item.setItemId("firebase course id'");
                item.setItemTitle("Course Title one'");
                item.setItemType("Course");
                dao.insert(item);

            });
        }
    };
}
