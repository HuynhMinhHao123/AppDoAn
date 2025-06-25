package com.example.appdoan.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdoan.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    private static final int MAX_REQUESTS_PER_HOUR = 10;
    private static final String PREF_NAME = "ResetPrefs";
    private static final String KEY_COUNT = "reset_count";
    private static final String KEY_TIMESTAMP = "reset_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        progressBar = findViewById(R.id.progressBar);

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!canSendResetEmail()) {
                Toast.makeText(this, "Bạn chỉ được yêu cầu tối đa 3 lần mỗi giờ", Toast.LENGTH_LONG).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Mã xác minh đã được gửi.\nCó hiệu lực trong 5 phút.\nHãy đặt mật khẩu mới ≥ 8 ký tự gồm chữ hoa, thường và số.",
                                Toast.LENGTH_LONG).show();
                        saveResetRequest();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
    private boolean canSendResetEmail() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int count = prefs.getInt(KEY_COUNT, 0);
        long lastTime = prefs.getLong(KEY_TIMESTAMP, 0);
        long now = Calendar.getInstance().getTimeInMillis();

        if (now - lastTime > 3600_000) {
            return true; // Reset lại mỗi giờ
        }
        return count < MAX_REQUESTS_PER_HOUR;
    }

    private void saveResetRequest() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        long now = Calendar.getInstance().getTimeInMillis();
        int count;
        long lastTime = prefs.getLong(KEY_TIMESTAMP, 0);

        if (now - lastTime > 3600_000) {
            count = 1; // reset count mỗi giờ
        } else {
            count = prefs.getInt(KEY_COUNT, 0) + 1;
        }

        prefs.edit()
                .putInt(KEY_COUNT, count)
                .putLong(KEY_TIMESTAMP, now)
                .apply();
    }
}
