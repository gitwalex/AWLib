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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.aw.awlib.R;
import de.aw.awlib.database.AWLibAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.database_private.AWLibDBDefinition;

/**
 * RemoteServer. Transfer von Files, filelist...
 */
public class RemoteFileServer extends AWLibApplicationGeschaeftsObjekt {
    public static final Creator<RemoteFileServer> CREATOR = new Creator<RemoteFileServer>() {
        @Override
        public RemoteFileServer createFromParcel(Parcel source) {
            return new RemoteFileServer(source);
        }

        @Override
        public RemoteFileServer[] newArray(int size) {
            return new RemoteFileServer[size];
        }
    };
    private static final AWLibDBDefinition tbd = AWLibDBDefinition.RemoteServer;
    private final SharedPreferences prefs;
    private FTPClient mClient;
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
    public RemoteFileServer(@NonNull String serverURL, @NonNull String username,
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

    public RemoteFileServer() {
        super(tbd);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public RemoteFileServer(AWLibAbstractDBDefinition tbd, Long id) throws LineNotFoundException {
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

    protected RemoteFileServer(Parcel in) {
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
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(mURL).apply();
        return super.delete(db);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Erstellt ggfs. einen Client entsprechend des {@link ConnectionType}. Gibt es keine
     * Verbindung, wird diese aufgebaut und eingeloggt.
     *
     * @return FTPClient
     *
     * @throws ConnectionFailsException,
     *         wenn die Verbindung fehlgeschlagen ist.
     */
    private void getFTPClient() throws ConnectionFailsException {
        if (mConnectionType != null) {
            if (mClient == null) {
                switch (mConnectionType) {
                    case SSL:
                        mClient = new FTPSClient();
                        break;
                    case NONSSL:
                        mClient = new FTPClient();
                }
            }
            try {
                if (!mClient.isConnected()) {
                    mClient.connect(mURL, 21);
                    mClient.enterLocalPassiveMode();
                    if (!mClient.login(mUserID, mUserPassword)) {
                        throw new ConnectionFailsException(mClient);
                    }
                    if (mClient instanceof FTPSClient) {
                        // Set protection buffer size
                        ((FTPSClient) mClient).execPBSZ(0);
                        // Set data channel protection to private
                        ((FTPSClient) mClient).execPROT("P");
                    }
                }
            } catch (IOException e) {
                throw new ConnectionFailsException(mClient);
            }
        }
    }

    public String getMainDirectory() {
        return mMainDirectory;
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
        if (!isValid()) {
            return -1;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(mURL, mUserPassword).apply();
        return super.insert(db);
    }

    public boolean isValid() {
        return mURL != null && mUserID != null && mUserPassword != null;
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server im Root-Directory
     *
     * @param filter
     *         FileFilter
     *
     * @return FTPFile-Array
     *
     * @throws IOException
     *         bei Fehlern.
     */
    public FTPFile[] listFiles(FTPFileFilter filter) throws ConnectionFailsException {
        return listFiles("/", filter);
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server zu einem Directory
     *
     * @param directory
     *         Directory
     *
     * @return FTPFile-Array
     *
     * @throws IOException
     *         bei Fehlern.
     */
    public FTPFile[] listFiles(String directory, FTPFileFilter filter)
            throws ConnectionFailsException {
        try {
            if (filter != null) {
                return mClient.listFiles(directory, filter);
            } else {
                return mClient.listFiles(directory);
            }
        } catch (IOException e) {
            throw new ConnectionFailsException(mClient);
        }
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
            return true;
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

    public boolean testConnection() throws ConnectionFailsException {
        if (mConnectionType == null) {
            put(R.string.column_connectionType, ConnectionType.SSL.name());
            try {
                getFTPClient();
            } catch (ConnectionFailsException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
                put(R.string.column_connectionType, ConnectionType.NONSSL.name());
                getFTPClient();
            }
        } else {
            getFTPClient();
            return true;
        }
        return true;
    }

    /**
     * Durchfuehrung des Transfers auf Server
     *
     * @param file
     *         das zu uebertragende File. Der Name des File auf dem Server entspriehc tdem namen
     *         dieses Files
     * @param remoteDirectory
     *         Verzeichnis auf dem Server, in dem das File gespeichert werden soll.
     *
     * @return true, wenn erfolgreich
     *
     * @throws IOException
     *         bei Fehlern
     * @throws ConnectionFailsException
     *         bei Fehlern
     */
    public boolean transferFileToServer(File file, String remoteDirectory)
            throws ConnectionFailsException {
        FileInputStream fis = null;
        boolean result = false;
        try {
            fis = new FileInputStream(file);
            mClient.changeWorkingDirectory(remoteDirectory);
            mClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            mClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            result = mClient.storeFile(file.getName(), fis);
        } catch (FileNotFoundException e) {
            throw new ConnectionFailsException(mClient);
        } finally {
            try {
                fis.close();
                if (mClient.isConnected()) {
                    mClient.logout();
                    mClient.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
            return result;
        }
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

    /**
     * Arten der Verbindungen zu einem Server.
     */
    public enum ConnectionType {
        /**
         * Uebertragung via SSL
         */
        SSL,
        /**
         * Uebertragung unverschluesselt
         */
        NONSSL
    }

    /**
     * Exception, wenn bei der  Verbindung zum Server Fehler festgestellt wurden
     */
    public static class ConnectionFailsException extends Throwable {
        private final String[] status;

        /**
         * @param client
         *         der ausloesende Client
         */
        public ConnectionFailsException(FTPClient client) {
            super("Fehler bei der Verbindung mit Server");
            this.status = client.getReplyStrings();
        }

        /**
         * @return Statusmeldungen des Servers bei Fehlern.
         */
        public String[] getStatus() {
            return status;
        }
    }
}

