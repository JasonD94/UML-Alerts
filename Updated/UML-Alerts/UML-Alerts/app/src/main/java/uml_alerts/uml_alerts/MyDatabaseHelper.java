package uml_alerts.uml_alerts;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jason on 4/9/15.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AlertsDatabase";

    public MyDatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, 1);
    }

    @Override

    public void onCreate(SQLiteDatabase database) {

        database.execSQL("CREATE TABLE alerts (_id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT, phone_number INTEGER);");
    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS alerts");

        onCreate(db);
    }

    public void addAlert(String msg,int phone_number) {

        ContentValues values = new ContentValues(2);
        values.put("message", msg);
        values.put("phone_number", phone_number);
        getWritableDatabase().insert("alerts", "msg", values);
    }
}