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
package de.aw.awlib;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import de.aw.awlib.activities.AWLibInterface;

/**
 * Erstellt und ersetzt Notifications
 */
public class AWLibNotification implements AWLibInterface {
    private static int lastNotifyID = 1;
    private final Context context;
    private String contentTitle;
    private Bundle extras;
    private boolean hasProgressBar;
    private int mNotifyID;
    private int number = NOID;
    private Class startActivity;
    private String ticker;

    /**
     * Siehe {@link AWLibNotification#AWLibNotification(Context, String)}
     *
     * @param startActivity
     *         Activity, die bei click auf Notification gestartet werden soll. Kann null sein, dann
     *         wird die AWLibMainActivity gestartet.
     */
    public AWLibNotification(@NonNull Context context, @NonNull String contentTitle,
                             Class startActivity) {
        this(context, contentTitle);
        this.startActivity = startActivity;
    }

    /**
     * @param context
     *         Context
     * @param contentTitle
     *         1. Zeile der Notification
     */
    public AWLibNotification(@NonNull Context context, @NonNull String contentTitle) {
        this.context = context;
        this.contentTitle = contentTitle;
        mNotifyID = lastNotifyID;
        lastNotifyID++;
    }

    /**
     * Cancelt die Notification
     */
    public void cancel() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        nMgr.cancel(mNotifyID);
    }

    /**
     * Erstellt Notification mit  Titel aus Konstruktor und ContentText
     *
     * @param contentText
     *         ContentText
     *
     * @return NotifyID
     */
    public int createNotification(@NonNull String contentText) {
        return createNotification(contentTitle, contentText);
    }

    /**
     * Erstellt Notification mit neuem Titel und neuen Text
     *
     * @param contentTitle
     *         ContentTitel
     * @param contentText
     *         ContentText
     *
     * @return NotifyID
     */
    public int createNotification(@NonNull String contentTitle, @NonNull String contentText) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    public Context getContext() {
        return context;
    }

    /**
     * Erstellt einen NotificationBuilder mit Ticker {@link AWLibNotification#ticker},
     * hasProgressBar und setzt (wenn gesetzt) startActivity als StartActivity.
     *
     * @return NotificationBuilder
     */
    protected NotificationCompat.Builder getNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).
                setSmallIcon(R.drawable.ic_stat_action_account).setAutoCancel(true);
        if (ticker != null) {
            mBuilder.setTicker(ticker);
        }
        if (number != NOID) {
            mBuilder.setNumber(number);
        }
        mBuilder.setProgress(0, 0, hasProgressBar);
        Intent intent;
        if (startActivity != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            intent = new Intent(context, startActivity);
            if (extras != null) {
                intent.putExtras(extras);
            }
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        return mBuilder;
    }

    public int getNotifyID() {
        return mNotifyID;
    }

    /**
     * Setzt die Notification mit einem neuen Text
     *
     * @param contentText
     *         Text
     *
     * @return NotifyID
     */
    public int replaceNotification(@NonNull String contentText) {
        return replaceNotification(contentTitle, contentText);
    }

    /**
     * Notification mit nur einer Zeile.
     *
     * @param contentTitle
     *         Title der Notification
     * @param contentText
     *         Text der Notification
     *
     * @return NotifyID
     */
    public int replaceNotification(@NonNull String contentTitle, @NonNull String contentText) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    /**
     * Ersetze Notification.
     *
     * @param contentListHeader
     *         Header der Liste.
     * @param contentListTexte
     *         Liste der NotificationTexte
     *
     * @return die NotificationID
     */
    public int replaceNotification(@NonNull String contentListHeader,
                                   @NonNull List<String> contentListTexte) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setSummaryText(contentListHeader);
        for (String s : contentListTexte) {
            inboxStyle.addLine(s);
        }
        mBuilder.setContentText(contentListHeader);
        mBuilder.setStyle(inboxStyle);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotifyID, mBuilder.build());
        return mNotifyID;
    }

    /**
     * Setzt den Ticker fuer die Notification
     *
     * @param contentTitle
     *         Titel der Notification
     */
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    /**
     * @param extras
     *         Bundle fuer die zu rufende Activity
     */
    public void setExtras(Bundle extras) {
        this.extras = extras;
    }

    /**
     * @param hasProgressBar
     *         wenn true, wird in der Notification eine Progressbar gezeigt
     */
    public void setHasProgressBar(boolean hasProgressBar) {
        this.hasProgressBar = hasProgressBar;
    }

    /**
     * @param number
     *         Number, die am Ende der Notification angezeigt werden soll, z.B. die Anzahl der
     *         importierten Umsaetze. Keine Anzeige, wenn NOID
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Setzt den Ticker fuer die Notification
     *
     * @param ticker
     *         Tickertext
     */
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public AWLibNotification startForegroundNotification(@NonNull Service service,
                                                         @NonNull String contentText) {
        NotificationCompat.Builder mBuilder = getNotification();
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        service.startForeground(mNotifyID, mBuilder.build());
        return this;
    }
}
