package com.seailz.lab.todo.model;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.seailz.lab.todo.TodoApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;

import java.util.List;
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
            return itemsRef.get().get().toObjects(TodoItem.class);
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
            return itemRef.get().get().toObject(TodoItem.class);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null; // Return null on error
        }
    }



}
