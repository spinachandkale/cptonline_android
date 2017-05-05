package edu.ucmo.cptonline.helper;

import android.os.Environment;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by avina on 5/3/2017.
 */

public class GoogleDriveHelper {

    private InputStream client_secret;

    public GoogleDriveHelper(InputStream in) {
        try {
            DATA_STORE_DIR = Environment.getExternalStorageDirectory()
                    + "/cptonline/" + "drive-java-quickstart";
            java.io.File file = new java.io.File(DATA_STORE_DIR);
            file.getParentFile().mkdirs();
            file.createNewFile();
            DATA_STORE_FACTORY = new FileDataStoreFactory(file);
            JSON_FACTORY = JacksonFactory.getDefaultInstance();
            SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY);
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.client_secret = in;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Directory to store user credentials for this application. */
//    private static final java.io.File DATA_STORE_DIR = new java.io.File(
//            System.getProperty("user.home"), ".credentials/drive-java-quickstart");

    private String DATA_STORE_DIR;


    private FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private JsonFactory JSON_FACTORY;

    /** Global instance of the HTTP transport. */
    private HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private List<String> SCOPES;
    private Drive service;

    private String directoryLink;

    public String getDirectoryLink() {
        return directoryLink;
    }

    public void setDirectoryLink(String directoryLink) {
        this.directoryLink = directoryLink;
    }

    private Credential authorize() throws IOException {
        // Load client secrets.

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(client_secret));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }


    private Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("cptonline")
                .build();
    }

    public void uploadFile(String filename, String studentId) throws IOException {
        // Build a new authorized API client service.

        String folderID = "";

        service = getDriveService();

        if (service == null) return;

        FileList result = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder'")
                .setQ("'0B46iJvBr8M_7cGR6c2ZZMVBjQTQ' in parents")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
        } else {
            for (File file : files) {
                if (file.getName().equals(studentId)) {
                    folderID = file.getId();
                    directoryLink = file.getWebViewLink();
                }
            }
        }
        if (folderID.equals("")) {
            folderID = createFolder(studentId);
        }

        insertFile(folderID, filename);
    }

    private String createFolder(String studentId) {
        File fileMetadata = new File();
        fileMetadata.setName("Invoices");
        fileMetadata.setParents(Collections.singletonList("0B46iJvBr8M_7cGR6c2ZZMVBjQTQ"));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = null;
        try {
            file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();

            directoryLink = file.getWebViewLink();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getId();
    }

    private void insertFile(String folderID, String filename) {
        File fileMetadata = new File();
        fileMetadata.setName(filename);
        fileMetadata.setParents(Collections.singletonList(folderID));
        java.io.File filePath = new java.io.File(filename);
        FileContent mediaContent = new FileContent("", filePath);
        try {
            service.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
