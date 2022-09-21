package com.example.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.todolist.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private TextInputLayout emailTIL, passwordTIL;
    private FirebaseAuth auth;
    private TextInputEditText emailET;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        emailTIL = binding.emailTIL;
        passwordTIL = binding.passwordTIL;
        auth = FirebaseAuth.getInstance();
        Pattern password = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&–:;'.,?$]).{6,20}$");
        Pattern emailAddress = Pattern
                .compile("[a-zA-Z0-9+._%-+]{1,256}" + "@"
                        + "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" + "(" + "."
                        + "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" + ")+");
        setEmailTIL(emailAddress);
        setPasswordTIL(password);
        login(emailAddress, password);
        logToReg();
        resetPassword(emailAddress);
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
            /*
             */
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

    private void login(Pattern email, Pattern pass){
        binding.buttonLogin.setOnClickListener(v->{
            if(email.matcher( binding.emailET.getText()).matches()
                    && pass.matcher(binding.passwordET.getText()).matches()){
                auth.signInWithEmailAndPassword(binding.emailET.getText().toString(), binding.passwordET.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Snackbar.make(v, "Login successful", Snackbar.LENGTH_SHORT).show();
                                    startActivity(new Intent(getActivity(), ToDoListActivity.class));
                                    //Navigation.findNavController(v).navigate(R.id.loginToReg);
                                }
                                else
                                    Snackbar.make(v, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });


    }

    private void logToReg(){
        binding.textViewReg.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(R.id.loginToReg);
        });
    }

    private void dialog(Pattern email){
        AlertDialog.Builder adb = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.forgot_pass_dialog, null);
        emailET = view.findViewById(R.id.emailETForgot);
        adb.setTitle("Reset Password");
        adb.setMessage("Please enter your email address. \nWe'll send you an email to reset your password.");
        adb.setView(view);

        adb.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(email.matcher(emailET.getText().toString()).matches())
                    sendMail();
            }
        });
        adb.show();
    }

    private void sendMail() {
        auth.sendPasswordResetEmail(emailET.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Toast.makeText(requireContext(), "Email sent successfully to reset your password, \ndon't forget to check your spam folder", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword(Pattern email){
        binding.textViewForgot.setOnClickListener(v->{
            dialog(email);
        });
    }
}