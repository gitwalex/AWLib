package de.aw.awlib.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.recyclerview.AWCursorRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Created by alex on 04.12.2016.
 */
public class AWFragmentCalendar extends AWCursorRecyclerViewFragment {
    private static final int layout = R.layout.awlib_default_recycler_view;
    private static final int viewHolderLayout = R.layout.awlib_calendarview;
    private static final int[] viewResIDs = new int[]{R.id.tvCalendarName};

    @Override
    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, Cursor cursor,
                                 int cursorPosition) {
        if (resID == R.id.tvCalendarName) {
            TextView tv = (TextView) view;
            tv.setText(cursor.getString(1) + ": " + cursor.getString(2));
            return true;
        } else {
            return super.onBindView(holder, view, resID, cursor, cursorPosition);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int p1, Bundle args) {
        Uri mUri = Uri.parse("content://com.android.calendar/calendars");
        String[] projection =
                new String[]{Calendars._ID, Calendars.CALENDAR_DISPLAY_NAME, Calendars.VISIBLE};
        String selection = Calendars.CALENDAR_DISPLAY_NAME + " like '%@%'";
        return new CursorLoader(getActivity(), mUri, projection, selection, null, null);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
    }

    @Override
    protected void startOrRestartLoader(int loaderID, Bundle args) {
        int permissionCheck =
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LoaderManager lm = getLoaderManager();
            Loader<Cursor> loader = lm.getLoader(loaderID);
            if (loader != null && !loader.isReset()) {
                lm.restartLoader(loaderID, args, this);
            } else {
                lm.initLoader(loaderID, args, this);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CALENDAR},
                    REQUEST_PERMISSION_READ_CALENDAR);
        }
    }
}
