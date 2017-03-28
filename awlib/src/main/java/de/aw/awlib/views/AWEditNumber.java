package de.aw.awlib.views;

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

import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;

import de.aw.awlib.database.AWDBConvert;

/**
 * EditText fuer Werte, die in der DB als Number abgelegt werden (also mit {@link
 * AWDBConvert#mNumberDigits} Stellen.
 */
public class AWEditNumber extends AWEditText {
    private OnLongValueChangedListener mOnLongValueChangedListener;
    private long mValue;

    public AWEditNumber(Context context) {
        super(context);
    }

    public AWEditNumber(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWEditNumber(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @return Liefert den aktuellen Wert zurueck. Der Wert beinhalten die gemaess {@link
     * AWDBConvert#mNumberDigits} vorgesehenen Stellen.
     */
    public long getLongValue() {
        return mValue;
    }

    /**
     * Startwerte festlegen.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setSelectAllOnFocus(true);
        setEms(10);
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        try {
            String val = text.toString().replaceAll("\\.", "");
            mValue = (long) (Double.parseDouble(val) * AWDBConvert.mNumberDigits);
            if (mOnLongValueChangedListener != null) {
                mOnLongValueChangedListener.onLongValueChanged(this, mValue);
            }
        } catch (NumberFormatException e) {
            // ignorieren
        }
    }

    public void setOnLongValueChangedListener(OnLongValueChangedListener listener) {
        mOnLongValueChangedListener = listener;
    }

    public void setValue(long value) {
        if (value != mValue) {
            mValue = value;
            setText(((Double) (value / AWDBConvert.mNumberDigits)).toString());
        }
    }

    public interface OnLongValueChangedListener {
        void onLongValueChanged(AWEditText view, long value);
    }
}
