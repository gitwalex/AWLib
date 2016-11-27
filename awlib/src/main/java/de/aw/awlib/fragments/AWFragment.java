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
package de.aw.awlib.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.activities.AWMainActivity;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.gv.AWApplicationGeschaeftsObjekt;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Template fuer MonMaFragmente
 * <p/>
 * Folgende Funktionen:
 * <p>
 * Bereitstellung eines Bundle 'args' fuer alle abgeleiteten Klassen
 */
public abstract class AWFragment extends DialogFragment
        implements AWInterface, AWFragmentInterface, DialogInterface.OnClickListener {
    /**
     * TAG Fuer die Fragmente
     */
    public final String TAG = this.getClass().getSimpleName();
    /**
     * Bundle fuer ein Fragment. Wird in onCreate() wiederhergestellt und in OnSaveStateInstance()
     * gesichert.
     */
    protected final Bundle args = new Bundle();
    /**
     * Layout des Fragments
     */
    protected int layout = NOLAYOUT;
    /**
     * SharedPreferences werden allen abgeleiteten Fragmenten bereitgestellt
     */
    protected SharedPreferences prefs;
    /**
     * Hiew wird die awlib_containerID gespeichert, in die das Fragment engehaengt wird.
     */
    protected int awlib_containerID;
    protected boolean isCanceled;
    protected int[] viewResIDs;
    protected int[] fromResIDs;
    protected AWApplicationGeschaeftsObjekt awlib_gv;
    protected MainAction mainAction;
    private OnAWFragmentCancelListener mOnCancelListener;
    /**
     *
     */
    private OnAWFragmentDismissListener mOnDismissListener;
    /**
     * Dient zur Berechnung der Startdauer
     */
    private long timer;

    public AWFragment() {
        timer = System.currentTimeMillis();
    }

    protected final void Log(String message) {
        AWApplication.Log(message);
    }

    protected final void LogError(String message) {
        AWApplication.LogError(message);
    }

    /**
     * Wird aus {@link MyTextWatcher#afterTextChanged(TextView, int, String)} aufgerufen.
     *
     * @param view
     *         View, deren Text geaendert wurde
     * @param identifier
     *         identifier gemaess Konstructor
     * @param newText
     *         Neuer Text
     */
    protected void afterTextChanged(TextView view, int identifier, String newText) {
    }

    protected String getActionBarSubTitle() {
        return args.getString(ACTIONBARSUBTITLE, null);
    }

    /**
     * AWFragment benoetigt fuer Insert/Update einen DBHelper.
     *
     * @return AbstractDBHelper
     *
     * @throws IllegalStateException
     *         wenn das erbende Fragment keinen AbstractDBHelper liefert.
     */
    public AbstractDBHelper getDBHelper() {
        throw new IllegalStateException("Fragment muss getDBHelper() ueberschreiben");
    }

    public MainAction getMainAction() {
        return mainAction;
    }

    @Override
    public final String getTAG() {
        return TAG;
    }

    /**
     * Die aktuellen Preferences werden ermittelt und in prefs gespeichert
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Ermoeglicht das manuelle binden der View.
     *
     * @param view
     *         View, die gebunden werden soll
     * @param resID
     *         resID der View
     *
     * @return true, wenn die View hier gebunden wurde. Wenn false, wird davon ausgegangen, dass es
     * sich um eine TextView handelt, der Text wird dann dort eingestellt. siehe auch
     * onViewCreated()
     */
    protected boolean onBindView(View view, int resID) {
        return false;
    }

    /**
     * Wird ein Dialog gecancelt, wird der mOnCancelListener gerufen (wenn vorhanden)
     * <p>
     * Ausserdem ist dann isCanceled true.
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        isCanceled = true;
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(layout, dialog);
        }
        super.onCancel(dialog);
    }

    /**
     * Wenn OK gewaehlt, wird der Geschaeftsvorfall gespeichert und eine SnackBar gezeigt.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                switch (mainAction) {
                    case ADD:
                    case EDIT:
                        AbstractDBHelper db = getDBHelper();
                        if (awlib_gv.isInserted()) {
                            awlib_gv.update(db);
                        } else {
                            awlib_gv.insert(db);
                        }
                        Snackbar.make(getView(), getString(R.string.awlib_datensatzGesichert),
                                Snackbar.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                dismiss();
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            default:
        }
    }

    /**
     * Setzen der durch setArguments(args) erhaltenen bzw. Ruecksichern der Argumente im Bundle
     * args.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        } else {
            Bundle argumente = getArguments();
            if (argumente != null) {
                args.putAll(argumente);
            }
            setInternalArguments(args);
        }
        layout = args.getInt(LAYOUT, NOLAYOUT);
        mainAction = args.getParcelable(AWLIBACTION);
        viewResIDs = args.getIntArray(VIEWRESIDS);
        fromResIDs = args.getIntArray(FROMRESIDS);
        String actionBarSubTitle = getActionBarSubTitle();
        if (actionBarSubTitle != null) {
            setSubtitle(actionBarSubTitle);
        }
    }

    /**
     * Erstellt einen Dialog mit Positive und Negative-Button. Die View fuer den Dailog wird ueber
     * LAYOUT in args ermittelt und ind den Dialog eingestellt.  Der Dailog wird so eingestellt,
     * dass ein Resize der View moeglich ist. Als ButtonListener wird das AWFragment eingestellt,
     * daher ist ggfs, die Methode {@link AWFragment#onClick(DialogInterface, int)} zu
     * ueberschreiben.
     * <p>
     * Nach Erstellen der View wird {@link AWFragment#onViewCreated(View, Bundle)} gerufen.
     * <p>
     * Ausserdem wird setRetainInstance(true) gesetzt, damit der Dialog bei einem
     * ConfigurationChange nicht verschwindet
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View childView = inflater.inflate(layout, null);
        onViewCreated(childView, savedInstanceState);
        builder.setView(childView);
        Dialog dialog = builder.setPositiveButton(R.string.awlib_btnAccept, this)
                .setNegativeButton(R.string.awlib_btnCancel, this).setView(childView).create();
        // Wenn das Dialogfenster teilweise von der eingeblendeten Tatstatur
        // ueberlappt wird, resize des Fensters zulassen.
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setRetainInstance(true);
        return dialog;
    }

    /**
     * Handelt es sich um einen Dialog, wird null zurueckgegeben. Ansoonsten wird die View erstellt
     * und zurueckgegeben.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!getShowsDialog()) {
            if (container != null) {
                awlib_containerID = container.getId();
            }
            if (layout == NOLAYOUT) {
                return null;
            }
            return inflater.inflate(layout, container, false);
        }
        return null;
    }

    /**
     * Nur loggen, dass Fragment destroeyed wurde.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log("Fragment " + getClass().getSimpleName() + " destroyed");
    }

    /**
     * Der Dailog verschwindet bei eine Aenderung der Configuration (z.B. drehen). Wenn das Fragment
     * retainInstance(true) gesetz hat, wird dies mit diesem Code verhindert. @see <a
     * href="https://code.google.com/p/android/issues/detail?id=17423">Hinweis</a>
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * Bei Dismiss wird ein OnDismissListener informiert(wenn vorhanden).
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!isCanceled && mOnDismissListener != null) {
            mOnDismissListener.onDismiss(layout, dialog);
        }
    }

    /**
     * Deregistrierung als OnSharedPreferenceListener, wenn die Klasse eine Instanz von
     * OnSharedPreferenceChangeListener ist
     */
    @Override
    public void onPause() {
        super.onPause();
        if (this instanceof SharedPreferences.OnSharedPreferenceChangeListener) {
            prefs.unregisterOnSharedPreferenceChangeListener(
                    (SharedPreferences.OnSharedPreferenceChangeListener) this);
        }
    }

    /**
     * Ist die Klasse eine Instanz von OnSharedPreferenceChangeListener, wird diese als {@link
     * SharedPreferences#registerOnSharedPreferenceChangeListener} registriert.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (this instanceof SharedPreferences.OnSharedPreferenceChangeListener) {
            prefs.registerOnSharedPreferenceChangeListener(
                    (SharedPreferences.OnSharedPreferenceChangeListener) this);
        }
    }

    /**
     * Sichern des bereitgestellten Bundles args. Wird in onCreate() wiederhergestellt.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(args);
        super.onSaveInstanceState(outState);
    }

    /**
     * Ausgabe Dauer des Starts des Fragments
     */
    @Override
    public void onStart() {
        super.onStart();
        if (timer != 0) {
            Log("Fragment " + getClass().getSimpleName() + " Startdauer: " + String
                    .valueOf(System.currentTimeMillis() - timer));
            timer = 0;
        }
    }

    /**
     * Wird ein Dialog angezeigt und ein Array viewResIDs ist nicht null, wird die View mit Daten
     * versorgt. Aufrufende Klasse kann in bindView() die View selbst belegen.
     * <p>
     * Nur wenn die (Text-/EditText-) View nicht selbst belegt wurde, passiert folgendes:
     * <p>
     * Handelt es sich bei der View um eine EditText-View, wird ein {@link MyTextWatcher} auf die
     * View gesetzt. Ausserdem werden die Werte direkt in den GV uebernommen (nur, wenn awlib_gv
     * nicht null ist).
     * <p>
     * Handelt es sich bei der View um eine TextView und awlib_gv ist nicht null, wird der Text aus
     * dem gv anhand der fromresID geholt und als Text eingestellt.
     * <p>
     * Damit dieses Feature benutzt werden kann sind folgende Voraussetzungen zu erfuellen:
     * <p>
     * VIEWRESIDS in args ist mit den resIDs der View belegt.
     * <p>
     * FROMRESIDS in args ist mit den entsprechend korrespondierenen fromResIDs zu viewResIDs
     * belegt.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getShowsDialog() && viewResIDs != null) {
            AWLibViewHolder holder = new AWLibViewHolder(view);
            for (int i = 0; i < viewResIDs.length; i++) {
                View target = holder.findViewById(viewResIDs[i]);
                if (!onBindView(target, viewResIDs[i])) {
                    int fromResID = fromResIDs[i];
                    if (target instanceof TextView && awlib_gv != null) {
                        TextView v = (TextView) target;
                        v.setText(awlib_gv.getAsString(fromResID));
                    }
                    if (target instanceof EditText) {
                        EditText v = (EditText) target;
                        v.addTextChangedListener(new MyTextWatcher(v, fromResID));
                    }
                }
            }
        }
    }

    /**
     * Uebernimmt Argumente zusaetzlich(!) zu bereits vorhandenen
     *
     * @see Fragment#setArguments(Bundle)
     */
    public final void setArguments(Bundle args) {
        Bundle oldArgs = getArguments();
        if (oldArgs != null) {
            if (args != null) {
                oldArgs.putAll(args);
            }
        } else {
            super.setArguments(args);
        }
    }

    /**
     * Methode wird aus onCreate gerufen. Im ubergebenen Bundle sind alle Argumente gespeichert, die
     * im Lifecycle des Fragments eingefuegt/gesichert wurden.
     * <p>
     * Zweckmaessigerweise wird zuerst super.setInternalArguments(args) gerufen. Danach sind in args
     * die Argumente vorhanden.  Es koennen auch noch weitere Argumente zum Initialisieren genau
     * dieses Fragments gesetzt werden.
     * <p>
     * Argumente, die von einem vererbten Fragment gesetzt werden, sind aber noch nicht vorhanden.
     * Werden diese benoetigt, sollten diese fruehestens in onCreate(saveStateInstance) aus args
     * geholt werden.
     * <p>
     * Als Default wird die MainAction SHOW gesetzt.
     *
     * @param args
     *         Bundel, welches in {@link Fragment#onSaveInstanceState(Bundle)} gesichert wird.
     */
    @CallSuper
    protected void setInternalArguments(Bundle args) {
        args.putParcelable(AWLIBACTION, MainAction.SHOW);
        args.putInt(LAYOUT, layout);
    }

    /**
     * @param listener
     *         erweiterter {@link OnAWFragmentCancelListener}
     */
    public void setOnCancelListener(OnAWFragmentCancelListener listener) {
        mOnCancelListener = listener;
    }

    /**
     * @param listener
     *         erweiterter {@link OnAWFragmentDismissListener}
     */
    public void setOnDismissListener(OnAWFragmentDismissListener listener) {
        mOnDismissListener = listener;
    }

    /**
     * Setzt den Subtitle in der SupportActionBar
     *
     * @param subTitle
     *         Text des Subtitles
     */
    public void setSubtitle(String subTitle) {
        ActionBar bar = ((AWMainActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            bar.setSubtitle(subTitle);
            args.putString(ACTIONBARSUBTITLE, subTitle);
        }
    }

    /**
     * Setzt den Subtitle in der SupportActionBar
     *
     * @param subTitleResID
     *         resID des Subtitles
     */
    public void setSubtitle(int subTitleResID) {
        String subtitle = getString(subTitleResID);
        setSubtitle(subtitle);
    }

    /**
     * Erweiterter Dialog-Cancel-Listener. Liefert zusaetzlich zum Dialog auch die layoutID des
     * Fragments mit.
     */
    public interface OnAWFragmentCancelListener {
        /**
         * Wird gerufen, wenn ein Dialog gecancelt wurde.Liefert zusaetzlich zum Dialog auch die
         * layoutID des Fragments mit.
         *
         * @param layoutID
         *         layout des Fragments
         * @param dialog
         *         Dialog
         */
        void onCancel(@LayoutRes int layoutID, DialogInterface dialog);
    }

    /**
     * Erweiterter Dialog-Dismiss-Listener. Liefert zusaetzlich zum Dialog auch die layoutID des
     * Fragments mit.
     */
    public interface OnAWFragmentDismissListener {
        /**
         * Wird gerufen, wenn ein Dialog beendet wurde.Liefert zusaetzlich zum Dialog auch die
         * layoutID des Fragments mit.
         *
         * @param layoutID
         *         layout des Fragments
         * @param dialog
         *         Dialog
         */
        void onDismiss(@LayoutRes int layoutID, DialogInterface dialog);
    }

    /**
     * TextWatcher, der auf in der View vorhandene EditTexte gelegt wird.
     */
    public class MyTextWatcher implements TextWatcher {
        private final EditText view;
        private final int identifier;

        /**
         * @param view
         *         EditText
         * @param identifier
         *         die ResID der Spalte des Geschaeftobjectes, in die der Text geschrieben wird.
         *         Dies wird immer in afterTextChanged(s) durchgefuehrt.
         */
        public MyTextWatcher(EditText view, int identifier) {
            this.view = view;
            this.identifier = identifier;
        }

        /**
         * Solbald sich der Text geaendert hat, wird in die durch den identifier vorgegebene Spalte
         * des Geschaeftspbjectes der Wert mittels put(resID, text) geschrieben.
         * <p>
         * Ist kein Text vorhanden, wird mittels remove(resID) der Wert aus der Spalte entfernt.
         * <p>
         * Ausserdem wird {@link AWFragment#afterTextChanged(TextView, int, String)} aufgerufen.
         *
         * @param s
         *         Text der EditTextView
         */
        @Override
        public void afterTextChanged(Editable s) {
            String newText = s.toString();
            if (!TextUtils.isEmpty(newText)) {
                awlib_gv.put(identifier, newText);
            } else {
                awlib_gv.remove(identifier);
            }
            AWFragment.this.afterTextChanged(view, identifier, newText);
        }

        /**
         * Leer
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /**
         * Leer
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}