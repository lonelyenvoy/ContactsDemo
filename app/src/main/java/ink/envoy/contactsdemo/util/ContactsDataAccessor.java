package ink.envoy.contactsdemo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

public class ContactsDataAccessor {

    private final Context applicationContext;

    public ContactsDataAccessor(Context context) {
        applicationContext = context;
    }

    private SQLiteDatabase getDatabase() {
        return new ContactsDatabaseHelper(applicationContext).getReadableDatabase();
    }

    public List<String[]> get() {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.query(
                "contacts",
                new String[] {"_id", "name", "phone", "email" },
                "",
                null,
                null,
                null,
                null);
        List<String[]> results = new LinkedList<>();
        while (cursor.moveToNext()) {
            results.add(new String[] {
                    cursor.getInt(0) + "", // id
                    cursor.getString(1), // name
                    cursor.getString(2), // phone
                    cursor.getString(3) // email
            });
        }
        cursor.close();
        db.close();
        return results;
    }

    public void put(String[] contactInfo) {
        SQLiteDatabase db = getDatabase();
        put(db, contactInfo);
        db.close();
    }

    public void put(List<String[]> contactInfos) {
        SQLiteDatabase db = getDatabase();
        for (String[] contactInfo : contactInfos) {
            put(db, contactInfo);
        }
        db.close();
    }

    protected void put(SQLiteDatabase db, String[] contactInfo) {
        ContentValues values = new ContentValues();
        values.put("name", contactInfo[0]);
        values.put("phone", contactInfo[1]);
        values.put("email", contactInfo[2]);
        db.insert("contacts", null, values);
    }

    public void delete(long id) {
        SQLiteDatabase db = getDatabase();
        db.delete("contacts", "_id=?", new String[] {id+""});
        db.close();
    }

    public void clear() {
        execSql("DELETE FROM contacts");
    }

    @Unsafe
    public void execSql(@NonNull String sql) throws SQLException {
        SQLiteDatabase db = getDatabase();
        db.execSQL(sql);
        db.close();
    }

    /**
     * 执行非破坏性SQL语句
     * @param sql SQL语句
     * @return 查询到的结果
     * @throws SQLException
     */
    public List<String[]> execQueryingSql(@NonNull String sql) throws SQLException {
        List<String[]> list = new LinkedList<>();
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.rawQuery(sql, new String[] {});
        while (cursor.moveToNext()) {
            list.add(new String[] {
                    cursor.getInt(0) + "",
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            });
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * Mark a method as unsafe.
     */
    @interface Unsafe {}
}
