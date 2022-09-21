package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.todolist.databinding.ActivityToDoListBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ToDoListActivity extends AppCompatActivity {
    private ActivityToDoListBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityToDoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        logout();
    }

    private void logout(){
        binding.imageView.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ToDoListActivity.this, MainActivity.class));
        });
    }
}