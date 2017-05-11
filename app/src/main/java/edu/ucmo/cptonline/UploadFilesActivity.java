package edu.ucmo.cptonline;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.ucmo.cptonline.datasource.Applications;
import edu.ucmo.cptonline.datasource.DriveShareRequest;
import edu.ucmo.cptonline.datasource.DriveUploadRequest;
import edu.ucmo.cptonline.datasource.Notifications;
import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.GoogleDriveHelper;
import edu.ucmo.cptonline.helper.NetworkRequest;
import edu.ucmo.cptonline.helper.UploadHelper;

public class UploadFilesActivity extends BaseActivity
        implements uploadTaskCompteletLister<Integer>,
        driveTaskCompleteListner<Boolean>  {

    private static final int REQUEST_CODE = 99;
    String filename;
    String directoryLink = "";
    Boolean scanComplete = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_files);
        super.onCreateDrawer();

        checkPermissions();

        Button btnUploadCentralDegree = (Button) findViewById(R.id.btnUploadCentralDegree);
        Button btnUploadOfferLetter = (Button) findViewById(R.id.btnUploadOfferLetter);
//        Button btnMainpage = (Button) findViewById(R.id.btnMainpage);
        final Button btnUpload = (Button) findViewById(R.id.btnUpload);

        btnUploadCentralDegree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filename = Environment.getExternalStorageDirectory() + "/cptonline/" + "CentralDegree_" + getDateTime() + ".png";
                startScan();
            }
        });

        btnUploadOfferLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filename = Environment.getExternalStorageDirectory() + "/cptonline/" + "OfferLetter_" + getDateTime() + ".png";
                startScan();
            }
        });


