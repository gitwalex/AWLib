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

package de.aw.awlib;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility-Klasse
 */
public final class AWLibUtils {
    /**
     * Liest eine Map aus Parcel.
     * <p/>
     * Usage: MyClass1 and MyClass2 must extend Parcelable Map<MyClass1, MyClass2> map;
     * <p/>
     * readParcelableMap(parcel, MyClass1.class, MyClass2.class);
     *
     * @param parcel
     *         parcel aus Kontruktor Object(Parcel parcel)
     * @param kClass
     *         Key-Klasse
     * @param vClass
     *         Value-Klasse
     * @param <K>
     *         Key. Parceable
     * @param <V>
     *         Value. Parceable
     *
     * @return Map
     */
    public static <K extends Parcelable, V extends Parcelable> Map<K, V> readParcelableMap(
            Parcel parcel, Class<K> kClass, Class<V> vClass) {
        int size = parcel.readInt();
        Map<K, V> map = new HashMap<K, V>(size);
        for (int i = 0; i < size; i++) {
            map.put(kClass.cast(parcel.readParcelable(kClass.getClassLoader())),
                    vClass.cast(parcel.readParcelable(vClass.getClassLoader())));
        }
        return map;
    }

    /**
     * Schreibt Map nach Parcel
     * <p/>
     * Usage: MyClass1 and MyClass2 must extend Parcelable Map<MyClass1, MyClass2> map;
     * <p/>
     * // Writing to a parcel
     * <p/>
     * writeParcelableMap(parcel, flags, map); *
     *
     * @param parcel
     *         parcel aus {@link Parcelable#writeToParcel(Parcel, int)}
     * @param flags
     *         flags aus {@link Parcelable#writeToParcel(Parcel, int)}
     * @param map
     *         Map, die geschrieben werden soll
     * @param <K>
     *         Key der Map. Parceable
     * @param <V>
     *         Value der Map. Parceable
     */
    public static <K extends Parcelable, V extends Parcelable> void writeParcelableMap(
            Parcel parcel, int flags, Map<K, V> map) {
        parcel.writeInt(map.size());
        for (Map.Entry<K, V> e : map.entrySet()) {
            parcel.writeParcelable(e.getKey(), flags);
            parcel.writeParcelable(e.getValue(), flags);
        }
    }
}
