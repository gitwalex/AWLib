package de.aw.awlib.database_private;

import android.net.Uri;

import java.util.Set;

import de.aw.awlib.database.AbstractDBChangeHelper;

/**
 * Created by alex on 21.11.2016.
 */
public class AWLibDBChangeHelper extends AbstractDBChangeHelper {
    @Override
    protected void notifyCursors(Set<Uri> tables) {
        super.notifyCursors(tables);
    }
}
