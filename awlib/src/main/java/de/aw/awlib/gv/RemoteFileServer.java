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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * RemoteServer. Transfer von Files, filelist...
 */
public class RemoteFileServer {
    private final String mURL;
    private final String mUserID;
    private final String mUserPassword;
    private final ConnectionType mConnectionType;
    private FTPClient mClient;

    /**
     * Neuer RemoteServer. Testet sofort die Verbindung.
     *
     * @param serverURL
     *         URL des Servers
     * @param username
     *         Username
     * @param passwort
     *         Passort
     * @param connectionType
     *         Typ der Verbindung gemaess {@link ConnectionType}
     *
     * @throws ConnectionFailsException
     *         Wenn die Verbindung fehlgeschlagen ist
     */
    public RemoteFileServer(String serverURL, String username, String passwort,
                            ConnectionType connectionType) throws ConnectionFailsException {
        mURL = serverURL;
        mUserID = username;
        mUserPassword = passwort;
        mConnectionType = connectionType;
        mClient = getFTPClient();
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
    private FTPClient getFTPClient() throws ConnectionFailsException {
        FTPClient client = mClient;
        if (mConnectionType != null) {
            if (mClient == null) {
                switch (mConnectionType) {
                    case SSL:
                        client = new FTPSClient();
                        break;
                    case NONSSL:
                        client = new FTPClient();
                }
            }
            try {
                if (!client.isConnected()) {
                    client.connect(mURL, 21);
                    client.enterLocalPassiveMode();
                    if (!client.login(mUserID, mUserPassword)) {
                        throw new ConnectionFailsException(client);
                    }
                    if (client instanceof FTPSClient) {
                        // Set protection buffer size
                        ((FTPSClient) client).execPBSZ(0);
                        // Set data channel protection to private
                        ((FTPSClient) client).execPROT("P");
                    }
                }
            } catch (IOException e) {
                throw new ConnectionFailsException(client);
            }
        }
        return client;
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
    public FTPFile[] listFiles(String directory) throws ConnectionFailsException {
        FTPClient client = getFTPClient();
        try {
            return client.listFiles(directory);
        } catch (IOException e) {
            throw new ConnectionFailsException(client);
        }
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server im Root-Directory
     *
     * @return FTPFile-Array
     *
     * @throws IOException
     *         bei Fehlern.
     */
    public FTPFile[] listFiles() throws ConnectionFailsException {
        return listFiles("/");
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
        FTPClient client = getFTPClient();
        FileInputStream fis = null;
        boolean result = false;
        try {
            fis = new FileInputStream(file);
            client.changeWorkingDirectory(remoteDirectory);
            client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            result = client.storeFile(file.getName(), fis);
        } catch (FileNotFoundException e) {
            throw new ConnectionFailsException(client);
        } finally {
            try {
                fis.close();
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
            return result;
        }
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

