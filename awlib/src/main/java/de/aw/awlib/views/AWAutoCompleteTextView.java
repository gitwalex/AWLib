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
import android.graphics.Rect;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * AutoCompleteTextView (siehe  {@link AWAutoCompleteTextView#initialize (DBDefinition, String,
 * String[], boolean, int[])}.<br> Sendet eine Message nach einer TextAenderung. Threshold ist
 * standardmaessig 3.
 *
 * @see AWAutoCompleteTextView#sendMessage()
 */
public abstract class AWAutoCompleteTextView
        extends android.support.v7.widget.AppCompatAutoCompleteTextView
        implements AWInterface, SimpleCursorAdapter.CursorToStringConverter, FilterQueryProvider,
        AdapterView.OnItemClickListener, AutoCompleteTextView.Validator {
    private static int[] viewResIDs = new int[]{android.R.id.text1};
    protected OnTextChangedListener mOnTextChangeListener;
    private int columnIndex;
    private int fromResID;
    private boolean isValidatorSet;
    private int mBroadcastIndex;
    private CharSequence mConstraint;
    private String mMainColumn;
    private String mOrderBy;
    private String[] mProjection;
    private String mSelection;
    private String selectedText = null;
    private long selectionID;
    private AWAbstractDBDefinition tbd;

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
        AWApplication mAppContext = (AWApplication) getContext().getApplicationContext();
        mSelection = mAppContext.getDBHelper().columnName(fromResID) + " Like ? ";
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
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(columnIndex);
    }

    @Override
    public CharSequence fixText(CharSequence invalidText) {
        return selectedText;
    }

    public int getBroadcastIndex() {
        return mBroadcastIndex;
    }

    /**
     * Liefert den gueltigen Text zurueck. Entweder den im Textfeld, ist ein Validator gesetzt, der
     * entsprechende Text aus dem Cursor.
     *
     * @return Text
     */
    public String getSelectedText() {
        if (isValidatorSet) {
            return selectedText;
        } else {
            return getText().toString();
        }
    }

    /**
     * @return Liefert die ID des selektierten Textes. Ist ein Validator gesetzt, den ersten aus dem
     * Cursor,ansonsten NOID.
     */
    public long getSelectionID() {
        return selectionID;
    }

    /**
     * @return Liefert den Text
     */
    public String getValue() {
        return getText().toString().trim();
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
    public void initialize(OnTextChangedListener mOnTextChangeListener, AWAbstractDBDefinition tbd,
                           String selection, String[] selectionArgs, int fromResID) {
        this.mOnTextChangeListener = mOnTextChangeListener;
        this.tbd = tbd;
        this.fromResID = fromResID;
        AbstractDBHelper mDBHelper =
                ((AWApplication) getContext().getApplicationContext()).getDBHelper();
        mMainColumn = mDBHelper.columnName(this.fromResID);
        mProjection =
                new String[]{mDBHelper.columnName(fromResID), mDBHelper.columnName(R.string._id)};
        buildSelectionArguments(selection, selectionArgs);
        SimpleCursorAdapter mSimpleCursorAdapter =
                new SimpleCursorAdapter(getContext(), android.R.layout.simple_dropdown_item_1line,
                        null, mProjection, viewResIDs, 0);
        mSimpleCursorAdapter.setCursorToStringConverter(this);
        mSimpleCursorAdapter.setFilterQueryProvider(this);
        setAdapter(mSimpleCursorAdapter);
    }

    /**
     * Ein Text kann nur gueltig sein, wenn die Textlaenge kleiner des selektierten Textes ist.
     */
    @Override
    public boolean isValid(CharSequence text) {
        return (text.toString().equals(selectedText));
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
        selectAll();
    }

    /**
     * Wenn die View den Fokus verliert, wird geprueft, ob neue Eintraeg zugelassen sind. Ist dies
     * nicht der Fall, wird der Text auf den zuletzt  gueltigen Text zurueckgesetzt und dieser Text
     * versendet.
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (previouslyFocusedRect == null) {
            setSelection(length());
            selectAll();
        }
        if (!focused) {
            if (isValidatorSet) {
                setText(selectedText);
            }
            sendMessage();
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    /**
     * Wenn ein List-Item ausgewaehlt wird, wird eine Message mit dem ausgewaehlten Text gesendet.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectionID = id;
        selectedText = getText().toString().trim();
        sendMessage();
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
            selectedText = data.getString(0).trim();
            if (data.getCount() == 1) {
                sendMessage();
            }
        }
        setDropDownHeight(getLineHeight() * 18);
        AbstractDBHelper mDBHelper =
                ((AWApplication) getContext().getApplicationContext()).getDBHelper();
        columnIndex = data.getColumnIndexOrThrow(mDBHelper.columnName(fromResID));
        return data;
    }

    /**
     * Wird bei Textaenderungen gerufen:
     * <p/>
     * 1. Es wird ein Item aus der Liste gewaehlt. Dann findet man unter {@link
     * AWAutoCompleteTextView#getSelectionID()} die ID, unter {@link AWAutoCompleteTextView#getSelectedText()}
     * den entsprechenden Text der Liste.
     * <p/>
     * 2a. Es ist kein Validator gesetzt: Dann findet man unter {@link
     * AWAutoCompleteTextView#getSelectionID()} die ID. diese ist NOID, wenn Text eingegeben wurde,
     * der nicht in der Liste vorhanden ist.Unter {@link AWAutoCompleteTextView#getSelectedText()}den
     * eingegebenen Text.
     * <p/>
     * 2b. Es ist ein Validator gesetzt: Es wird nach Lesen der Datenbank die selectionID mit der ID
     * und der Text mit dem Text des ersten gefundenen Wertes des Cursors vorbelegt.Diese ID findet
     * man dann unter {@link AWAutoCompleteTextView#getSelectionID()}. Unter {@link
     * AWAutoCompleteTextView#getSelectedText()}den zur ID gehoerenden Text. Es sind gibt dann keine
     * neuen Werte.
     */
    protected void sendMessage() {
        mOnTextChangeListener.onTextChanged(this, getSelectedText(), getSelectionID());
    }

    /**
     * Setzt sich selbst als Validator.
     *
     * @see AWAutoCompleteTextView#setAsValidator(Validator)
     */
    public void setAsValidator() {
        setAsValidator(this);
    }

    /**
     * Setzt einen Validator. Wird einer gesetzt, koennen nur Zeilen aus der Tabelle gewaehlt
     * werden.
     */
    public void setAsValidator(Validator validator) {
        super.setValidator(validator);
        isValidatorSet = true;
    }

    /**
     * Dann kann diese View mehrmals in einem Layout verwendet werden.
     *
     * @param index
     *         index
     */
    public void setBroadcastIndex(int index) {
        mBroadcastIndex = index;
    }

    public void setSelectedText(String text) {
        selectedText = text;
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
         *         ID aus der DB, wenn Nutzer ein Item aus dem Pulldown selektiert hat oder wenn der
         *         LoaderManager nur eine Zeile gefunden hat UND ein validator gesetzt ist.
         */
        void onTextChanged(View view, String newText, long newID);
    }
}
