package de.aw.awlib.database_private;

import android.database.sqlite.SQLiteDatabase;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBAlterHelper;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Created by alex on 21.11.2016.
 */
public class AWDBHelper extends AbstractDBHelper {
    private static AWDBHelper db;

    private AWDBHelper() {
        super(AWApplication.getMainApplicationConfig(), null);
    }

    public static AWDBHelper getInstance() {
        if (db == null) {
            db = new AWDBHelper();
        }
        return db;
    }

    @Override
    public AWAbstractDBDefinition[] getAllDBDefinition() {
        return AWDBDefinition.values();
    }

    @Override
    public AWAbstractDBDefinition getDBDefinition(String tablename) {
        return AWDBDefinition.valueOf(tablename);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        AWDBAlterHelper dbhelper = new AWDBAlterHelper(database);
        for (AWDBDefinition tbd : AWDBDefinition.values()) {
            if (!tbd.isView()) {
                dbhelper.createTable(tbd);
            }
        }
        for (AWDBDefinition tbd : AWDBDefinition.values()) {
            if (tbd.isView()) {
                dbhelper.alterView(tbd);
            }
            tbd.createDatabase(dbhelper);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                AWDBAlterHelper helper = new AWDBAlterHelper(db);
                AWDBDefinition tbd = AWDBDefinition.RemoteServer;
        }
    }
}
