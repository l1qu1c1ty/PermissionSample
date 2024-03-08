package com.melihcandemir.permissionsample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_SETTING = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        setContentView(R.layout.activity_main);

        // Android 12 ve sonrası için All Files Access izni kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAllFilesAccessPermission();
        } else {
            // Android 11 ve altı için depolama izni kontrolü
            checkStoragePermission();
        }
    }

    private void checkAllFilesAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // İzin zaten verilmiş, işlemlere devam et
                Log.d("Permission", "All Files Access permission already granted");
                performOperations();
            } else {
                requestAllFilesAccessPermission();
            }
        }
    }

    private void requestAllFilesAccessPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("All Files Access Permission Required");
        builder.setMessage("The application needs All Files Access permission to function properly. " +
                "Please grant the permission in the upcoming dialog.");

        builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
            }
        });

        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // İzin zaten verilmiş, işlemlere devam et
            Log.d("Permission", "Storage permission already granted");
            performOperations();
        } else {
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_SETTING);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verilmiş, işlemlere devam et
                Log.d("Permission", "Permission granted after request");
                performOperations();
            } else {
                // İzin reddedilmiş, kullanıcıyı bilgilendir ve uygulamayı kapat
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("The application cannot run without the required permission. Please grant the permission in the app settings.");

        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAppSettings();
            }
        });

        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            // İzin talep sonuçları kontrol edilir
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 ve sonrası için All Files Access izni kontrolü
                checkAllFilesAccessPermission();
            } else {
                // Android 11 ve altı için depolama izni kontrolü
                checkStoragePermission();
            }
        }
    }

    private void performOperations() {
        // İzinler alındıktan sonra yapılacak işlemler burada gerçekleştirilir
        Toast.makeText(this, "All required permissions granted. Performing operations...", Toast.LENGTH_SHORT).show();
    }
}