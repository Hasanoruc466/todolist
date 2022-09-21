package com.example.todolist;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.todolist.adapters.RVAdapter;
import com.example.todolist.databinding.FragmentListBinding;
import com.example.todolist.models.ToDo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    private FragmentListBinding binding;
    private ArrayList<ToDo> toDoArrayList = new ArrayList<>();
    private RVAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseDatabase fd;
    private DatabaseReference df;
    private TextInputEditText titleET, descET;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        auth = FirebaseAuth.getInstance();
        fd = FirebaseDatabase.getInstance();
        df = fd.getReference("todolist");
        binding.recyclerView2.setHasFixedSize(true);
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.fab.setOnClickListener(v->{
            dialog();
        });
        getList();
        return view;
    }

    private void getList(){
        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                toDoArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String user = auth.getCurrentUser().getEmail();
                    if(user.equals(ds.child("user").getValue(String.class))){
                        String key = ds.getKey();
                        String title = ds.child("title").getValue(String.class);
                        String desc = ds.child("description").getValue(String.class);
                        ToDo td = new ToDo(key, title, desc, user);
                        toDoArrayList.add(td);
                    }

                }
                adapter = new RVAdapter(requireContext(), toDoArrayList);
                binding.recyclerView2.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dialog(){
        AlertDialog.Builder adb = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.alert_dialog, null);
        titleET = view.findViewById(R.id.titleET);
        descET = view.findViewById(R.id.descET);
        adb.setTitle("Add Task");
        adb.setView(view);
        adb.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addTask();
            }
        });
        adb.show();
    }

    private void addTask(){
        String title = titleET.getText().toString();
        String desc = descET.getText().toString();
        String user = auth.getCurrentUser().getEmail();
        ToDo td = new ToDo(title, desc, user);
        df.push().setValue(td);
    }
}