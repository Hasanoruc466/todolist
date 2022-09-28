package com.example.todolist.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.ListFragmentDirections;
import com.example.todolist.R;
import com.example.todolist.models.ToDo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVCardView> {
    private Context context;
    private List<ToDo> toDoList;

    public RVAdapter(Context context, List<ToDo> toDoList) {
        this.context = context;
        this.toDoList = toDoList;
    }

    @NonNull
    @Override
    public RVCardView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.card_view, parent, false);
        return new RVCardView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVCardView holder, int position) {
        holder.textViewTitle.setText(toDoList.get(position).getTitle());
        holder.textViewDesc.setText(toDoList.get(position).getDescription());
        holder.buttonComplete.setOnClickListener(v->{
            deleteList(position);
        });
        holder.cardView.setOnClickListener(v->{
            SharedPreferences sp = context.getSharedPreferences("Key", Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putString("key", toDoList.get(position).getKey());
            e.commit();
            Navigation.findNavController(v).navigate(R.id.listToRec);
        });
    }

    private void deleteList(int i){
        String key = toDoList.get(i).getKey();
        String fileName = toDoList.get(i).getFileName();
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference df = fd.getReference("todolist");
        StorageReference sr = FirebaseStorage.getInstance().getReference("uploads");
        sr.child(fileName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        df.child(key).removeValue();
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    protected class RVCardView extends RecyclerView.ViewHolder{
        private TextView textViewTitle, textViewDesc;
        private Button buttonComplete;
        private CardView cardView;
        public RVCardView(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
            cardView = itemView.findViewById(R.id.cardView);
            buttonComplete = itemView.findViewById(R.id.buttonComplete);
        }
    }
}
