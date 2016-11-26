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

/**
 * Exception innerhalb MonMa. Entweder einfache Exception, alternativ kann innerhab der Exception
 * auch noch ein Object geseichert werden
 */
public class AWException extends Exception {
    private Object object;

    /**
     * Exception mit Message
     *
     * @param message
     *         Message
     */
    public AWException(String message) {
        super(message);
    }

    /**
     * Exception mit Message
     *
     * @param message
     *         Message
     * @param o
     *         Object, welches durch {@link AWException#getExceptionObject()} geholt werden kann.
     */
    public AWException(String message, Object o) {
        super(message);
        this.object = o;
    }

    /**
     * Object, welches bei Erstellung der Exception mmitgeliefert wurde.
     *
     * @return Object der Exception
     */
    public Object getExceptionObject() {
        return object;
    }

    /**
     * @return true, wenn innerhalb der Exception ein Object gespeichert wurde.
     */
    public boolean hasObject() {
        return object != null;
    }
}
