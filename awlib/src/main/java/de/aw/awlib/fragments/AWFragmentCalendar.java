package de.aw.awlib.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
            tv.setText(cursor.getString(1));
            return true;
        } else {
            return super.onBindView(holder, view, resID, cursor, cursorPosition);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int p1, Bundle args) {
        Uri mUri = Uri.parse("content://com.android.calendar/calendars");
        String[] projection = new String[]{"_id", "calendar_displayName"};
        return new CursorLoader(getActivity(), mUri, projection, null, null, null);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
    }
}
