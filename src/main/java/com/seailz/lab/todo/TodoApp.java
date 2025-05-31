package com.seailz.lab.todo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Trial task for a service team
 */
public class TodoApp {

    public static String GCP_PROJECT_ID = "new-b1e89"; // slzlab project
    public static Firestore GCP_PRIMARY_FIRESTORE;

    public static void main(String[] args) {
        new TodoApp();
    }

    public TodoApp() {
        GoogleCredentials credentials = null;

        try {
            credentials = GoogleCredentials.getApplicationDefault();
//            throw new IOException("Failed to get application default credentials");
        } catch (IOException e) {
            try {
                credentials = GoogleCredentials.fromStream(new FileInputStream("utils/gcp-creds.json"));
            } catch (IOException ex) {
                System.exit(1);
            }
        }

        FirebaseApp.initializeApp(FirebaseOptions.builder().setProjectId(GCP_PROJECT_ID).setCredentials(credentials).build());
        GCP_PRIMARY_FIRESTORE = FirestoreClient.getFirestore();
    }

}
