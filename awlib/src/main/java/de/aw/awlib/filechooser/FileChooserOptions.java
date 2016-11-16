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
package de.aw.awlib.filechooser;

import android.support.annotation.NonNull;

import java.util.Locale;

public class FileChooserOptions implements Comparable<FileChooserOptions> {
    public final Boolean parent;
    private final String name;
    private final String data;
    private final String path;

    public FileChooserOptions(String n, String d, String p, Boolean parent) {
        name = n;
        data = d;
        path = p;
        this.parent = parent;
    }

    @Override
    public int compareTo(@NonNull FileChooserOptions o) {
        if (this.name != null) {
            return this.name.toLowerCase(Locale.getDefault())
                    .compareTo(o.getName().toLowerCase(Locale.getDefault()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
