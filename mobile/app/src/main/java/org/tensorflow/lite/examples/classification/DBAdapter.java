package org.tensorflow.lite.examples.classification;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class DBAdapter {
    private SQLiteDatabase db;

    public DBAdapter(Context context, String DB_NAME) {
        super();
        String DB_PATH = "/data/data/" + context.getPackageName()+ "/databases";
        File dbCopy = new File(DB_PATH + DB_NAME);

        if (dbCopy.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbCopy.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            this.db = db;
        }
    }

    public void close() {
        db.close();
    }

    public Cursor search(String sql) {
        Cursor cursor = db.rawQuery(sql, null);

        return cursor;
    }
}
