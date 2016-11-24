package de.aw.awlib.utils;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.aw.awlib.gv.RemoteFileServerExecuter;

import static de.aw.awlib.activities.AWLibInterface.linefeed;
import static de.aw.awlib.utils.FileServerExecuter.Options.ListFilesInDirectory;
import static de.aw.awlib.utils.FileServerExecuter.Options.TransferFile;

/**
 * Created by alex on 24.11.2016.
 */
public class FileServerExecuter extends
        AsyncTask<FileServerExecuter.Options, Void, FileServerExecuter.ConnectionFailsException> {
    private final RemoteFileServerExecuter mRemoteFileServerExecuter;
    private FTPClient mClient;
    private String mDestDirectoryName;
    private String mDirectory;
    private FTPFileFilter mFilter;
    private File mTransferFile;

    public FileServerExecuter(RemoteFileServerExecuter remoteFileServerExecuter) {
        mRemoteFileServerExecuter = remoteFileServerExecuter;
        mClient = mRemoteFileServerExecuter.getFTPClient();
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
                mClient.connect(mRemoteFileServerExecuter.getUrl(), 21);
                mClient.enterLocalPassiveMode();
                if (!mClient.login(mRemoteFileServerExecuter.getUserID(),
                        mRemoteFileServerExecuter.getUserPassword())) {
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

    protected ConnectionFailsException doInBackground(Options... params) {
        try {
            switch (params[0]) {
                case ListAllFiles:
                case ListFilesInDirectory:
                    FTPFile[] result = listFiles(mDirectory, mFilter);
                    mRemoteFileServerExecuter.setFiles(result);
                    break;
                case TransferFile:
                    transferFileToServer(mTransferFile, mDestDirectoryName);
                    break;
            }
            return null;
        } catch (ConnectionFailsException e) {
            return e;
        }
    }

    public void executeListAllFiles(FTPFileFilter filter) {
        mDirectory = "/";
        executeListFilesInDiretory(mDirectory, filter);
    }

    public void executeListFilesInDiretory(String directory, FTPFileFilter filter) {
        mDirectory = directory;
        mFilter = filter;
        execute(ListFilesInDirectory);
    }

    public void executeTransferFile(File transferFile, String destDirectoryName) {
        mTransferFile = transferFile;
        mDestDirectoryName = destDirectoryName;
        execute(TransferFile);
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
    private FTPFile[] listFiles(FTPFileFilter filter) throws ConnectionFailsException {
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
     * @throws ConnectionFailsException
     *         bei Fehlern.
     */
    private FTPFile[] listFiles(String directory, FTPFileFilter filter)
            throws ConnectionFailsException {
        connectClient();
        FTPFile[] files;
        try {
            if (filter != null) {
                files = mClient.listFiles(directory, filter);
            } else {
                files = mClient.listFiles(directory);
            }
            if (mClient != null && mClient.isConnected()) {
                mClient.logout();
                mClient.disconnect();
            }
        } catch (IOException e) {
            throw new ConnectionFailsException(mClient);
        }
        return files;
    }

    @Override
    protected void onPostExecute(ConnectionFailsException result) {
        mRemoteFileServerExecuter.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        mRemoteFileServerExecuter.onPreExecute();
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
    private boolean transferFileToServer(File file, String remoteDirectory)
            throws ConnectionFailsException {
        connectClient();
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
                if (fis != null) {
                    fis.close();
                }
                if (mClient != null && mClient.isConnected()) {
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

        public String getStatusMessage() {
            StringBuilder s = new StringBuilder();
            for (String val : getStatus()) {
                s.append(val).append(linefeed);
            }
            return s.toString();
        }
    }
}
