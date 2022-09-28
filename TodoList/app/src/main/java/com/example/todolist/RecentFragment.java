package com.example.todolist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.todolist.databinding.FragmentRecentBinding;
import com.example.todolist.models.ToDo;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class RecentFragment extends Fragment {
    private FragmentRecentBinding binding;
    private SharedPreferences sp;
    private FirebaseDatabase fd;
    private DatabaseReference df;
    private StorageReference sr;
    private String key;
    private ToDo td;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        fd = FirebaseDatabase.getInstance();
        df = fd.getReference("todolist");
        sr = FirebaseStorage.getInstance().getReference("uploads");
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
                                d.child("user").getValue(String.class),
                                d.child("fileURL").getValue(String.class),
                                d.child("fileName").getValue(String.class)
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
        Picasso.with(requireContext()).load(td.getFileURL()).into(binding.imageViewFileR);

    }

    private void removeTask(){
        binding.buttonRemove.setOnClickListener(v->{
            StorageReference temp = sr.child(td.getFileName());
            df.child(key).removeValue();
            temp.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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