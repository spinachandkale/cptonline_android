package edu.ucmo.cptonline;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.ucmo.cptonline.datasource.Applications;
import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.NetworkRequest;

public class StudentNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_navigation);

        Button btnFileApplication = (Button) findViewById(R.id.btnFileApplication);
        Button btnUploadFiles = (Button) findViewById(R.id.btnUploadFiles);
        EditText status = (EditText) findViewById(R.id.nav_status);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String email = settings.getString("email", "");

        String[] emailParts = email.split("@");

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/"+emailParts[0]);
        nr.getRequestDefault();
        nr.waitForResult();
        String response = nr.getResponse();

        ObjectMapper mapper = new ObjectMapper();
        Long studentId = Long.valueOf(0);
        try {
            Students student = mapper.readValue(response, Students.class);
            studentId = student.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nr.setURL("http://35.188.97.91:8761/applications/"+studentId);
        nr.setResponse("");
        nr.getRequestDefault();
        nr.waitForResult();
        response = nr.getResponse();

        try {
            Applications application = mapper.readValue(response,Applications.class);
            status.setText(application.getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
