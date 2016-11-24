package de.aw.awlib.gv;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;

import de.aw.awlib.utils.FileServerExecuter;

/**
 * Created by alex on 24.11.2016.
 */
public class RemoteFileServerExecuter {
    private final String mUserPassword;
    private final String mUserID;
    private final String mUrl;
    private final ExecutionListener mExecutionListener;
    private FTPClient mClient;
    private FileServerExecuter.ConnectionType mConnectionType;
    private FTPFile[] mFiles;

    public RemoteFileServerExecuter(RemoteFileServer theFileServer,
                                    ExecutionListener executionListener) {
        mExecutionListener = executionListener;
        mUrl = theFileServer.getURL();
        mUserID = theFileServer.getUserID();
        mConnectionType = theFileServer.getConnectionType();
        mUserPassword = theFileServer.getUserPassword();
    }

    public FTPClient getFTPClient() {
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

    public FTPFile[] getFiles() {
        return mFiles;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getUserID() {
        return mUserID;
    }

    public String getUserPassword() {
        return mUserPassword;
    }

    public void listFilesInDirectory(String directory, FTPFileFilter filter) {
        new FileServerExecuter(this).executeListFilesInDiretory(directory, filter);
    }

    public void onPostExecute(FileServerExecuter.ConnectionFailsException result) {
        mExecutionListener.onPostExecute(result);
    }

    public void onPreExecute() {
        mExecutionListener.onPreExecute();
    }

    public void setFiles(FTPFile[] files) {
        mFiles = files;
    }

    public interface ExecutionListener {
        void onPostExecute(FileServerExecuter.ConnectionFailsException result);

        void onPreExecute();
    }
}