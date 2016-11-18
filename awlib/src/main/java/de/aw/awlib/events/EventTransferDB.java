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
package de.aw.awlib.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.FileInputStream;
import java.io.IOException;

import de.aw.awlib.R;

/**
 * Transfer der DB au einen Server. Die Daten zum Server werden aus den Preferences ermittelt.
 */
public class EventTransferDB {
    private static final String dir = "monma";
    private static final String directory = "/files";
    private final String password;
    private final String user;
    private final String servername;

    /**
     * Liest die Zugangsdaten aus Preferences und uebertraegt das file auf den Server
     *
     * @param context
     *         Context. Wird fuer die SharedPreferences benoetigt
     * @param connectionArt
     *         Art der {@link ConnectionArt Verbingung}.
     * @param filename
     *         Filename auf dem Server
     */
    public EventTransferDB(Context context, ConnectionArt connectionArt, String filename)
            throws ConnectionFailsException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        servername = prefs.getString(context.getString(R.string.pkServerURL), null);
        user = prefs.getString(context.getString(R.string.pkServerUID), null);
        password = prefs.getString(context.getString(R.string.dbServerPW), null);
        if (prefs.getBoolean(context.getString(R.string.pkExterneSicherung), false)) {
            FTPClient client = null;
            switch (connectionArt) {
                case SSL:
                    client = new FTPSClient();
                    break;
                case NONSSL:
                    client = new FTPClient();
            }
            execute(client, filename);
        }
    }

    public EventTransferDB(String mServerName, String mUserName, String mPasswort,
                           ConnectionArt connectionArt) throws ConnectionFailsException {
        servername = mServerName;
        user = mUserName;
        password = mPasswort;
        FTPClient client = null;
        switch (connectionArt) {
            case SSL:
                client = new FTPSClient();
                break;
            case NONSSL:
                client = new FTPClient();
        }
        testConnection(client);
    }

    /**
     * Durchfuehrung des Transfers auf Server
     *
     * @param client
     *         FTP(S)Client, auf dem das File liegen soll
     * @param filename
     *         Name des zu uebertragenden File
     *
     * @return true, wenn erfolgreich
     *
     * @throws IOException
     *         bei Fehlern
     * @throws ConnectionFailsException
     *         bei Fehlern
     */
    private boolean execute(FTPClient client, String filename)
            throws IOException, ConnectionFailsException {
        testConnection(client);
        FileInputStream fis = new FileInputStream(filename);
        String[] file = filename.split("/");
        String remoteFileName = file[file.length - 1];
        boolean result = false;
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            result = client.storeFile(remoteFileName, fis);
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
        }
        return result;
    }

    /**
     * Testet eine Verbindung zum Server. Connect, Login und Wechsel in das Verzeichnis, wo die
     * Daten abgelegt werden sollen.
     *
     * @param client
     *         FTP(S)Client
     *
     * @return true, wenn erfolgreich
     *
     * @throws ConnectionFailsException,
     *         wenn die Verbindung fehlgeschlagen ist.
     */
    private boolean testConnection(FTPClient client) throws ConnectionFailsException {
        try {
            client.connect(servername, 21);
            client.enterLocalPassiveMode();
            if (!client.login(user, password)) {
                throw new ConnectionFailsException(client.getReplyStrings());
            }
            if (client instanceof FTPSClient) {
                // Set protection buffer size
                ((FTPSClient) client).execPBSZ(0);
                // Set data channel protection to private
                ((FTPSClient) client).execPROT("P");
            }
            if (!client.changeWorkingDirectory(directory)) {
                throw new ConnectionFailsException(client.getReplyStrings());
            }
            client.makeDirectory(dir);
            if (!client.changeWorkingDirectory(dir)) {
                throw new ConnectionFailsException(client.getReplyStrings());
            }
        } catch (IOException e) {
            throw new ConnectionFailsException(client.getReplyStrings());
        }
        return true;
    }

    /**
     * Arten der Verbindungen zu einem Server.
     */
    public enum ConnectionArt {
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

        public ConnectionFailsException(String[] status) {
            super("Fehler bei der Verbindung mit Server");
            this.status = status;
        }

        /**
         * @return Statusmeldungen des Servers bei Fehlern.
         */
        public String[] getStatus() {
            return status;
        }
    }
}
