package edu.ucmo.cptonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.ucmo.cptonline.datasource.Applications;
import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.NetworkRequest;

public class StudentNavigationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_navigation);
        super.onCreateDrawer();

        Button btnFileApplication = (Button) findViewById(R.id.btnFileApplication);
        Button btnUploadFiles = (Button) findViewById(R.id.btnUploadFiles);
        TextView status = (TextView) findViewById(R.id.nav_status);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = preferences.getString("email", null);

        String[] emailParts = email.split("@");

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/"+emailParts[0]);
        nr.getRequestDefault();
        nr.waitForResult();
        String response = nr.getResponse();

        ObjectMapper mapper = new ObjectMapper();
        Long studentId = Long.valueOf(0);
        try {
            Students[] students = mapper.readValue(response, Students[].class);
            if (students != null)
                studentId = students[0].getId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nr.setURL("http://35.188.97.91:8761/applications/"+studentId);
        nr.setResponse("");
        nr.getRequestDefault();
        nr.waitForResult();
        response = nr.getResponse();

        try {
            Applications[] applications = mapper.readValue(response,Applications[].class);
            if (applications != null && applications[0].getStatus() != null)
                status.setText(applications[0].getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (studentId != 0) {
            saveToSharedPreferences("studentId", String.valueOf(studentId));
        }

        // Add action listeners for buttons

        btnFileApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToFileApplication();
            }
        });

        btnUploadFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToUploadfiles();
            }
        });
    }

    void proceedToFileApplication() {
        Intent intent = new Intent(this, studentInfoActivity.class);
        startActivity(intent);
    }

    void proceedToUploadfiles() {
        Intent intent = new Intent(this, UploadFilesActivity.class);
        startActivity(intent);
    }

    public void saveToSharedPreferences(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
