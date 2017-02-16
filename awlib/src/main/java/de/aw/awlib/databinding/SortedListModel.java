package de.aw.awlib.databinding;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
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

/**
 * Interface fuer eine {@link android.support.v7.util.SortedList} des {@link
 * de.aw.awlib.adapters.AWSortedListAdapter}
 */
public interface SortedListModel<T> {
    /**
     * Wird aus dem Adapter gerufen, wenn {@link SortedListModel#areItemsTheSame(Object)} true
     * zuruckgegeben hat. Dann kann hier angegeben werden, ob nicht nur die Suchkritieren identisch
     * sind, sindern auch der Inhalt.
     *
     * @param other
     *         das zu vergleichende Item
     * @return true, wenn die Inhalte gleich sind.
     */
    boolean areContentsTheSame(T other);

    /**
     * Wird aus dem Adapter gerufen, wenn {@link SortedListModel#compare(Object)}  '0' zuruckgegeben
     * hat. Dann kann hier angegeben werden, ob die Suchkritieren identisch sind.
     *
     * @param other
     *         das zu vergleichende Item
     * @return true, wenn die Suchkriterien gleich sind.
     */
    boolean areItemsTheSame(T other);

    /**
     * Wird aus dem Adapter gerufen, um die Reihenfolge festzulegen.
     *
     * @param other
     *         das zu vergleichende Item
     * @return -1, wenn dieses Item vor other liegen soll
     * <p>
     * 1, wenn dieses Item hinter other liegen soll
     * <p>
     * sonst 0. Dann wird {@link SortedListModel#areItemsTheSame(Object)} gerufen
     */
    int compare(T other);

    /**
     * @return Liefert die ID zuruck.
     */
    long getID();
}
