package ink.envoy.contactsdemo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactsDatabaseHelper extends SQLiteOpenHelper {

    public ContactsDatabaseHelper(Context context) {
        super(context, "contacts.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE contacts(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, email TEXT, createdAt INTEGER, updatedAt INTEGER)"
        );
        long time = System.currentTimeMillis();
        sqLiteDatabase.execSQL(
                "INSERT INTO contacts(name, phone, email, createdAt, updatedAt) values ('小刘', '13609046213', 'admin@envoy.ink', ?, ?)",
                new Object[] { time, time });
        sqLiteDatabase.execSQL(
                "INSERT INTO contacts(name, phone, email, createdAt, updatedAt) values ('张华', '15361527951', 'zhang@qq.com', ?, ?)",
                new Object[] { time, time });
        sqLiteDatabase.execSQL(
                "INSERT INTO contacts(name, phone, email, createdAt, updatedAt) values ('小林', '18026342612', 'lin@126.com', ?, ?)",
                new Object[] { time, time });
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
