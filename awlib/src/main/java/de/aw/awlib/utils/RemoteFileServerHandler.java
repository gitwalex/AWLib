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

import static de.aw.awlib.activities.AWLibInterface.linefeed;
import static de.aw.awlib.utils.RemoteFileServerHandler.Options.ListFilesInDirectory;
import static de.aw.awlib.utils.RemoteFileServerHandler.Options.TransferFile;

/**
 * Handler fuer Zugriffe auf einen FileServer. Alle Transaktionen werden in einem separatem
 * AsyncTask durchgefuehrt.
 */
public class RemoteFileServerHandler extends
        AsyncTask<RemoteFileServerHandler.Options, Void, RemoteFileServerHandler.ConnectionFailsException> {
    private final RemoteFileServerAdapter mRemoteFileServerAdapter;
    private final ExecutionListener mExecutionListener;
    private FTPClient mClient;
    private String mDestDirectoryName;
    private String mDirectory;
    private FTPFileFilter mFilter;
    private File mTransferFile;

    RemoteFileServerHandler(RemoteFileServerAdapter remoteFileServerAdapter,
                            ExecutionListener executionListener) {
        mRemoteFileServerAdapter = remoteFileServerAdapter;
        mClient = mRemoteFileServerAdapter.getFTPClient();
        mExecutionListener = executionListener;
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
                mClient.connect(mRemoteFileServerAdapter.getUrl(), 21);
                mClient.enterLocalPassiveMode();
                if (!mClient.login(mRemoteFileServerAdapter.getUserID(),
                        mRemoteFileServerAdapter.getUserPassword())) {
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

    protected ConnectionFailsException doInBackground(Options... params) {
        try {
            connectClient();
            switch (params[0]) {
                case ListAllFiles:
                case ListFilesInDirectory:
                    FTPFile[] result = executeListFiles(mDirectory, mFilter);
                    mRemoteFileServerAdapter.setFiles(result);
                    break;
                case TransferFile:
                    executeTransferFileToServer(mTransferFile, mDestDirectoryName);
                    break;
            }
            return null;
        } catch (ConnectionFailsException e) {
            return e;
        } finally {
            disconnectClient();
        }
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server im Root-Directory
     *
     * @param filter
     *         FileFilter
     *
     * @return FTPFile-Array
     *
     * @throws ConnectionFailsException
     *         bei Fehlern.
     */
    private FTPFile[] executeListFiles(FTPFileFilter filter) throws ConnectionFailsException {
        return executeListFiles("/", filter);
    }

    /**
     * Ermittelt alle Files auf dem Remote-Server zu einem Directory
     *
     * @param directory
     *         Directory
     *
     * @return FTPFile-Array
     *
     * @throws ConnectionFailsException
     *         bei Fehlern.
     */
    private FTPFile[] executeListFiles(String directory, FTPFileFilter filter)
            throws ConnectionFailsException {
        FTPFile[] files;
        try {
            if (filter != null) {
                files = mClient.listFiles(directory, filter);
            } else {
                files = mClient.listFiles(directory);
            }
        } catch (IOException e) {
            throw new ConnectionFailsException(mClient);
        }
        return files;
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
     * @throws ConnectionFailsException
     *         bei Fehlern
     */
    private boolean executeTransferFileToServer(File file, String remoteDirectory)
            throws ConnectionFailsException {
        FileInputStream fis = null;
        boolean result = false;
        try {
            fis = new FileInputStream(file);
            mClient.changeWorkingDirectory(remoteDirectory);
            mClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            mClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            result = mClient.storeFile(file.getName(), fis);
        } catch (IOException e) {
            throw new ConnectionFailsException(mClient);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * Listet alle Files im RootDirectory des Servers
     *
     * @param filter
     *         FileFilter. Kann null sein
     */
    public void listFiles(FTPFileFilter filter) {
        mDirectory = "/";
        listFilesInDiretory(mDirectory, filter);
    }

    /**
     * Listet alle Files im angegebenen Directory des Servers
     *
     * @param filter
     *         FileFilter. Kann null sein
     */
    public void listFilesInDiretory(String directory, FTPFileFilter filter) {
        mDirectory = directory;
        mFilter = filter;
        execute(ListFilesInDirectory);
    }

    /**
     * Ruft {@link ExecutionListener#onPostExecute(ConnectionFailsException)}
     */
    @Override
    protected void onPostExecute(ConnectionFailsException result) {
        mExecutionListener.onPostExecute(result);
    }

    /**
     * Ruft {@link ExecutionListener#onPreExecute()}
     */
    @Override
    protected void onPreExecute() {
        mExecutionListener.onPreExecute();
    }

    /**
     * Uebertraegt ein File auf den Server.
     *
     * @param transferFile
     *         File, welche uebertragen werden soll
     * @param destDirectoryName
     *         ZielDirectory
     */
    public void transferFile(File transferFile, String destDirectoryName) {
        mTransferFile = transferFile;
        mDestDirectoryName = destDirectoryName;
        execute(TransferFile);
    }

    /**
     * Ubertraegt ein File auf das Backupdirectory des Servers
     *
     * @param transferFile
     *         File, welches Uebertragen werden soll
     */
    public void transferFileToBackup(File transferFile) {
        mTransferFile = transferFile;
        mDestDirectoryName = mRemoteFileServerAdapter.getBackupDirectory();
        execute(TransferFile);
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
     * Moegliche Serveraktionen
     */
    enum Options {
        /**
         * Listet alle Files auf dem Server
         */
        ListAllFiles,
        /**
         * Listet alle Files eines Directories
         */
        ListFilesInDirectory,
        /**
         * Uebertraegt ein File zum Server
         */
        TransferFile
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
        void onPostExecute(RemoteFileServerHandler.ConnectionFailsException result);

        /**
         * Wird vor Beginn der Transaktion gerufen.
         */
        void onPreExecute();
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
}
