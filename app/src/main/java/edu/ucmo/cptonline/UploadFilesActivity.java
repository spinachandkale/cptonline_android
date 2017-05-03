package edu.ucmo.cptonline;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.ucmo.cptonline.datasource.Applications;
import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.GoogleDriveHelper;
import edu.ucmo.cptonline.helper.NetworkRequest;

public class UploadFilesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    String filename;
    String directoryLink = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_files);

        Button btnUploadCentralDegree = (Button) findViewById(R.id.btnUploadCentralDegree);
        Button btnUploadOfferLetter = (Button) findViewById(R.id.btnUploadOfferLetter);
        Button btnMainpage = (Button) findViewById(R.id.btnMainpage);

        btnUploadCentralDegree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filename = "CentralDegree_" + getDateTime() + ".png";
                startScan();
            }
        });

        btnUploadOfferLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filename = "OfferLetter_" + getDateTime() + ".png";
                startScan();
            }
        });

        btnMainpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToStudentNavigationPage();
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
                out = new FileOutputStream(filename);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                    uploadFile();
                    if(filename.contains("CentralDegree")) {
                        ((TextView)findViewById(R.id.upload_CentralDegree_status)).setText("Central degree upload successful");
                    } else if(filename.contains("OfferLetter")) {
                        ((TextView)findViewById(R.id.upload_offerletter_status)).setText("Offer letter upload successful");
                    }
                    updateStudentDetails();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadFile() {
        GoogleDriveHelper driveHelper = new GoogleDriveHelper();
        try {
            driveHelper.uploadFile(filename,getSharedPreferenceValue("studentId"));
            directoryLink = driveHelper.getDirectoryLink();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String getSharedPreferenceValue(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getString(key, null);
    }

    private void updateStudentDetails() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String studentEmail= preferences.getString("email", null);

        String[] emailParts = studentEmail.split("@");

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/"+emailParts[0]);
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
            if (student.getDirectorylink().equals("") && !directoryLink.equals("")) {
                student.setDirectorylink(directoryLink);
            }
            NetworkRequest nr1 = new NetworkRequest("http://35.188.97.91:8761/students");
            nr1.putStudent(student);
            nr1.waitForResult();
            String response1 = nr.getResponse();
            if(response1.equals("error")){
                Toast.makeText(this, "Unable to save student details", Toast.LENGTH_SHORT).show();
            }

            if(!student.getDirectorylink().equals("")
                && !student.getInternshipform().equals("")
                && !student.getCentraldegree().equals("")
                && !student.getOfferletter().equals("")) {
                updateApplicationStatus(Long.toString(student.getId()));
            }
        }

    }

    private void updateApplicationStatus(String studentId) {
        Applications application = getApplications(studentId);
        if(application != null) {
            application.setStatus("coordinator-verification-required");
            if (saveApplication(application)) {
                Toast.makeText(this, "Successfully filed application", Toast.LENGTH_SHORT).show();
                proceedToStudentNavigationPage();
            }
        }
    }

    private Applications getApplications(String studentId) {

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/applications/"+studentId);
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
        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/applications");
        nr.putApplication(application);
        nr.waitForResult();
        String response = nr.getResponse();
        return !(response.equals("error"));
    }

    private void proceedToStudentNavigationPage() {
        Intent intent = new Intent(this, StudentNavigationActivity.class);
        startActivity(intent);
    }

}
