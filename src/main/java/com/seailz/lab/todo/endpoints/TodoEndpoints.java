package com.seailz.lab.todo.endpoints;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.seailz.lab.todo.TodoApp;
import com.seailz.lab.todo.model.TodoItem;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@Component
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TodoEndpoints {

    @GetMapping("/todo")
    public ResponseEntity<String> getTodo(HttpServletRequest request) {
        FirebaseToken token = authorize(request);
        if (token == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<TodoItem> todos = TodoItem.getTodos(token.getUid());

        if (todos == null) {
            return ResponseEntity.status(500).body("Error retrieving todos");
        }

        JSONArray arr = new JSONArray();
        for (TodoItem todo : todos) {
            arr.put(todo.toJson());
        }

        return ResponseEntity.ok(arr.toString());
    }

    @PostMapping("/todo")
    public ResponseEntity<String> addTodo(HttpServletRequest request, @RequestBody String reqBody) throws ExecutionException, InterruptedException {
        FirebaseToken token = authorize(request);
        if (token == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (reqBody == null || reqBody.isEmpty()) {
            return ResponseEntity.status(400).body("Invalid request body");
        }

        JSONObject json;
        try {
            json = new JSONObject(reqBody);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Invalid JSON format");
        }

        TodoItem todoItem = TodoItem.newTodo(token.getUid(), json.getString("content"));
        todoItem.save();

        return ResponseEntity.ok(todoItem.toJson().toString());
    }

    @PatchMapping("/todo/{id}")
    public ResponseEntity<String> updateTodo(HttpServletRequest request, @PathVariable("id") String id, @RequestBody String reqBody) throws ExecutionException, InterruptedException {
        FirebaseToken token = authorize(request);
        if (token == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (reqBody == null || reqBody.isEmpty()) {
            return ResponseEntity.status(400).body("Invalid request body");
        }

        JSONObject json;
        try {
            json = new JSONObject(reqBody);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Invalid JSON format");
        }

        // Only completed & content fields are allowed to be updated
        if (!json.has("completed") && !json.has("content")) {
            return ResponseEntity.status(400).body("Invalid fields to update");
        }

        TodoItem todoItem = TodoItem.getTodo(token.getUid(), id);
        if (todoItem == null) {
            return ResponseEntity.status(404).body("Todo item not found");
        }

        if (json.has("completed")) {
            todoItem.setCompleted(json.getBoolean("completed"));
        }

        if (json.has("content")) {
            todoItem.setContent(json.getString("content"));
        }

        todoItem.save();

        return ResponseEntity.ok(todoItem.toJson().toString());
    }


    private FirebaseToken authorize(HttpServletRequest request) {
        if (request.getHeader("Authorization") == null) return null;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        try {
            return firebaseAuth.verifyIdToken(request.getHeader("Authorization"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
