package de.aw.awlib.fragments;

import android.os.Bundle;

import org.apache.commons.net.ftp.FTPSClient;

import de.aw.awlib.Serverdaten;

/**
 * Created by alex on 20.11.2016.
 */
public class AWLibRemoteFileChooser extends AWLibFileChooser {
    private FTPSClient mServer;

    public static AWLibRemoteFileChooser newInstance(Serverdaten serverdaten) {
        Bundle args = new Bundle();
        args.putParcelable(SERVERDATEN, serverdaten);
        args.putString(DIRECTORYNAME, "/");
        AWLibRemoteFileChooser fragment = new AWLibRemoteFileChooser();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serverdaten mServerdaten = args.getParcelable(SERVERDATEN);
        if (mServerdaten != null) {
            mServer = new FTPSClient();
        }
    }
}
