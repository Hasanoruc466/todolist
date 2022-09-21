package com.example.todolist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.todolist.databinding.FragmentRecentBinding;
import com.example.todolist.models.ToDo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RecentFragment extends Fragment {
    private FragmentRecentBinding binding;
    private SharedPreferences sp;
    private FirebaseDatabase fd;
    private DatabaseReference df;
    private String key;
    private ToDo td;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        fd = FirebaseDatabase.getInstance();
        df = fd.getReference("todolist");
        sp = getActivity().getSharedPreferences("Key", Context.MODE_PRIVATE);
        key = sp.getString("key","null");
        findTask();
        returnToList();
        return view;
    }

    private void findTask(){
        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot d:snapshot.getChildren()){
                    if(key.equals(d.getKey())){
                        td = new ToDo(
                                key,
                                d.child("title").getValue(String.class),
                                d.child("description").getValue(String.class),
                                d.child("user").getValue(String.class)
                        );
                    }
                }
                setEditTexts();
                removeTask();
                updateTask();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setEditTexts(){
        binding.titleETRec.setText(td.getTitle());
        binding.descETRec.setText(td.getDescription());
    }

    private void removeTask(){
        binding.buttonRemove.setOnClickListener(v->{
            df.child(key).removeValue();
            Navigation.findNavController(v).navigate(R.id.recTolList);
        });
    }

    private void updateTask(){
        binding.buttonUpdate.setOnClickListener(v->{
            String newTitle = binding.titleETRec.getText().toString();
            String newDesc = binding.descETRec.getText().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("title", newTitle);
            map.put("description", newDesc);
            df.child(key).updateChildren(map);
            Navigation.findNavController(v).navigate(R.id.recTolList);
        });
    }

    private void returnToList(){
        binding.imageViewBack.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.recTolList);
        });
    }
}