package com.seailz.lab.todo.model;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.seailz.lab.todo.TodoApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Data
@AllArgsConstructor
public class TodoItem {

    private String ownerId;
    private String id;
    private String content;
    private boolean completed;

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("owner_id", ownerId);
        json.put("id", id);
        json.put("content", content);
        json.put("completed", completed);
        return json;
    }

    public static TodoItem fromJson(JSONObject json) {
        String ownerId = json.getString("owner_id");
        String id = json.getString("id");
        String content = json.getString("content");
        boolean completed = json.getBoolean("completed");
        return new TodoItem(ownerId, id, content, completed);
    }

    public static TodoItem newTodo(String ownerId, String content) {
        return new TodoItem(ownerId, UUID.randomUUID().toString(), content, false);
    }

    public void save() throws ExecutionException, InterruptedException {
        // Save to Firestore
        DocumentReference colRef = TodoApp.GCP_PRIMARY_FIRESTORE
                .collection("todo-trial")
                .document(ownerId);

        CollectionReference itemsRef = colRef.collection("items");
        DocumentReference itemRef = itemsRef.document(id);

        itemRef.set(this.toJson().toMap()).get();
    }


    public static List<TodoItem> getTodos(String ownerId) {
        // Get all todo items for the owner
        DocumentReference colRef = TodoApp.GCP_PRIMARY_FIRESTORE
                .collection("todo-trial")
                .document(ownerId);

        CollectionReference itemsRef = colRef.collection("items");

        try {
            QuerySnapshot itemsSnapshot = itemsRef.get().get();
            if (itemsSnapshot.isEmpty()) {
                return List.of(); // Return an empty list if no items found
            }
            List<TodoItem> items = new ArrayList<>();
            for (QueryDocumentSnapshot queryDocumentSnapshot : itemsSnapshot) {
                TodoItem item = TodoItem.fromJson(new JSONObject(queryDocumentSnapshot.getData()));
                items.add(item);
            }
            return items;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return List.of(); // Return an empty list on error
        }
    }

    public static TodoItem getTodo(String ownerId, String id) {
        // Get a specific todo item by ID
        DocumentReference colRef = TodoApp.GCP_PRIMARY_FIRESTORE
                .collection("todo-trial")
                .document(ownerId);

        CollectionReference itemsRef = colRef.collection("items");
        DocumentReference itemRef = itemsRef.document(id);

        try {
            QuerySnapshot itemsSnapshot = itemsRef.get().get();
            if (itemsSnapshot.isEmpty()) {
                return null; // Return null if no items found
            }

            return TodoItem.fromJson(new JSONObject(itemRef.get().get().getData()));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null; // Return null on error
        }
    }



}
