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
import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.graphics.Rect;
import android.support.annotation.CallSuper;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.database.AWAbstractDBDefinition;

/**
 * AutoCompleteTextView (siehe  {@link AWAutoCompleteTextView#initialize (DBDefinition, String,
 * String[], boolean, int[])}.<br> Sendet eine Message nach einer TextAenderung. Threshold ist
 * standardmaessig 3.
 *
 * @see AWAutoCompleteTextView#onTextChanged(String newText)
 */
public abstract class AWAutoCompleteTextView
        extends android.support.v7.widget.AppCompatAutoCompleteTextView
        implements AWInterface, SimpleCursorAdapter.CursorToStringConverter, FilterQueryProvider,
        AdapterView.OnItemClickListener, AutoCompleteTextView.Validator {
    protected OnTextChangedListener mOnTextChangeListener;
    private int columnIndex;
    private int fromResID;
    private int mIndex;
    private CharSequence mConstraint;
    private String mMainColumn;
    private String mOrderBy;
    private String[] mProjection;
    private String mSelection;
    private String validatedText = null;
    private long selectionID;
    private AWAbstractDBDefinition tbd;

    @BindingAdapter({"onTextChanged"})
    public static void onTextChanged(AWAutoCompleteTextView view, OnTextChangedListener listener) {
        view.setOnTextChangedListener(listener);
    }

    public AWAutoCompleteTextView(Context context) {
        super(context);
    }

    public AWAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void buildSelectionArguments(String mUserSelection, String[] mUserSelectionArgs) {
        mSelection = tbd.columnName(fromResID) + " Like ? ";
        mOrderBy = "LENGTH(" + mMainColumn + ")";
        if (mUserSelection != null) {
            if (mUserSelectionArgs != null) {
                for (String sel : mUserSelectionArgs) {
                    mUserSelection = mUserSelection.replaceFirst("\\?", "'" + sel + "'");
                }
            }
            mSelection = mSelection + " AND (" + mUserSelection + ")";
        }
        mSelection = mSelection + "  GROUP BY " + mMainColumn;
        mOrderBy = mOrderBy + ", " + mMainColumn;
    }

    @Override
    public final CharSequence convertToString(Cursor cursor) {
        return cursor.getString(columnIndex);
    }

    @Override
    public final CharSequence fixText(CharSequence invalidText) {
        return validatedText;
    }

    public final int getIndex() {
        return mIndex;
    }

    /**
     * Dann kann diese View mehrmals in einem Layout verwendet werden.
     *
     * @param index
     *         index
     */
    public final void setIndex(int index) {
        mIndex = index;
    }

    /**
     * @return Liefert die ID des selektierten Textes. Ist ein Validator gesetzt, den ersten aus dem
     * Cursor,ansonsten NOID.
     */
    public long getSelectionID() {
        if (getValidator() != null) {
            return selectionID;
        } else {
            if (getText().toString().equals(validatedText)) {
                return selectionID;
            }
        }
        return NOID;
    }

    /**
     * Initialisiert AutoCompleteTextView.
     *
     * @param tbd
     *         DBDefinition. Aus dieser Tabelle wird das Feld gelesen
     * @param selection
     *         selection
     * @param selectionArgs
     *         Argumente zur Selection
     * @param fromResID
     *         Feld, welches fuer die Selection benutzt werden soll.
     * @throws NullPointerException,
     *         wenn LoaderManager null ist.
     */
    public final void initialize(OnTextChangedListener mOnTextChangeListener,
                                 AWAbstractDBDefinition tbd, String selection,
                                 String[] selectionArgs, int fromResID) {
        this.mOnTextChangeListener = mOnTextChangeListener;
        initialize(tbd, selection, selectionArgs, fromResID);
    }

    /**
     * Initialisiert AutoCompleteTextView.
     *
     * @param tbd
     *         DBDefinition. Aus dieser Tabelle wird das Feld gelesen
     * @param selection
     *         selection
     * @param selectionArgs
     *         Argumente zur Selection
     * @param fromResID
     *         Feld, welches fuer die Selection benutzt werden soll.
     * @throws NullPointerException,
     *         wenn LoaderManager null ist.
     */
    public final void initialize(AWAbstractDBDefinition tbd, String selection,
                                 String[] selectionArgs, int fromResID) {
        this.tbd = tbd;
        this.fromResID = fromResID;
        mMainColumn = tbd.columnName(this.fromResID);
        mProjection = new String[]{tbd.columnName(fromResID), tbd.columnName(R.string._id)};
        buildSelectionArguments(selection, selectionArgs);
        SimpleCursorAdapter mSimpleCursorAdapter =
                new SimpleCursorAdapter(getContext(), android.R.layout.simple_dropdown_item_1line,
                        null, mProjection, new int[]{android.R.id.text1}, 0);
        mSimpleCursorAdapter.setCursorToStringConverter(this);
        mSimpleCursorAdapter.setFilterQueryProvider(this);
        setAdapter(mSimpleCursorAdapter);
    }

    /**
     * Ein Text kann nur gueltig sein, wenn die Textlaenge kleiner des selektierten Textes ist.
     */
    @Override
    public boolean isValid(CharSequence text) {
        return (text.toString().equals(validatedText));
    }

    @Override
    protected void onDetachedFromWindow() {
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getAdapter();
        if (adapter != null) {
            Cursor c = adapter.swapCursor(null);
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setThreshold(3);
        setOnItemClickListener(this);
        setSelectAllOnFocus(true);
    }

    /**
     * Wenn die View den Fokus verliert, wird geprueft, ob neue Eintraeg zugelassen sind. Ist dies
     * nicht der Fall, wird der Text auf den zuletzt  gueltigen Text zurueckgesetzt und dieser Text
     * versendet.
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (!focused) {
            if (getValidator() != null) {
                setText(validatedText);
                onTextChanged(validatedText);
            }
        } else {
            showDropDown();
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    /**
     * Wenn ein List-Item ausgewaehlt wird, wird eine Message mit dem ausgewaehlten Text gesendet.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectionID = id;
        validatedText = ((TextView) view).getText().toString().trim();
        onTextChanged(validatedText);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (getValidator() != null) {
            onTextChanged(validatedText);
        } else {
            onTextChanged(text.toString());
        }
    }

    /**
     * Wird bei Textaenderungen gerufen.
     *
     * @param newText
     *         Text
     */
    @CallSuper
    protected void onTextChanged(String newText) {
        if (mOnTextChangeListener != null) {
            mOnTextChangeListener.onTextChanged(this, newText, getSelectionID(), mIndex);
        }
    }

    /**
     * Restartet die TextView mit neuen Argumenten
     *
     * @param selection
     *         neue selection
     * @param selectionArgs
     *         neue SelectionArgs
     */
    protected void restart(String selection, String[] selectionArgs) {
        if (selection != null) {
            buildSelectionArguments(selection, selectionArgs);
        }
        runQuery(mConstraint);
    }

    /**
     * Nach tippen wird hier nachgelesen. Es wird mit 'LIKE %constraint%' ausgewaehlt.
     * <p>
     * Hat der Cursor Daten und validierung ist eingeschaltet (es ist kein neuer Wert zugelassen),
     * wird die erste ID aus dem Cursor geholt und der  Text auf den entsprechenden Wert des Cursors
     * gesetzt. Ausserdem wird dann gleich eine Message versendet, wenn es nur genau einen Wert
     * gibt.
     *
     * @param constraint
     *         Text
     * @return den neuen Cursor
     */
    @Override
    public Cursor runQuery(CharSequence constraint) {
        selectionID = NOID;
        if (constraint == null) {
            constraint = "";
        }
        mConstraint = constraint.toString().trim();
        String[] mSelectionArgs = new String[]{"%" + mConstraint + "%"};
        Cursor data = getContext().getContentResolver()
                                  .query(tbd.getUri(), mProjection, mSelection, mSelectionArgs,
                                          mOrderBy);
        assert data != null;
        if (data.moveToFirst()) {
            selectionID = data.getLong(1);
            validatedText = data.getString(0).trim();
            if (data.getCount() == 1 && getValidator() != null) {
                setText(validatedText);
            }
        }
        columnIndex = data.getColumnIndexOrThrow(tbd.columnName(fromResID));
        return data;
    }

    public void setOnTextChangedListener(OnTextChangedListener onTextChangedListener) {
        mOnTextChangeListener = onTextChangedListener;
    }

    public void setValidatedText(String text) {
        validatedText = text;
    }

    /**
     * Setzt sich selbst als Validator.
     *
     * @see AWAutoCompleteTextView#setValidator(Validator)
     */
    public void setValidating() {
        setValidator(this);
    }

    /**
     * Interface fuer Listener auf Textaenderungen
     */
    public interface OnTextChangedListener {
        /**
         * Wird gerufen, wenn sich der Text einer View geaendert hat
         *
         * @param view
         *         view, deren Text sich geaendert hat
         * @param newText
         *         Neuer Text.
         * @param newID
         *         ID aus der DB, wenn Nutzer ein Item aus dem Pulldown selektiert hat oder wenn
         *         der
         * @param index
         *         index, wie in {@link AWAutoCompleteTextView#setIndex(int)} gesetzt
         */
        void onTextChanged(View view, String newText, long newID, int index);
    }
}
