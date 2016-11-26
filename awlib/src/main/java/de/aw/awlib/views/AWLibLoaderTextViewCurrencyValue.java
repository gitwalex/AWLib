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
package de.aw.awlib.views;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;

import de.aw.awlib.database.AWLibDBConvert;

/**
 * Convenience-Klasse fuer TextView, die mit einem Loader hinterlegt ist. ID der TextView wird
 * entweder durch xml vorgegeben. Alternativ wird die resID benutzt, die in initialize() uebergeben
 * wird. Der Wert wird als Waehrung angezeigt.
 */
public class AWLibLoaderTextViewCurrencyValue extends AWLibAbstractLoaderTextView {
    private long mValue;

    public AWLibLoaderTextViewCurrencyValue(Context context) {
        super(context);
    }

    public AWLibLoaderTextViewCurrencyValue(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWLibLoaderTextViewCurrencyValue(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String convertValue(Cursor data) {
        mValue = data.getLong(0);
        return AWLibDBConvert.convertCurrency(mValue);
    }

    public long getValue() {
        return mValue;
    }
}
