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
package de.aw.awlib.events;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Definiert moegliche Events innerhalb MonMa.
 */
public enum AWLibEvent implements Parcelable {
    /**
     * Event fuer Sicherung der Datenbank
     */
    DoDatabaseSave;
    public static final Parcelable.Creator<AWLibEvent> CREATOR =
            new android.os.Parcelable.Creator<AWLibEvent>() {
                public AWLibEvent createFromParcel(Parcel in) {
                    return AWLibEvent.values()[in.readInt()];
                }

                public AWLibEvent[] newArray(int size) {
                    return new AWLibEvent[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }
}
