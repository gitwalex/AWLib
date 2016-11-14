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
package de.aw.awlib;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.MenuItem;

/**
 * @author alex
 */
public interface AWLibInterface {
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
            , POSITION = "POSITION"
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
            , INTENTFILTER = "INTENTFILTER"
            /**
             *
             */
            , ACTIONBARTITLE = "ACTIONBARTITLE"
            /**
             *
             */
            , ACTIONBARSUBTITLE = "ACTIONBARSUBTITLE"
            /**
             * Ist Passwort noch guelitg?
             */
            , PASSWORDISVALID = "PASSWORDISVALID"
            /**
             * MainAction
             */
            , AWLIBACTION = "AWLIBACTION";
    int
            /**
             *
             */
            NOLAYOUT = -1
            /**
             *
             */
            , NOID = 0
            /**
             * Flag fuer Home-Button-Pressed.
             * @see AWLibMainActivity#onOptionsItemSelected(MenuItem)
             */
            , HOME_BUTTON_PRESSED = 1;
    Long
            /**
             *
             */
            NOROWS = -1L;
    int POPBACKSTACK = 1;
    String
            /**
             *
             */
            linefeed = System.getProperty("line.separator");

    enum MainAction implements Parcelable {
        Logout, Login, ADD, EDIT, SHOW;
        public static final Creator<MainAction> CREATOR =
                new Creator<MainAction>() {
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

