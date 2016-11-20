package de.aw.awlib;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Haelt daten fuer einen externen Server
 */
public class Serverdaten implements Parcelable {
    public static final Parcelable.Creator<Serverdaten> CREATOR =
            new Parcelable.Creator<Serverdaten>() {
                @Override
                public Serverdaten createFromParcel(Parcel source) {
                    return new Serverdaten(source);
                }

                @Override
                public Serverdaten[] newArray(int size) {
                    return new Serverdaten[size];
                }
            };
    private String mURL;
    private String mUserID;
    private String mUserPassword;

    /**
     * Erstellt neue Serverzugangsdaten
     *
     * @param url
     *         url der Servers
     * @param userID
     *         UserID
     * @param password
     *         Password
     */
    public Serverdaten(String url, String userID, String password) {
        mURL = url;
        mUserID = userID;
        mUserPassword = password;
    }

    public Serverdaten() {
    }

    protected Serverdaten(Parcel in) {
        this.mURL = in.readString();
        this.mUserID = in.readString();
        this.mUserPassword = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getURL() {
        return mURL;
    }

    public String getUserID() {
        return mUserID;
    }

    public String getUserPassword() {
        return mUserPassword;
    }

    public void setURL(String mURL) {
        this.mURL = mURL;
    }

    public void setUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public void setUserPassword(String mUserPassword) {
        this.mUserPassword = mUserPassword;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mURL);
        dest.writeString(this.mUserID);
        dest.writeString(this.mUserPassword);
    }
}
