/*
 * MonMa: Eine freie Android-Application fuer die Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

package de.aw.awlib.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import de.aw.awlib.BR;
import de.aw.awlib.R;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.gv.CalendarItem;
import de.aw.awlib.recyclerview.AWCursorRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Zeigt alle externen Calendars des Devices
 */
public class AWFragmentCalendar extends AWCursorRecyclerViewFragment {
    private static final int layout = R.layout.awlib_default_recycler_view;
    private static final int viewHolderLayout = R.layout.awlib_calendarview;
    private static final AbstractDBHelper.AWDBDefinition tbd =
            AbstractDBHelper.AWDBDefinition.AndroidCalendar;
    private static final String[] projection =
            new String[]{Calendars._ID, Calendars.CALENDAR_DISPLAY_NAME};
    private static final String selection = Calendars.CALENDAR_DISPLAY_NAME + " like '%@%'";

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, Cursor cursor, int position) {
        CalendarItem item = new CalendarItem(cursor);
        holder.setVariable(BR.calendar, item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_CALENDAR:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.READ_CALENDAR)) {
                        startOrRestartLoader(layout, args);
                    }
                    i++;
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.selectCalendar);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
        args.putParcelable(DBDEFINITION, tbd);
        args.putStringArray(PROJECTION, projection);
        args.putString(SELECTION, selection);
    }

    @Override
    protected void startOrRestartLoader(int loaderID, Bundle args) {
        int permissionCheck =
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            super.startOrRestartLoader(loaderID, args);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},
                    REQUEST_PERMISSION_READ_CALENDAR);
        }
    }
}
