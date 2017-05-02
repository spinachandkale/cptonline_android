package edu.ucmo.cptonline;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.ucmo.cptonline.datasource.Logins;
import edu.ucmo.cptonline.helper.NetworkRequest;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button registerButton = (Button) findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Register();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void Register() throws IOException, InterruptedException {
        EditText id = (EditText) findViewById(R.id.reg_id);
        EditText name = (EditText) findViewById(R.id.reg_fullname);
        EditText email = (EditText) findViewById(R.id.reg_email);
        EditText password = (EditText) findViewById(R.id.reg_password);

        Long studentid = Long.parseLong(id.getText().toString());
        String nameStr = name.getText().toString();
        String emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();

        if (studentid == 0 || nameStr.equals("") || emailStr.equals("") || passwordStr.equals("")) {
            Toast.makeText(getApplicationContext(), "please enter details", Toast.LENGTH_LONG);
        } else if (emailValidate(emailStr) == false){
            Toast.makeText(getApplicationContext(), "please enter valid email", Toast.LENGTH_LONG);
        } else {
            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8760/logins");
            nr.PostLogin(studentid,emailStr,nameStr,passwordStr);
            nr.waitForResult();
            // verify response
            if (!nr.getResponse().equals("")
                    && !nr.getResponse().equals("error")
                    && verifyLoginResponse(nr.getResponse(), emailStr, nameStr)) {
                Toast.makeText(getApplicationContext(),"Registration successful", Toast.LENGTH_LONG).show();
                saveToSharedPreferences("email",emailStr);
                saveToSharedPreferences("password", passwordStr);
            } else {
                Toast.makeText(getApplicationContext(),"Registration unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void saveToSharedPreferences(String key, String value) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public Boolean emailValidate(String email) {
        return email.contains("@");
    }

    public Boolean verifyLoginResponse(String response, String email, String name) throws IOException {
        Boolean ret = true;
        ObjectMapper mapper = new ObjectMapper();
        Logins loginObj = mapper.readValue(response, Logins.class);
        if (loginObj.getName().equals(name) && loginObj.getEmail().equals(email)) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

}
