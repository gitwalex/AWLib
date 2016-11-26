package de.aw.awlib.utils;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.aw.awlib.gv.RemoteFileServer;

import static de.aw.awlib.activities.AWLibInterface.linefeed;

/**
 * Handler fuer Zugriffe auf einen FileServer. Alle Transaktionen werden in einem separatem
 * AsyncTask durchgefuehrt.
 */
public class RemoteFileServerHandler {
    private final RemoteFileServer mRemoteFileServer;
    private final ExecutionListener mExecutionListener;
    private final ConnectionType mConnectionType;
    private FTPClient mClient;
    private FTPFile[] mFiles;

    public RemoteFileServerHandler(RemoteFileServer remoteFileServer,
                                   ExecutionListener executionListener) {
        mRemoteFileServer = remoteFileServer;
        mClient = getFTPClient();
        mExecutionListener = executionListener;
        mConnectionType = remoteFileServer.getConnectionType();
    }

    /**
     * Erstellt ggfs. einen Client entsprechend des {@link ConnectionType}. Gibt es keine
     * Verbindung, wird diese aufgebaut und eingeloggt.
     *
     * @throws ConnectionFailsException,
     *         wenn die Verbindung fehlgeschlagen ist.
     */
    private void connectClient() throws ConnectionFailsException {
        try {
            if (!mClient.isConnected()) {
                mClient.connect(mRemoteFileServer.getURL(), 21);
                mClient.enterLocalPassiveMode();
                if (!mClient.login(mRemoteFileServer.getUserID(),
                        mRemoteFileServer.getUserPassword())) {
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

    /**
     * /** Loescht ein File vom Server
     *
     * @param pathname
     *         Vollstaendiger Pfad zum File
     */
    public void deleteFile(final String pathname) {
        new RemoteFileServerTask() {
            @Override
            protected ConnectionFailsException doInBackground(Void... params) {
                try {
                    if (!mClient.deleteFile(pathname)) {
                        return new ConnectionFailsException("File not found");
                    }
                    return null;
                } catch (IOException e) {
                    return new ConnectionFailsException(mClient);
                }
            }
        }.execute();
    }

    /**
     * Baut die Verbindung zum Server wieder ab.
     */
    private void disconnectClient() {
        if (mClient != null && mClient.isConnected()) {
            try {
                mClient.logout();
                mClient.disconnect();
            } catch (IOException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Liefert in Abhaengigkeit des {@link de.aw.awlib.utils.RemoteFileServerHandler.ConnectionType}
     * einen FTPClient zurueck.
     */
    private FTPClient getFTPClient() {
        if (mClient == null) {
            switch (mConnectionType) {
                case SSL:
                    mClient = new FTPSClient();
                    break;
                case NONSSL:
                    mClient = new FTPClient();
            }
        }
        return mClient;
    }

    /**
     * @return Liefert die in {@link RemoteFileServerHandler#listFilesInDirectory(String,
     * FTPFileFilter)} oder {@link RemoteFileServerHandler#listFiles(FTPFileFilter)} ermittelten
     * Files.
     */
    public FTPFile[] getFiles() {
        return mFiles;
    }

    /**
     * Listet alle Files im RootDirectory des Servers. Das Ergebnis kann durch {@link
     * RemoteFileServerHandler#getFiles()} abgeholt werden.
     *
     * @param filter
     *         FileFilter. Kann null sein
     */
    public void listFiles(FTPFileFilter filter) {
        listFilesInDirectory("/", filter);
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server zu einem Directory. Das Ergebnis kann durch {@link
     * RemoteFileServerHandler#getFiles()} abgeholt werden.
     *
     * @param directory
     *         Directory
     * @param filter
     *         FileFilter. Kann null sein
     */
    public void listFilesInDirectory(final String directory, final FTPFileFilter filter) {
        new RemoteFileServerTask() {
            @Override
            protected ConnectionFailsException doInBackground(Void... params) {
                FTPFile[] files;
                try {
                    connectClient();
                    if (filter != null) {
                        files = mClient.listFiles(directory, filter);
                    } else {
                        files = mClient.listFiles(directory);
                    }
                    setFiles(files);
                    return null;
                } catch (IOException e) {
                    return new ConnectionFailsException(mClient);
                } catch (ConnectionFailsException e) {
                    return e;
                } finally {
                    disconnectClient();
                }
            }
        }.execute();
    }

    /**
     * @param files
     *         die nach Abschluss von {@link RemoteFileServerHandler#listFilesInDirectory(String,
     *         FTPFileFilter)} oder {@link RemoteFileServerHandler#listFiles(FTPFileFilter)}
     *         ermittelten Files.
     */
    private void setFiles(FTPFile[] files) {
        mFiles = files;
    }

    /**
     * Uebertraegt ein File auf den Server.
     *
     * @param transferFile
     *         das zu uebertragende File. Der Name des File auf dem Server entspriehc dem Namen
     *         dieses Files
     * @param destDirectoryName
     *         Verzeichnis auf dem Server, in dem das File gespeichert werden soll.
     */
    public void transferFile(final File transferFile, final String destDirectoryName) {
        new RemoteFileServerTask() {
            @Override
            protected ConnectionFailsException doInBackground(Void... params) {
                FileInputStream fis = null;
                try {
                    connectClient();
                    fis = new FileInputStream(transferFile);
                    mClient.changeWorkingDirectory(destDirectoryName);
                    mClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                    mClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    if (!mClient.storeFile(transferFile.getName(), fis)) {
                        throw new ConnectionFailsException("Filetransfer failed");
                    }
                    return null;
                } catch (IOException e) {
                    return new ConnectionFailsException(mClient);
                } catch (ConnectionFailsException e) {
                    return e;
                } finally {
                    disconnectClient();
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            //TODO Execption bearbeiten
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.execute();
    }

    /**
     * Ubertraegt ein File auf das Backupdirectory des Servers
     *
     * @param transferFile
     *         File, welches Uebertragen werden soll
     */
    public void transferFileToBackup(File transferFile) {
        String mDestDirectoryName = mRemoteFileServer.getBackupDirectory();
        transferFile(transferFile, mDestDirectoryName);
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
     * Listener fuer auf dem Server laufende Transaktion
     */
    public interface ExecutionListener {
        /**
         * Wird nach Ende der Transaktion gerufen.
         *
         * @param result
         *         Die {@link de.aw.awlib.utils.RemoteFileServerHandler.ConnectionFailsException},
         *         wenn Fehler aufgetretn sind. Ansonsten null.
         */
        void onEndFileServerTask(RemoteFileServerHandler.ConnectionFailsException result);

        /**
         * Wird vor Beginn der Transaktion gerufen.
         */
        void onStartFileServerTask();
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
        ConnectionFailsException(FTPClient client) {
            super("Fehler bei der Verbindung mit Server");
            this.status = client.getReplyStrings();
        }

        /**
         * @param message
         *         Message der Exception
         */
        ConnectionFailsException(String message) {
            super(message);
            this.status = new String[]{message};
        }

        /**
         * @return Statusmeldungen des Servers bei Fehlern.
         */
        public String[] getStatus() {
            return status;
        }

        /**
         * @return Liefert einen aufbereiteten String mit den StatusMessages. Jede einzelne Zeile
         * des Status + linefeed
         */
        public String getStatusMessage() {
            StringBuilder s = new StringBuilder();
            for (String val : getStatus()) {
                s.append(val).append(linefeed);
            }
            return s.toString();
        }
    }

    /**
     * Template fuer einen AsyncTask zur ausfuehrung des Auftrags
     */
    private abstract class RemoteFileServerTask extends
            AsyncTask<Void, Void, de.aw.awlib.utils.RemoteFileServerHandler.ConnectionFailsException> {
        /**
         * Ruft {@link ExecutionListener#onEndFileServerTask(ConnectionFailsException)}
         */
        @Override
        protected void onPostExecute(ConnectionFailsException result) {
            mExecutionListener.onEndFileServerTask(result);
        }

        /**
         * Ruft {@link ExecutionListener#onStartFileServerTask()}
         */
        @Override
        protected void onPreExecute() {
            mExecutionListener.onStartFileServerTask();
        }
    }
}
