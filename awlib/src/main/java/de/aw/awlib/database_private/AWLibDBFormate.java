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
package de.aw.awlib.database_private;

import de.aw.awlib.database.AbstractDBFormate;

/**
 * Formate fuer die einzelnen Columns der DB
 */
public class AWLibDBFormate extends AbstractDBFormate {
    private static AWLibDBFormate mInstance;

    private AWLibDBFormate() {
    }

    public static AWLibDBFormate getInstance() {
        if (mInstance == null) {
            mInstance = new AWLibDBFormate();
        }
        return mInstance;
    }

    /**
     * @return Liste der columns. [0] = resID, [1] = format
     */
    public int[][] getItems() {
        return new int[][]{};
    }
}
