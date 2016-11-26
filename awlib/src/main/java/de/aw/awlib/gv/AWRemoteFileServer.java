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
package de.aw.awlib.gv;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import de.aw.awlib.R;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.database_private.AWDBDefinition;
import de.aw.awlib.utils.AWRemoteFileServerHandler.ConnectionType;

import static de.aw.awlib.utils.AWRemoteFileServerHandler.ConnectionType.SSL;

/**
 * Stammdaten fuer einen AWRemoteFileServer.
 */
public class AWRemoteFileServer extends AWApplicationGeschaeftsObjekt {
    public static final Creator<AWRemoteFileServer> CREATOR = new Creator<AWRemoteFileServer>() {
        @Override
        public AWRemoteFileServer createFromParcel(Parcel source) {
            return new AWRemoteFileServer(source);
        }

        @Override
        public AWRemoteFileServer[] newArray(int size) {
            return new AWRemoteFileServer[size];
        }
    };
    private static final AWDBDefinition tbd = AWDBDefinition.RemoteServer;
    private final SharedPreferences prefs;
    private ConnectionType mConnectionType;
    private String mMainDirectory;
    private String mURL;
    private String mUserID;
    private String mUserPassword;

    /**
     * Neuer RemoteServer.
     *
     * @param serverURL
     *         URL des Servers
     * @param username
     *         Username
     * @param passwort
     *         Passort
     * @param connectionType
     *         Typ der Verbindung gemaess {@link ConnectionType}
     */
    public AWRemoteFileServer(@NonNull String serverURL, @NonNull String username,
                              @NonNull String passwort, ConnectionType connectionType) {
        super(tbd);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mURL = serverURL;
        mUserID = username;
        mUserPassword = passwort;
        mConnectionType = connectionType;
        put(R.string.column_connectionType, mConnectionType.name());
        put(R.string.column_serverurl, mURL);
        put(R.string.column_userID, mUserID);
    }

    public AWRemoteFileServer() {
        super(tbd);
        put(R.string.column_connectionType, SSL.name());
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public AWRemoteFileServer(AWAbstractDBDefinition tbd, Long id) throws LineNotFoundException {
        super(tbd, id);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mURL = getAsString(R.string.column_serverurl);
        mUserID = getAsString(R.string.column_serverurl);
        mMainDirectory = getAsString(R.string.column_maindirectory);
        this.mConnectionType = ConnectionType.valueOf(getAsString(R.string.column_connectionType));
        if (mURL != null) {
            mUserPassword = prefs.getString(mURL, null);
        }
    }

    protected AWRemoteFileServer(Parcel in) {
        super(in);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.mURL = in.readString();
        this.mUserID = in.readString();
        this.mUserPassword = in.readString();
        this.mMainDirectory = in.readString();
        int tmpMConnectionType = in.readInt();
        this.mConnectionType =
                tmpMConnectionType == -1 ? null : ConnectionType.values()[tmpMConnectionType];
    }

    @Override
    public int delete(AbstractDBHelper db) {
        int result = super.delete(db);
        if (result != 0) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(mURL).apply();
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getBackupDirectory() {
        return mMainDirectory;
    }

    public ConnectionType getConnectionType() {
        return mConnectionType;
    }

    /**
     * @return URL des Servers
     */
    public String getURL() {
        return mURL;
    }

    /**
     * @return UserID
     */
    public String getUserID() {
        return mUserID;
    }

    /**
     * @return Passwort
     */
    public String getUserPassword() {
        return mUserPassword;
    }

    @Override
    public long insert(AbstractDBHelper db) {
        long id = -1;
        if (isValid()) {
            id = super.insert(db);
            if (id != -1) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(mURL, mUserPassword).apply();
            }
        }
        return id;
    }

    public boolean isValid() {
        return mURL != null && mUserID != null && mUserPassword != null && mConnectionType != null;
    }

    @Override
    public boolean put(int resID, Object value) {
        if (resID == R.string.column_serverurl) {
            mURL = (String) value;
        } else if (resID == R.string.column_userID) {
            mUserID = (String) value;
        } else if (resID == R.string.column_maindirectory) {
            mMainDirectory = (String) value;
        } else if (resID == R.string.column_connectionType) {
            mConnectionType = ConnectionType.valueOf((String) value);
        }
        return super.put(resID, value);
    }

    public void setMainDirectory(String mMainDirectory) {
        put(R.string.column_maindirectory, mMainDirectory);
    }

    /**
     * @return Passwort
     */
    public void setUserPassword(String password) {
        mUserPassword = password;
    }

    @Override
    public int update(AbstractDBHelper db) {
        if (!isValid()) {
            return 0;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(mURL, mUserPassword).apply();
        return super.update(db);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mURL);
        dest.writeString(this.mUserID);
        dest.writeString(this.mUserPassword);
        dest.writeString(this.mMainDirectory);
        dest.writeInt(this.mConnectionType == null ? -1 : this.mConnectionType.ordinal());
    }
}

