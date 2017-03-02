package com.doura.meetingplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by doura on 2/22/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME ="MeetingPlanner_DB";
    private static final String TABLE_NAME ="Profile_Table";
    private static final int DATABASE_VERSION = 1;
    private static final String COL_1 ="_ID";
    private static final String COL_2 ="_NAME";
    private static final String COL_3 ="_PHOTO";
    private static final String COL_4 ="_GROUP";
    private static final String COL_5 ="_ORGANIZER";
    private static final String COL_6 ="_LONGITUDE";
    private static final String COL_7 ="_LATITUDE";

    private Context mCxt;
    private static DatabaseHelper mInstance = null;

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_2 + " TEXT, "
            + COL_3 + " BLOB, "
            + COL_4 + " TEXT, "
            + COL_5 + " INTEGER, "
            + COL_6 + " TEXT, "
            + COL_7 + " TEXT);";


    //Constructeur pour creer la base de donnée
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);                         //constructeur parent
        this.mCxt = context;
        SQLiteDatabase db = this.getWritableDatabase();                                //creation de la table
    }

    //Appeler le constructeur si la bdd n'est pas crée
    public static DatabaseHelper getInstance(Context ctx) {

        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public boolean insertData(String nom, String groupe, byte[] image){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(COL_2,nom);
        contentvalues.put(COL_3,image);
        contentvalues.put(COL_4,groupe);

        long result = db.insert(TABLE_NAME,null,contentvalues);
        if (result == -1)
            return false;
        else
            return true;
    }

}
