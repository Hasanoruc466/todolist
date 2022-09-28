package com.example.todolist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.todolist.adapters.RVAdapter;
import com.example.todolist.databinding.FragmentListBinding;
import com.example.todolist.models.ToDo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ListFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_CAMERA_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 1;
    private FragmentListBinding binding;
    private ArrayList<ToDo> toDoArrayList = new ArrayList<>();
    private RVAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseDatabase fd;
    private DatabaseReference df;
    private TextInputEditText titleET, descET, fNameET;
    private Uri uri;
    private Bitmap bitmap;
    private ImageView imageView;
    private StorageReference sr;
    private ToDo td;
    private AlertDialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        loadDialog();
        auth = FirebaseAuth.getInstance();
        fd = FirebaseDatabase.getInstance();
        df = fd.getReference("todolist");
        sr = FirebaseStorage.getInstance().getReference("uploads");
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
                        String fileName = ds.child("fileName").getValue(String.class);
                        String fileURL = ds.child("fileURL").getValue(String.class);
                        ToDo td = new ToDo(key, title, desc, user, fileURL, fileName);
                        toDoArrayList.add(td);
                    }

                }
                adapter = new RVAdapter(requireContext(), toDoArrayList);
                binding.recyclerView2.setAdapter(adapter);
                dialog.dismiss();
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
        fNameET = view.findViewById(R.id.fNameET);
        Button buttonFile = view.findViewById(R.id.buttonFile);
        Button buttonCam = view.findViewById(R.id.buttonCam);
        imageView = view.findViewById(R.id.imageViewUpload);
        //adb.setTitle("Add Task");
        adb.setView(view);
        buttonFile.setOnClickListener(v->{
            openFileResource();
        });
        buttonCam.setOnClickListener(v->{
            openCamera();
        });
        adb.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addTask();
                loadDialog();
            }
        });
        adb.show();
    }

    private void addTask(){
        String title = titleET.getText().toString();
        String desc = descET.getText().toString();
        String user = auth.getCurrentUser().getEmail();
        td = new ToDo(title, desc, user);
        if(uri != null)
            uploadUriFile();
        else
            uploadBitmapFile();
    }

    private void uploadBitmapFile(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte bytes[] = baos.toByteArray();
        String fileName = fNameET.getText().toString() + ".jpg";
        StorageReference temp = sr.child(fileName);
        temp.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    temp.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            td.setFileName(fileName);
                            td.setFileURL(uri.toString());
                            df.push().setValue(td);
                        }
                    });
                }
                else{
                    Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getFileExtension(Uri newUri){
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(newUri));
    }

    private void uploadUriFile(){
        String fileName = fNameET.getText().toString() + "." + getFileExtension(uri);
        StorageReference temp = sr.child(fileName);
        temp.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    temp.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            td.setFileName(fileName);
                            td.setFileURL(uri.toString());
                            df.push().setValue(td);
                        }
                    });
                }
                else{
                    Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFileResource(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null){
            uri = data.getData();
            Picasso.with(getActivity()).load(uri).into(imageView);
        }
        else if(requestCode == PICK_CAMERA_REQUEST && resultCode == getActivity().RESULT_OK){
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_CAMERA_REQUEST);
            }
            else{
                Toast.makeText(requireContext(), "You can allow it in the settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, PICK_CAMERA_REQUEST);
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void loadDialog(){
        AlertDialog.Builder adb = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        adb.setView(view);
        dialog = adb.create();
        dialog.show();
    }
}