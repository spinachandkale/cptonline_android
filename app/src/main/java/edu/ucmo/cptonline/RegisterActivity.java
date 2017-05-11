package edu.ucmo.cptonline;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        String passwordHash = "";
        try {
            passwordHash = md5(passwordStr);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (studentid == 0 || nameStr.equals("") || emailStr.equals("") || passwordHash.equals("")) {
            Toast.makeText(getApplicationContext(), "please enter details", Toast.LENGTH_LONG);
        } else if (emailValidate(emailStr) == false){
            Toast.makeText(getApplicationContext(), "please enter valid email", Toast.LENGTH_LONG);
        } else {
            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8760/logins");
            nr.PostLogin(studentid,emailStr,nameStr,passwordHash);
            nr.waitForResult();
            // verify response
            if (!nr.getResponse().equals("")
                    && !nr.getResponse().equals("error")
                    && verifyLoginResponse(nr.getResponse(), emailStr, nameStr)) {
                Toast.makeText(getApplicationContext(),"Registration successful", Toast.LENGTH_LONG).show();
                saveToSharedPreferences("email",emailStr);
                saveToSharedPreferences("password", passwordStr);
                proceedToLogin();
            } else {
                Toast.makeText(getApplicationContext(),"Registration unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void saveToSharedPreferences(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public Boolean emailValidate(String email) {
        String emailParts[] = email.split("@");
        if(emailParts.length != 2) {
            return Boolean.FALSE;
        }
        if (!emailParts[1].equals("ucmo.edu")) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean verifyLoginResponse(String response, String email, String name) throws IOException {
        Boolean ret = true;
        ObjectMapper mapper = new ObjectMapper();
        Logins loginObj = mapper.readValue(response, Logins.class);
        if (loginObj.getName() != null && loginObj.getName().equals(name)
                && loginObj.getEmail() != null && loginObj.getEmail().equals(email)) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

    private String md5(String input) throws NoSuchAlgorithmException {
        String result = input;
        if(input != null) {
            MessageDigest md = MessageDigest.getInstance("MD5"); //or "SHA-1"
            md.update(input.getBytes());
            BigInteger hash = new BigInteger(1, md.digest());
            result = hash.toString(16);
            while(result.length() < 32) { //40 for SHA-1
                result = "0" + result;
            }
        }
        return result;
    }

    private void proceedToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
