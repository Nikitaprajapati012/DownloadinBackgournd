package com.example.nikita.downloadinbackgournd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nikita on 25/7/17.
 */

public class DownloadService extends Service {

    SharedPreferences preferences;

    private static final String DOCUMENT_VIEW_STATE_PREFERENCES = "DjvuDocumentViewState";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private NotificationManager mNM;
    String downloadUrl;
    public static boolean serviceState = false;
    private String fileName = "audio.wav";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            downloadFile();
            showNotification("notification_catalog_downloaded", "VVS");
            stopSelf(msg.arg1);
        }
    }

    private void showNotification(String message, String title) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = message;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.mipmap.ic_launcher, "vvs",
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(this, HomeScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this.getBaseContext(), 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, title,text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.app_name, notification);
    }

    private void downloadFile() {
        downloadFile(this.downloadUrl, fileName);
    }

    @Override
    public void onCreate() {
        serviceState = true;
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        HandlerThread thread = new HandlerThread("ServiceStartArguments", 1);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SERVICE-ONCOMMAND", "onStartCommand");

        Bundle extra = intent.getExtras();
        if (extra != null) {
            String downloadUrl = extra.getString("downloadUrl");
            Log.d("URL", downloadUrl);

            this.downloadUrl = downloadUrl;
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public void onDestroy() {

        Log.d("SERVICE-DESTROY", "DESTORY");
        serviceState = false;
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
    public void downloadFile(String fileURL, String fileName) {

        StatFs stat_fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double avail_sd_space = (double) stat_fs.getAvailableBlocks() * (double) stat_fs.getBlockSize();
        //double GB_Available = (avail_sd_space / 1073741824);
        double MB_Available = (avail_sd_space / 10485783);
        //System.out.println("Available MB : " + MB_Available);
        Log.d("MB", "" + MB_Available);
        try {
            File root = new File(Environment.getExternalStorageDirectory() + "/vvveksperten");
            if (root.exists() && root.isDirectory()) {

            } else {
                root.mkdir();
            }
            Log.d("CURRENT PATH", root.getPath());
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            int fileSize = c.getContentLength() / 1048576;
            Log.d("FILESIZE", "" + fileSize);
            if (MB_Available <= fileSize) {
                this.showNotification(getResources().getString(R.string.notification_no_memory), getResources().getString(R.string.notification_error));
                c.disconnect();
                return;
            }

            FileOutputStream f = new FileOutputStream(new File(root.getPath(), fileName));

            InputStream in = c.getInputStream();


            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            File file = new File(root.getAbsolutePath() + "/" + "some.pdf");
            if (file.exists()) {
                file.delete();
                Log.d("FILE-DELETE", "YES");
            } else {
                Log.d("FILE-DELETE", "NO");
            }
            File from = new File(root.getAbsolutePath() + "/" + fileName);
            File to = new File(root.getAbsolutePath() + "/" + "some.pdf");


        } catch (Exception e) {
            Log.d("Downloader", e.getMessage());

        }
    }
}
