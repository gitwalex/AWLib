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
import android.util.AttributeSet;
import android.widget.EditText;

import de.aw.awlib.activities.AWInterface;

/**
 * Created by alex on 02.03.2015.
 */
public class AWEditText extends EditText implements AWInterface {
    private String aktuellerText;
    private int mBroadcastIndex;
    private AWAutoCompleteTextView.OnTextChangedListener mOnTextChangedListener;

    public AWEditText(Context context) {
        super(context);
    }

    public AWEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getBroadcastIndex() {
        return mBroadcastIndex;
    }

    public void setBroadcastIndex(int index) {
        mBroadcastIndex = index;
    }

    public String getValue() {
        return getText().toString();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setSelectAllOnFocus(true);
    }

    /**
     * Verendet eine BroadcastMessage, wenn sich der Text geaendert hat.
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        String newText = text.toString();
        if (!newText.equals(aktuellerText)) {
            aktuellerText = newText;
            sendBroadcast(newText);
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    /**
     * Versendet den Text als Broadcast
     *
     * @param text
     *         Text, der versendet werden soll.
     */
    protected void sendBroadcast(String text) {
        if (mOnTextChangedListener != null) {
            mOnTextChangedListener.onTextChanged(this, text, NOID);
        }
    }

    public void setOnTextChangedListener(AWAutoCompleteTextView.OnTextChangedListener listener) {
        mOnTextChangedListener = listener;
    }
}
