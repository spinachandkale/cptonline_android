package edu.ucmo.cptonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.NetworkRequest;

public class studentInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

        Button edit = (Button) findViewById(R.id.btnEditStudentInfo);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((Button)v).getText() == "Edit") {
                    makeStudentDetailsEditable();
                } else if(((Button)v).getText() == "Save"){
                    saveStudentDetails();
                    proceedToNavigationActivity();
                }
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = preferences.getString("email", null);

        String[] emailParts = email.split("@");

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/"+emailParts[0]);
        nr.getRequestDefault();
        nr.waitForResult();
        String response = nr.getResponse();

        ObjectMapper mapper = new ObjectMapper();
        Students[] students;
        try {
            students = mapper.readValue(response, Students[].class);
            if (students.length == 0) {
                displayFileApplication();
            } else {
                displayEditApplication(students[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayFileApplication() {

    }

    private void displayEditApplication(Students student) {

    }

    private void makeStudentDetailsEditable() {

    }

    private void saveStudentDetails() {

    }

    private void proceedToNavigationActivity() {
        Intent intent = new Intent(this, StudentNavigationActivity.class);
        startActivity(intent);
    }
}
