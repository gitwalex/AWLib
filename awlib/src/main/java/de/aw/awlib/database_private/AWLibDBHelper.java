package de.aw.awlib.database_private;

import android.database.sqlite.SQLiteDatabase;

import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.database.AWLibDBAlterHelper;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Created by alex on 21.11.2016.
 */
public class AWLibDBHelper extends AbstractDBHelper {
    private static AWLibDBHelper db;

    private AWLibDBHelper() {
        super(AWLIbApplication.getMainApplicationConfig(), null);
    }

    public static AWLibDBHelper getInstance() {
        if (db == null) {
            db = new AWLibDBHelper();
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        AWLibDBAlterHelper dbhelper =
                new AWLibDBAlterHelper(AWLIbApplication.getContext(), database);
        for (AWLibDBDefinition tbd : AWLibDBDefinition.values()) {
            if (!tbd.isView()) {
                dbhelper.createTable(tbd);
            }
        }
        for (AWLibDBDefinition tbd : AWLibDBDefinition.values()) {
            if (tbd.isView()) {
                dbhelper.alterView(tbd);
            }
            tbd.createDatabase(dbhelper);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
