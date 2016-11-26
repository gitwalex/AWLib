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
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.database.AWAbstractDBDefinition;

/**
 * AutoCompleteTextView (siehe  {@link AWAutoCompleteTextView#initialize (DBDefinition, String,
 * String[], boolean, int[])}.<br> Sendet eine Message nach einer TextAenderung. Threshold ist
 * standardmaessig 3.
 *
 * @see AWAutoCompleteTextView#sendMessage()
 */
public abstract class AWAutoCompleteTextView extends AutoCompleteTextView
        implements AWInterface, SimpleCursorAdapter.CursorToStringConverter, FilterQueryProvider,
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>,
        AutoCompleteTextView.Validator {
    private static final String CONSTRAINT = "CONSTRAINT", USERSCELECTION = "USERSELECTION",
            USERSELECTIONARGS = "USERSELECTIONARGS";
    private static int staticLoaderID;
    private static int[] viewResIDs = new int[]{android.R.id.text1};
    protected OnTextChangedListener mOnTextChangeListener;
    private Bundle args = new Bundle();
    private int columnIndex;
    private int fromResID;
    private boolean isValidatorSet;
    private int mBroadcastIndex;
    private int mLoaderID;
    private LoaderManager mLoaderManager;
    private SimpleCursorAdapter mSimpleCursorAdapter;
    private String mainColumn;
    private String[] projection;
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
     * @return Liefert die Anzahl der Zeilen im Cursor zurueck.
     */
    public int getNumberOfRows() {
        return mSimpleCursorAdapter.getCount();
    }

    /**
     * Liefert den gueltigen Text zurueck. Entweder den im Textfeld, ist ein Validator gesetzt, der
     * entsprechende Text aus dem Cursor.
     *
     * @return Text
     */
    public String getSelectedText() {
        return selectedText;
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
        return getText().toString();
    }

    /**
     * Initialisiert AutoCompleteTextView.
     *
     * @param loaderManager
     *         LoaderManager
     * @param tbd
     *         DBDefinition. Aus dieser Tabelle wird das Feld gelesen
     * @param selection
     *         selection
     * @param selectionArgs
     *         Argumente zur Selection
     * @param fromResID
     *         Feld, welches fuer die Selection benutzt werden soll.
     *
     * @throws NullPointerException,
     *         wenn LoaderManager null ist.
     */
    public void initialize(LoaderManager loaderManager, OnTextChangedListener mOnTextChangeListener,
                           AWAbstractDBDefinition tbd, String selection, String[] selectionArgs,
                           int fromResID) {
        if (loaderManager == null) {
            throw new NullPointerException("LoaderManager darf nicht null sein");
        }
        this.mOnTextChangeListener = mOnTextChangeListener;
        this.tbd = tbd;
        this.fromResID = fromResID;
        mainColumn = tbd.columnName(this.fromResID);
        projection = new String[]{tbd.columnName(fromResID), tbd.columnName(R.string._id)};
        args.putString(USERSCELECTION, selection);
        args.putStringArray(USERSELECTIONARGS, selectionArgs);
        args.putStringArray(PROJECTION, projection);
        args.putString(ORDERBY, mainColumn);
        mLoaderManager = loaderManager;
        mSimpleCursorAdapter =
                new SimpleCursorAdapter(getContext(), android.R.layout.simple_dropdown_item_1line,
                        null, projection, viewResIDs, 0);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        projection = args.getStringArray(PROJECTION);
        String constraint = args.getString(CONSTRAINT, "");
        String selection = tbd.columnName(fromResID) + " Like ? ";
        String[] selectionArgs = new String[]{"%" + constraint + "%"};
        String userSelection = args.getString(USERSCELECTION);
        if (userSelection != null) {
            String[] userSelectionArgs = args.getStringArray(USERSELECTIONARGS);
            if (userSelectionArgs != null) {
                for (String sel : userSelectionArgs) {
                    userSelection = userSelection.replaceFirst("\\?", "'" + sel + "'");
                }
            }
            selection = selection + " AND (" + userSelection + ")";
        }
        selection = selection + "  GROUP BY " + mainColumn;
        String mOrderBy = "LENGTH(" + mainColumn + ")";
        String orderBy = args.getString(ORDERBY);
        if (orderBy == null) {
            orderBy = mOrderBy;
        } else {
            orderBy = mOrderBy + ", " + orderBy;
        }
        return new CursorLoader(getContext(), tbd.getUri(), projection, selection, selectionArgs,
                orderBy);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setThreshold(3);
        setOnItemClickListener(this);
        setSelectAllOnFocus(true);
        selectAll();
        mLoaderID = staticLoaderID++;
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
     * Hat der Cursor Daten und validierung ist eingeschaltet (es ist kein neuer Wert zugelassen),
     * wird die erste ID aus dem Cursor geholt und der  Text auf den entsprechenden Wert des Cursors
     * gesetzt. Ausserdem wird dann gleich eine Message versendet, wenn es nur genau einen Wert
     * gibt.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        selectionID = NOID;
        if (data != null && data.moveToFirst()) {
            if (isValidatorSet) {
                selectionID = data.getLong(1);
                selectedText = data.getString(0);
                if (data.getCount() == 1) {
                    sendMessage();
                }
            }
        }
        setDropDownHeight(getLineHeight() * 18);
        columnIndex = data.getColumnIndexOrThrow(tbd.columnName(fromResID));
        mSimpleCursorAdapter.swapCursor(data);
    }

    /**
     * Adapter leeren
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSimpleCursorAdapter.swapCursor(null);
    }

    /**
     * Restartet die TextView mit neuen Argumenten
     *
     * @param selection
     *         neue selectio
     * @param selectionArgs
     *         neue SelectionArgs
     */
    protected void restart(String selection, String[] selectionArgs) {
        args.putString(USERSCELECTION, selection);
        args.putStringArray(USERSELECTIONARGS, selectionArgs);
        startOrRestartLoader(mLoaderID, args);
    }

    /**
     * Nach tippen wird hier nachgelesen. Es wird mit 'LIKE %constraint%' ausgewaehlt.
     *
     * @param constraint
     *         Bisheriger Text
     *
     * @return null. Der neue Cursor wird durch {@link AWAutoCompleteTextView#startOrRestartLoader(int,
     * Bundle)} erstellt.
     */
    @Override
    public Cursor runQuery(CharSequence constraint) {
        if (constraint == null) {
            constraint = "";
        }
        args.putString(CONSTRAINT, constraint.toString());
        startOrRestartLoader(mLoaderID, args);
        return null;
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
     * Startet oder restartet den Loader
     *
     * @param id
     *         id des Loaders
     * @param args
     *         args fuer Loader
     */
    private void startOrRestartLoader(int id, Bundle args) {
        Loader<Cursor> loader = mLoaderManager.getLoader(id);
        if (loader != null && !loader.isReset()) {
            mLoaderManager.restartLoader(id, args, this);
        } else {
            mLoaderManager.initLoader(id, args, this);
        }
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
