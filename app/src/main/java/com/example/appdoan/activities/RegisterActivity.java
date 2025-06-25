package com.example.appdoan.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdoan.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, emailEditText, birthdayEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup genderRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        CheckBox showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        Button registerButton = findViewById(R.id.registerButton);
        birthdayEditText.setOnClickListener(v -> showDatePicker());

        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                confirmPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    @SuppressLint("DefaultLocale") String date = String.format("%02d/%02d/%04d", d, m + 1, y);
                    birthdayEditText.setText(date);
                }, year, month, day);
        dialog.show();
    }
    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String phone = phoneEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String birthday = birthdayEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String confirmPass = confirmPasswordEditText.getText().toString().trim();
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadio = findViewById(selectedId);
        final String gender = selectedRadio.getText().toString();
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || birthday.isEmpty() ||
                password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        saveUserToFirestore(user, name, phone, birthday, gender, email);
                    } else {
                        Toast.makeText(this, "Không thể tạo người dùng", Toast.LENGTH_SHORT).show();
                        Log.e("REGISTER", "FirebaseUser null sau khi đăng ký.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("REGISTER_ERROR", "Lỗi tạo tài khoản", e);
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String phone, String birthday, String gender, String inputEmail) {
        String email = user.getEmail() != null ? user.getEmail() : inputEmail;
        Log.d("REGISTER_CHECK", "name: " + name + ", phone: " + phone + ", email: " + email +
                ", birthday: " + birthday + ", gender: " + gender);

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("birthday", birthday);
        userData.put("gender", gender);
        String role = "user";
        if ("admin@gmail.com".equalsIgnoreCase(email)) {
            role = "admin";
        }
        userData.put("role", role);

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIRESTORE_ERROR", "Lỗi lưu dữ liệu", e);
                });
    }
}
