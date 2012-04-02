package org.cry.otp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_PROF_NAME = "prof_name";
    public static final String KEY_SEED = "seed";
    public static final String KEY_OTP_TYPE = "otp_type";
    public static final String KEY_COUNT = "count";
    public static final String KEY_TIME_ZONE = "time_zone";
    public static final String KEY_DIGITS = "digits";
    public static final String KEY_TIME_INTERVAL = "time_interval";
    private static final String TAG = "DBAdapter";
    private static final String DATABASE_NAME = "profs";
    private static final String DATABASE_TABLE = "profiles";
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_CREATE = "create table "
            + DATABASE_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, " + KEY_PROF_NAME
            + " text not null, " + KEY_SEED + " text not null, " + KEY_OTP_TYPE
            + " integer not null, " + KEY_COUNT + " integer not null, "
            + KEY_DIGITS + " integer not null, " + KEY_TIME_ZONE
            + " text not null, " + KEY_TIME_INTERVAL + " integer not null);";
    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.v("DBAdapter", "DATABASE CREATE = " + DATABASE_CREATE);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_OTP_TYPE + "  integer not null DEFAULT 0");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_COUNT + " integer not null DEFAULT 0");
            }
            if (oldVersion == 1 && newVersion == 3) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_OTP_TYPE + "  integer not null DEFAULT 0");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_COUNT + " integer not null DEFAULT 0");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_DIGITS + " integer not null DEFAULT 6");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_ZONE + " text not null DEFAULT GMT");
            }
            if (oldVersion == 1 && newVersion == 4) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_OTP_TYPE + "  integer not null DEFAULT 0");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_COUNT + " integer not null DEFAULT 0");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_DIGITS + " integer not null DEFAULT 6");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_ZONE + " text not null DEFAULT GMT");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_INTERVAL + " integer not null DEFAULT 30");
            }
            if (oldVersion == 2 && newVersion == 3) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_DIGITS + " integer not null DEFAULT 6");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_ZONE + " text not null DEFAULT GMT");
            }
            if (oldVersion == 2 && newVersion == 4) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_DIGITS + " integer not null DEFAULT 6");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_ZONE + " text not null DEFAULT GMT");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_INTERVAL + " integer not null DEFAULT 30");
            }
            if (oldVersion == 3 && newVersion == 4) {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN "
                        + KEY_TIME_INTERVAL + " integer not null DEFAULT 30");
            }
        }
    }

    public void reCreate() {
        db.execSQL("DROP TABLE IF EXISTS profiles");
        db.execSQL(DATABASE_CREATE);
    }

    // ---opens the database---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    // ---closes the database---
    public void close() {
        DBHelper.close();
    }

    // ---insert a title into the database---
    public long insertProfile(String prof_name, String seed, int otpType,
            int digits, String time_zone, int timeInterval) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PROF_NAME, prof_name);
        initialValues.put(KEY_SEED, seed);
        initialValues.put(KEY_OTP_TYPE, otpType);
        initialValues.put(KEY_COUNT, 0);
        initialValues.put(KEY_DIGITS, digits);
        initialValues.put(KEY_TIME_ZONE, time_zone);
        initialValues.put(KEY_TIME_INTERVAL, timeInterval);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // ---deletes a particular title---
    public boolean deleteProfile(int rowId) {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public void deleteAllProfiles() {
        db.execSQL("DROP TABLE IF EXISTS profiles");
        db.execSQL(DATABASE_CREATE);
    }

    public Cursor getAllProfiles() {
        return db.query(DATABASE_TABLE, new String[] { KEY_ROWID,
                KEY_PROF_NAME, KEY_SEED, KEY_OTP_TYPE, KEY_COUNT, KEY_DIGITS,
                KEY_TIME_ZONE, KEY_TIME_INTERVAL }, null, null, null, null,
                null);
    }

    // ---retrieves a particular title---
    public Cursor getProfile(int rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_PROF_NAME, KEY_SEED, KEY_OTP_TYPE, KEY_COUNT,
                KEY_DIGITS, KEY_TIME_ZONE, KEY_TIME_INTERVAL }, KEY_ROWID + "="
                + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // ---updates a title---
    public boolean updateProfile(int rowId, String prof_name, String seed,
            int otp_type, int count, int digits, String time_zone,
            int time_interval) {
        ContentValues args = new ContentValues();
        args.put(KEY_PROF_NAME, prof_name);
        args.put(KEY_SEED, seed);
        args.put(KEY_OTP_TYPE, otp_type);
        args.put(KEY_COUNT, count);
        args.put(KEY_DIGITS, digits);
        args.put(KEY_TIME_ZONE, time_zone);
        args.put(KEY_TIME_INTERVAL, time_interval);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateCount(int rowId, int count) {
        ContentValues args = new ContentValues();
        args.put(KEY_COUNT, count);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}