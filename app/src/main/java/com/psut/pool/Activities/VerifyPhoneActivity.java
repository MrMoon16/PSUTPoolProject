package com.psut.pool.Activities;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.psut.pool.R;
import com.psut.pool.Shared.Authentication;
import com.psut.pool.Shared.Layout;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.psut.pool.Shared.Constants.INTENT_PHONE_NUMBER_KEY;
import static com.psut.pool.Shared.Constants.INVALID_CODE;
import static com.psut.pool.Shared.Constants.LIMIT_EXCEEDED;
import static com.psut.pool.Shared.Constants.WENT_WRONG;

public class VerifyPhoneActivity extends AppCompatActivity implements Layout {

    //Global Variables and Objects:
    private EditText txtCode;
    private Button btnConfirm;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthCredential authCredential;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String verificationID, phoneNumber, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        Objects.requireNonNull(getSupportActionBar()).hide();

        layoutComponents();

        //Objects:
        firebaseAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra(INTENT_PHONE_NUMBER_KEY);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            //Checking if user already exists
            Authentication.isVerified(uid, this);
        } else {
            btnConfirm.setVisibility(View.VISIBLE);
        }

        //Sign In:
        startPhoneNumberVerification(phoneNumber);

        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnConfirm) {
            if (txtCode.getText().toString().isEmpty()) {
                txtCode.setError(INVALID_CODE);
            } else {
                verifyPhoneNumberWithCode(verificationID, txtCode.getText().toString());
            }
        }
    }

    private void verifyPhoneNumberWithCode(String s, String code) {
        try {
            PhoneAuthCredential authCredential = PhoneAuthProvider.getCredential(s, code);
            signInWithPhoneAuthCredential(authCredential);
        } catch (Exception e) {
            if(e instanceof NetworkErrorException){
                Toast.makeText(this, "Check Your Internet Connnection", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, WENT_WRONG, Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
    }

    private void verificationCallbacks() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();
                authCredential = phoneAuthCredential;
                txtCode.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneAuthCredential(authCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(VerifyPhoneActivity.this, LIMIT_EXCEEDED, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(VerifyPhoneActivity.this, WENT_WRONG, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
                resendingToken = forceResendingToken;
            }
        };
    }

    private void startPhoneNumberVerification(String s) {
        verificationCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+962" + s,
                60,
                TimeUnit.SECONDS,
                this,
                callbacks
        );
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.putExtra(INTENT_PHONE_NUMBER_KEY, phoneNumber);
                Toast.makeText(this, "Your Number is " + phoneNumber, Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    txtCode.setError(INVALID_CODE);
                } else {
                    Objects.requireNonNull(task.getException()).printStackTrace();
                }
            }
        });
    }

    @Override
    public void layoutComponents() {
        txtCode = findViewById(R.id.txtCodeVerfiy);
        btnConfirm = findViewById(R.id.btnConfirmVerfiy);
    }

    @Override
    public void getLayoutComponents() {

    }

    @Override
    public void onClickLayout() {

    }
}
