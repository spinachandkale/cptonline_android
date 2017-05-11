package edu.ucmo.cptonline.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.ucmo.cptonline.uploadTaskCompteletLister;

/**
 * Created by avina on 5/3/2017.
 */

public class UploadHelper extends AsyncTask<ArrayList<String>, Void, Integer> {

    private int taskResult = -1;

    ProgressDialog pd;
    uploadTaskCompteletLister<Integer> listner;

    public UploadHelper(uploadTaskCompteletLister<Integer> listner, ProgressDialog pd) {
        this.listner = listner;
        this.pd = pd;
    }

    @Override
    protected Integer doInBackground(ArrayList<String>... params) {
        String CrLf = "\r\n";
        String Url = params[0].get(0);
        String filename = params[0].get(1);
        String contentType = params[0].get(2);

        File file = new File(filename);
        String uploadFilename = file.getName();

        Integer ret = -1;
        URLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;

        try {
            URL url = new URL(Url);
            System.out.println("url:" + url);
            conn = url.openConnection();
            conn.setDoOutput(true);

            String postData = "";

            InputStream imgIs = new FileInputStream(filename);
            byte[] imgData = new byte[imgIs.available()];
            imgIs.read(imgData);

            String message1 = "";
            message1 += "-----------------------------4664151417711" + CrLf;
            message1 += "Content-Disposition: form-data; name=\"uploadedfile\"; filename="
                    + uploadFilename + CrLf;
            message1 += "Content-Type: " + contentType + CrLf;
            message1 += CrLf;

            // the image is sent between the messages in the multipart message.

            String message2 = "";
            message2 += CrLf + "-----------------------------4664151417711--"
                    + CrLf;

            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=---------------------------4664151417711");
            // might not need to specify the content-length when sending chunked
            // data.
            conn.setRequestProperty("Content-Length", String.valueOf((message1
                    .length() + message2.length() + imgData.length)));

            os = conn.getOutputStream();

            os.write(message1.getBytes());

            // SEND THE IMAGE
            int index = 0;
            int size = 1024;
            do {
                if ((index + size) > imgData.length) {
                    size = imgData.length - index;
                }
                os.write(imgData, index, size);
                index += size;
            } while (index < imgData.length);

            os.write(message2.getBytes());
            os.flush();

            is = conn.getInputStream();

            char buff = 512;
            int len;
            byte[] data = new byte[buff];
            do {
                len = is.read(data);
            } while (len > 0);

        } catch (Exception e) {
            e.printStackTrace();
            ret = 0;
        } finally {
            try {
                os.close();
                is.close();
                ret = 1;
            } catch (Exception e) {
                e.printStackTrace();
                ret = 0;
            }
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Integer result) {
        pd.dismiss();
        taskResult = (result == 1) ? 1 : 0;
        listner.onUploadTaskComplete(result);
    }

    public Integer getTaskResult() {
        return taskResult;
    }
}


    //    private final String CrLf = "\r\n";
//    private String Url;
//    private String filename;
//    private String uploadFilename;
//    private String contentType;
//
//    public UploadHelper(String url, String filename, String contentType) {
//        this.Url = url;
//        this.filename = filename;
//        File f = new File(filename);
//        uploadFilename = f.getName();
//        this.contentType = contentType;
//    }
//
//    public Boolean httpConn() {
//        Boolean ret = Boolean.FALSE;
//        URLConnection conn = null;
//        OutputStream os = null;
//        InputStream is = null;
//
//        try {
//            URL url = new URL(Url);
//            System.out.println("url:" + url);
//            conn = url.openConnection();
//            conn.setDoOutput(true);
//
//            String postData = "";
//
//            InputStream imgIs = new FileInputStream(filename);
//            byte[] imgData = new byte[imgIs.available()];
//            imgIs.read(imgData);
//
//            String message1 = "";
//            message1 += "-----------------------------4664151417711" + CrLf;
//            message1 += "Content-Disposition: form-data; name=\"uploadedfile\"; filename="
//                    + uploadFilename + CrLf;
//            message1 += "Content-Type: " + contentType + CrLf;
//            message1 += CrLf;
//
//            // the image is sent between the messages in the multipart message.
//
//            String message2 = "";
//            message2 += CrLf + "-----------------------------4664151417711--"
//                    + CrLf;
//
//            conn.setRequestProperty("Content-Type",
//                    "multipart/form-data; boundary=---------------------------4664151417711");
//            // might not need to specify the content-length when sending chunked
//            // data.
//            conn.setRequestProperty("Content-Length", String.valueOf((message1
//                    .length() + message2.length() + imgData.length)));
//
//            os = conn.getOutputStream();
//
//            os.write(message1.getBytes());
//
//            // SEND THE IMAGE
//            int index = 0;
//            int size = 1024;
//            do {
//                if ((index + size) > imgData.length) {
//                    size = imgData.length - index;
//                }
//                os.write(imgData, index, size);
//                index += size;
//            } while (index < imgData.length);
//
//            os.write(message2.getBytes());
//            os.flush();
//
//            is = conn.getInputStream();
//
//            char buff = 512;
//            int len;
//            byte[] data = new byte[buff];
//            do {
//                len = is.read(data);
//            } while (len > 0);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                os.close();
//                ret = Boolean.TRUE;
//            } catch (Exception e) {
//            }
//            try {
//                is.close();
//            } catch (Exception e) {
//            }
//        }
//
//        return ret;
//    }
