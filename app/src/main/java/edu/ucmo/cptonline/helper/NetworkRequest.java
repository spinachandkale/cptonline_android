package edu.ucmo.cptonline.helper;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.ucmo.cptonline.datasource.Logins;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.Thread.sleep;

/**
 * Created by avina on 5/1/2017.
 */

public class NetworkRequest {
    private String URL;
    private String response;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public NetworkRequest(String url) {
        this.URL = url;
        this.response= "";
    }

    public Boolean PostLogin(Long id, String email, String name, String password) {

        Logins login = new Logins();
        login.setId(id);
        login.setEmail(email);
        login.setName(name);
        login.setPassword(password);
        login.setIsadmin(0);
        login.setIsinternshipoffice(0);
        login.setTempcode(0);
        // convert to Json
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = null;
        try {
            jsonInString = mapper.writeValueAsString(login);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonInString);
//             Send in HTTP
        Request request = new Request.Builder()
                .url("http://35.188.97.91:8760/logins")
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    setResponse("error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String ret = response.body().string().toString();
                    setResponse(ret);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    public Boolean getRequestDefault() {
        Boolean ret = Boolean.TRUE;
        Request request = new Request.Builder()
                .url(URL)
                .build();
        OkHttpClient httpclient = new OkHttpClient();
        String jsonInString;
        httpclient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setResponse("error");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                setResponse(response.body().string().toString());
            }
        });
        return ret;
    }
    public void waitForResult() {
        while(response.equals("")) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
