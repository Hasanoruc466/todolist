package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.todolist.databinding.FragmentRegisterBinding;
import com.example.todolist.models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private TextInputLayout emailTIL, passwordTIL;
    private Users user;
    private FirebaseAuth auth;
    private AlertDialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        emailTIL = binding.emailTILReg;
        passwordTIL = binding.passwordTILReg;
        auth = FirebaseAuth.getInstance();
        Pattern password = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&–:;'.,?$]).{6,20}$");
        Pattern emailAddress = Pattern
                .compile("[a-zA-Z0-9+._%-+]{1,256}" + "@"
                        + "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" + "(" + "."
                        + "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" + ")+");
        setEmailTIL(emailAddress);
        setPasswordTIL(password);
        setButtonClick(emailAddress, password);
        return view;
    }

    private void setEmailTIL(Pattern email){
        emailTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!email.matcher(charSequence).matches())
                {
                    emailTIL.setError("Your email should be like \"abc@d.e\".");
                    emailTIL.setErrorEnabled(true);
                } else {
                    emailTIL.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setPasswordTIL(Pattern password){
        passwordTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!password.matcher(charSequence).matches()){
                    passwordTIL.setError("* At least one digit\n* At least one lowercase letter\n" +
                            "* At least one uppercase letter\n* At least one special character (!@#&–:;'.,?$)\n* At least 6 characters");
                    passwordTIL.setErrorEnabled(true);
                }
                else
                    passwordTIL.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setButtonClick(Pattern email, Pattern pass){
        binding.buttonRegister.setOnClickListener(v -> {
            if(email.matcher( binding.emailETReg.getText()).matches() && pass.matcher(binding.passwordETReg.getText()).matches()){
                user = new Users(
                        binding.nameETReg.getText().toString(),
                        binding.surnameETReg.getText().toString(),
                        binding.emailETReg.getText().toString(),
                        binding.passwordETReg.getText().toString()
                );
                loadDialog();
                register(v);
            }
            else
                Snackbar.make(v, "Email or password is incorrect", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void register(View v){
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Snackbar.make(v, "User Created", Snackbar.LENGTH_SHORT).show();
                            Navigation.findNavController(v).navigate(R.id.regToLog);
                            dialog.dismiss();
                        }
                        else {
                            Snackbar.make(v, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void loadDialog(){
        AlertDialog.Builder adb = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        adb.setView(view);
        dialog = adb.create();
        dialog.show();
    }
}