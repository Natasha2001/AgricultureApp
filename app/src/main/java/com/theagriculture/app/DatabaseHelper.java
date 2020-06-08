package com.theagriculture.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "VillagesList.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COL_1 = "Village_Id";
    private static final String COL_2 = "Village_Name";
    private static final String TABLE_NAME = "VILLAGES_TABLE";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE VILLAGES_TABLE (Village_Id INTEGER PRIMARY KEY, Village_Name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean insertData(int id, String villageName) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_1, id);
        values.put(COL_2, villageName);
        long result = database.insert("VILLAGES_TABLE", null, values);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor res = database.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }
}