//        btnMainpage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                proceedToStudentNavigationPage();
//            }
//        });
//
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionUpload();
                btnUpload.setEnabled(false);
                btnUpload.setBackgroundResource(android.R.drawable.btn_default);
            }
        });
    }

    private void startScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            FileOutputStream out = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                ImageView scannedImageView = (ImageView) findViewById(R.id.scannedImage);
                scannedImageView.setImageBitmap(bitmap);
                Button upload = (Button) findViewById(R.id.btnUpload);
                upload.setEnabled(true);
                upload.setBackgroundResource(R.color.red);
                ((TextView) findViewById(R.id.upload_status)).setText("");
                File file = new File(filename);
                file.getParentFile().mkdirs();
                file.createNewFile();
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                ((TextView) findViewById(R.id.upload_status)).setText("Uploading file...");
                scanComplete = Boolean.TRUE;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void driveUpload() {
        ProgressDialog pd = new ProgressDialog(UploadFilesActivity.this);
        pd.setMessage("Uploading file");
        pd.show();
        driveTask dTask = new driveTask(this, filename, pd);
        dTask.execute((Void) null);
    }

    private void driveUploadHandleResult(Boolean result) {
        if (result && filename.contains("CentralDegree")) {
            ((TextView) findViewById(R.id.upload_status)).setText("Central degree upload successful");
        } else if (result && filename.contains("OfferLetter")) {
            ((TextView) findViewById(R.id.upload_status)).setText("Offer letter upload successful");
        } else if (!result) {
            ((TextView) findViewById(R.id.upload_status)).setText("File upload unsuccessful");
        }
    }


    private void actionUpload() {
        ArrayList<String> args = new ArrayList<>();
        args.add("http://35.188.97.91/cptuploads/postfile.php");
//        args.add("http://192.168.1.68/uploads/postfile.php");
        args.add(filename);
        args.add("image/png");

        ProgressDialog pd = new ProgressDialog(UploadFilesActivity.this);
        pd.setMessage("Saving file");
        pd.show();
        UploadHelper fileTransfer = new UploadHelper(this, pd);
        fileTransfer.execute(args);
    }

    @Override
    public void onUploadTaskComplete(Integer result) {
        if (result == 1) {
            driveUpload();
        } else {
            ((TextView) findViewById(R.id.upload_status)).setText("File upload unsuccessful");
        }
    }

    @Override
    public void onDriveTaskComplete(Boolean result) {
        if (result == Boolean.TRUE) {
            driveUploadHandleResult(result);
        }
    }

    public class driveTask extends AsyncTask<Void, Void, Boolean> {

        private String filename;
        private ProgressDialog pd;
        private String directoryLink;
        private driveTaskCompleteListner<Boolean> listner;

        public driveTask(driveTaskCompleteListner<Boolean> listner, String filename, ProgressDialog pd) {
            this.listner = listner;
            this.filename = filename;
            this.pd = pd;
            directoryLink = "";
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DriveUploadRequest request = new DriveUploadRequest();
            File file = new File(filename);
            request.setFilename(file.getName());
            request.setStudentId(getSharedPreferenceValue("studentId"));

            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8759/uploads");
            nr.postDriveUpload(request);
            nr.waitForResult();
            String response = nr.getResponse();
            if (response.equals("")) {
                return Boolean.FALSE;
            }

            directoryLink = response;
            if (!updateStudentDetails()) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            pd.dismiss();
            listner.onDriveTaskComplete(aBoolean);
        }

        private Boolean updateStudentDetails() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String studentEmail = preferences.getString("email", null);

            String[] emailParts = studentEmail.split("@");

            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/" + emailParts[0]);
            nr.getRequestDefault();
            nr.waitForResult();
            String response = nr.getResponse();

            ObjectMapper mapper = new ObjectMapper();
            Students[] students;
            Students student = null;
            try {
                students = mapper.readValue(response, Students[].class);
                if (students.length > 0) {
                    student = students[0];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (student != null) {
                if (filename.contains("OfferLetter")) {
                    student.setOfferletter(filename);
                } else if (filename.contains("CentralDegree")) {
                    student.setCentraldegree(filename);
                }
                if (student.getDirectorylink() != null &&
                        student.getDirectorylink().equals("") && !directoryLink.equals("")) {
                    student.setDirectorylink(directoryLink);
                }
                NetworkRequest nr1 = new NetworkRequest("http://35.188.97.91:8761/students/");
                nr1.putStudent(student);
                nr1.waitForResult();
                String response1 = nr1.getResponse();
                if (response1.equals("error")) {
                    return Boolean.FALSE;
                }

                if (student.getDirectorylink() != null && !student.getDirectorylink().equals("")
                        && student.getInternshipform() != null && !student.getInternshipform().equals("")
                        && student.getCentraldegree() != null && !student.getCentraldegree().equals("")
                        && student.getOfferletter() != null && !student.getOfferletter().equals("")) {

                    if (shareDriveFolder()) {
                        updateApplicationStatus(Long.toString(student.getId()));
                    } else {
                        return Boolean.FALSE;
                    }

                }
            }
            return Boolean.TRUE;

        }

        private String getSharedPreferenceValue(String key) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            return preferences.getString(key, null);
        }

        private Boolean shareDriveFolder() {
            DriveShareRequest request = new DriveShareRequest();
            request.setEmail(getNotificationsEmail("coordinator-verification-required"));
            request.setFileID(directoryLink);
            request.setStudentID(getSharedPreferenceValue("studentId"));

            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8759/uploads/share/");
            nr.postDriveShare(request);
            nr.waitForResult();
            String response = nr.getResponse();
            if (response.equals("success")) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        private void updateApplicationStatus(String studentId) {
            Applications application = getApplications(studentId);
            if (application != null) {
                application.setStatus("coordinator-verification-required");
                if (saveApplication(application)) {
//                    proceedToStudentNavigationPage();
                }
            }
        }

        private Applications getApplications(String studentId) {

            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/applications/" + studentId);
            nr.getRequestDefault();
            nr.waitForResult();
            String response = nr.getResponse();

            ObjectMapper mapper = new ObjectMapper();
            Applications[] applications;
            Applications app = null;
            try {
                applications = mapper.readValue(response, Applications[].class);
                if (applications.length > 0) {
                    app = applications[0];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return app;
        }

        private Boolean saveApplication(Applications application) {
            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/applications/");
            nr.putApplication(application);
            nr.waitForResult();
            String response = nr.getResponse();
            return !(response.equals("error"));
        }

        private String getNotificationsEmail(String status) {
            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/notifications/" + status);
            nr.getRequestDefault();
            nr.waitForResult();
            String response = nr.getResponse();

            ObjectMapper mapper = new ObjectMapper();
            Notifications notification = null;
            try {
                notification = mapper.readValue(response, Notifications.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (notification.getEmail() != null) ? notification.getEmail() : "";
        }

    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void proceedToStudentNavigationPage() {
        Intent intent = new Intent(this, StudentNavigationActivity.class);
        startActivity(intent);
    }

    private boolean checkPermissions() {
        String[] permissions = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }

}

