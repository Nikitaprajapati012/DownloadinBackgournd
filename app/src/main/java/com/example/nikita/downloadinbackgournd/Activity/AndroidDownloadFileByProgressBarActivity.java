package com.example.nikita.downloadinbackgournd.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nikita.downloadinbackgournd.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by nikita on 25/7/17.
 */

public class AndroidDownloadFileByProgressBarActivity extends Activity {
    Button btnShowProgress;
    public ImageView my_image;
    private ProgressDialog pDialog;
    //    private static String file_url = "https://api.androidhive.info/progressdialog/hive.jpg";
    private static String file_url = "https://amazingslider.com/wp-content/uploads/2012/12/dandelion.jpg";
//    private static String file_url = "https://www.w3schools.com/css/trolltunga.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnShowProgress = (Button) findViewById(R.id.btnProgressBar);
        my_image = (ImageView) findViewById(R.id.my_image);
        btnShowProgress.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DownloadFileFromURL().execute(file_url);
            }
        });
    }

    private class DownloadFileFromURL extends AsyncTask<String, Integer, Boolean> {
        int contentLength = -1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AndroidDownloadFileByProgressBarActivity.this);
            pDialog.setMessage("Downloading file. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... param) {
            boolean successful = false;
            int counter = 0;
            URL downloadURL;
            HttpURLConnection connection;
            InputStream inputStream;
            FileOutputStream fileOutputStream = null;
            File file;

            try {
                downloadURL = new URL(param[0]);
                connection = (HttpURLConnection) downloadURL.openConnection();
                contentLength = connection.getContentLength();
                inputStream = connection.getInputStream();
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsoluteFile() + "/" + Uri.parse(param[0]).getLastPathSegment());

                fileOutputStream = new FileOutputStream(file);
                Log.d("Path :- @@", file.getPath());  // Show PATH where image are store.
                int read = -1;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                    counter += read;
                    publishProgress(counter);
                }
                successful = true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return successful;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            pDialog.setProgress((int) (((double) (values[0]) / contentLength) * 100));
            pDialog.setProgress(values[0]);
        }
        
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            pDialog.dismiss();
            String imageLocation = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_PICTURES) + "/dandelion.jpg";
            Log.d("IMAGEPATH@@", "" + imageLocation);
            my_image.setImageDrawable(Drawable.createFromPath(imageLocation));
        }
    }
}