package de.aw.awlib.utils;

import android.support.annotation.Nullable;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;

import de.aw.awlib.gv.RemoteFileServer;
import de.aw.awlib.utils.RemoteFileServerHandler.ExecutionListener;

/**
 * Adapter fuer den {link {@link RemoteFileServerHandler}}
 */
public class RemoteFileServerAdapter {
    private final String mUserPassword;
    private final String mUserID;
    private final String mUrl;
    private final ExecutionListener mExecutionListener;
    private String mBackupDirectory;
    private FTPClient mClient;
    private RemoteFileServerHandler.ConnectionType mConnectionType;
    private FTPFile[] mFiles;

    /**
     * @param theFileServer
     *         FileServer, der benutzt werden soll
     * @param executionListener
     *         Listener fuer Ergebnisse siehe {@link ExecutionListener}
     */
    public RemoteFileServerAdapter(RemoteFileServer theFileServer,
                                   ExecutionListener executionListener) {
        mExecutionListener = executionListener;
        mUrl = theFileServer.getURL();
        mUserID = theFileServer.getUserID();
        mConnectionType = theFileServer.getConnectionType();
        mUserPassword = theFileServer.getUserPassword();
        mBackupDirectory = theFileServer.getBackupDirectory();
    }

    /**
     * @return Liefert das BackupDirector des Servers
     */
    String getBackupDirectory() {
        return mBackupDirectory;
    }

    /**
     * @return Liefert in Abhaengigkeit des {@link de.aw.awlib.utils.RemoteFileServerHandler.ConnectionType}
     * einen FTPClient zurueck.
     */
    FTPClient getFTPClient() {
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
     * Wenn {@link RemoteFileServerAdapter#listFilesInDirectory(String, FTPFileFilter)} gerufen
     * wurde, kann der {@link ExecutionListener} in {@link ExecutionListener#onPostExecute(RemoteFileServerHandler.ConnectionFailsException)}
     * die ermittelten Files hier abholen
     *
     * @return Liste der Files auf dem Server.
     */
    public FTPFile[] getFiles() {
        return mFiles;
    }

    /**
     * @return Uri des FileServers
     */
    String getUrl() {
        return mUrl;
    }

    /**
     * @return UserID fuer den Fileserveer
     */
    String getUserID() {
        return mUserID;
    }

    /**
     * @return Password fuer den User des Fileservers
     */
    String getUserPassword() {
        return mUserPassword;
    }

    /**
     * Listet alle Files auf dem Fileserver im directory
     *
     * @param directory
     *         Verzeichnis auf dem Server, zu em die Files ermittelt werden sollen.
     * @param filter
     *         FileFilter, der benutzt werden soll. Kann null sein
     */
    public void listFilesInDirectory(String directory, @Nullable FTPFileFilter filter) {
        new RemoteFileServerHandler(this, mExecutionListener)
                .listFilesInDiretory(directory, filter);
    }

    /**
     * Hier setzt der Handler die ermittelten Files, wenn die Transaktion beendet wurde.
     *
     * @param files
     *         Liste der Files, die gelesen wurden.
     */
    void setFiles(FTPFile[] files) {
        mFiles = files;
    }
}