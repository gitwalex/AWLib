/*
 * MonMa: Eine freie Android-App fuer Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 23730 Neustadt/Germany]
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
/**
 *
 */
package de.aw.awlib.activities;

import android.os.Parcel;
import android.os.Parcelable;

import de.aw.awlib.events.AWEvent;
import de.aw.awlib.fragments.AWFileChooser;

/**
 * @author alex
 */
public interface AWInterface {
    String
            /**
             *
             */
            ID = "ID"
            /**
             *
             */
            , LAYOUT = "LAYOUT"
            /**
             *
             */
            , VIEWHOLDERLAYOUT = "VIEWHOLDERLAYOUT"
            /**
             *
             */
            , VIEWRESIDS = "VIEWRESIDS"
            /**
             *
             */
            , FROMRESIDS = "FROMRESIDS"
            /**
             *
             */
            , LOGFILE = "LOGFILE"
            /**
             *
             */
            , NEXTACTIVITY = "NEXTACTIVITY"
            /**
             *
             */
            , DBDEFINITION = "DBDEFINITION"
            /**
             *
             */
            , PROJECTION = "PROJECTION"
            /**
             *
             */
            , SELECTION = "SELECTION"
            /**
             *
             */
            , SELECTIONARGS = "SELECTIONARGS"
            /**
             *
             */
            , GROUPBY = "GROUPBY"
            /**
             *
             */
            , ORDERBY = "ORDERBY"
            /**
             *
             */
            , VIEWID = "VIEWID"
            /**
             *
             */
            , SELECTEDVIEWHOLDERITEM = "SELECTEDVIEWHOLDERITEM"
            /**
             *
             */
            , LASTSELECTEDPOSITION = "LASTSELECTEDPOSITION"
            /**
             *
             */
            , ACTIONBARTITLE = "ACTIONBARTITLE"
            /**
             *
             */
            , ACTIONBARSUBTITLE = "ACTIONBARSUBTITLE"
            /**
             * Events fuer {@link AWEvent}
             */
            , AWLIBEVENT = "AWLIBEVENT"
            /**
             * MainAction
             */
            , AWLIBACTION = "AWLIBACTION"
            /**
             * Filter fuer einen Filenamen. Wird in {@link AWFileChooser} verwendet.
             */
            , FILENAMEFILTER = "FILENAMEFILTER"
            /**
             * Daten fuer einen Server
             */
            , REMOTEFILESERVER = "REMOTEFILESERVER";
    int
            /**
             *
             */
            REQUEST_PERMISSION_READ_CALENDAR = 100
            /**
             *
             */
            , REQUEST_PERMISSION_CALENDAR = 110
            /**
             *
             */
            , REQUEST_PERMISSION_STORAGE = 110
            /**
             *
             */
            , NOLAYOUT = -1
            /**
             *
             */
            , NOID = 0;
    Long
            /**
             *
             */
            NOROWS = -1L;
    String
            /**
             *
             */
            linefeed = System.getProperty("line.separator");

    enum MainAction implements Parcelable {
        ADD, EDIT, SHOW;
        public static final Creator<MainAction> CREATOR = new Creator<MainAction>() {
            public MainAction createFromParcel(Parcel in) {
                return MainAction.values()[in.readInt()];
            }

            public MainAction[] newArray(int size) {
                return new MainAction[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(ordinal());
        }
    }
}

