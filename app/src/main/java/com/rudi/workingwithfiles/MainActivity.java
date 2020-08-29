package com.rudi.workingwithfiles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 42;
    private EditText        mUrl;
    private Button          mDownloadBtn;
    private ImageView       mImage;
    private Button          mShowButton;
    private String          mValidUrl;
    DownloadManager         downloadManager;
    private long refid;
    ArrayList<Long> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUrl = findViewById(R.id.et_url);
        mDownloadBtn = findViewById(R.id.btn_download);
        mImage = findViewById(R.id.iv_image);
        mShowButton = findViewById(R.id.btn_show);

        mDownloadBtn.setOnClickListener(this);
        mShowButton.setOnClickListener(this);

        if (!isWritePermissionGranted()) {
            requestWritePermission();
        }


        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_download:
                String url = mUrl.getText().toString().trim();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(this, "Please enter the URL", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (!isUrlForImage(url)) {
                    Toast.makeText(this, "The given Url is not image", Toast.LENGTH_SHORT).show();
                    break;
                }
                mValidUrl = url;
                if (!isWritePermissionGranted()) {
                    requestWritePermission();
                } else {
                    downloadAndSaveFile(mValidUrl);
                }

                Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_show:
                Toast.makeText(this, "Show", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isUrlForImage(String url) {
        return url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".bmp") || url.endsWith(".jpg");
    }

    private boolean isWritePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setMessage("This permission is needed to save downloaded file to storage")
                    .setPositiveButton("I agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE) return;
        if (grantResults.length != 1) return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!TextUtils.isEmpty(mValidUrl)) {
                downloadAndSaveFile(mValidUrl);
            }
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("You can set the permissions at the phone settings")
                    .setPositiveButton("Ok", null)
                    .show();
        }
    }

    private void downloadAndSaveFile(String url) {
        Uri downloadUri = Uri.parse(url);

        String extension = url.substring(url.lastIndexOf('.') + 1);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("file." + extension);
        request.setDescription("Downloading " + url);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "file." + extension);

        refid = downloadManager.enqueue(request);
        list.add(refid);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

// remove it from our list
            list.remove(referenceId);

// if list is empty means all downloads completed
            if (list.isEmpty())
            {

// show a notification
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Working With Files")
                                .setContentText("All Download completed");


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, mBuilder.build());


            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }
}