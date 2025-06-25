package com.example.appdoan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.appdoan.activities.LoginActivity;
import com.example.appdoan.activities.SettingActivity;
import com.example.appdoan.fragment.AccountFragment;
import com.example.appdoan.fragment.AdminFragment;
import com.example.appdoan.fragment.BookingFragment;
import com.example.appdoan.fragment.HistoryFragment;
import com.example.appdoan.fragment.HomeFragment;
import com.example.appdoan.fragment.ManageUserFragment;
import com.example.appdoan.fragment.OrderFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private String userRole = "user"; // mặc định
//change
    //change 2
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        var db = FirebaseFirestore.getInstance();
        var currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                        if (userRole == null) userRole = "user";

                        // Load menu theo vai trò
                        if (userRole.equals("admin")) {
                            bottomNavigationView.getMenu().clear();
                            bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin);
                            loadFragment(new AdminFragment());
                        } else {
                            bottomNavigationView.getMenu().clear();
                            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
                            loadFragment(new HomeFragment());
                        }

                        // Xử lý chọn tab
                        bottomNavigationView.setOnItemSelectedListener(item -> {
                            Fragment selectedFragment = null;
                            int id = item.getItemId();

                            if (userRole.equals("admin")) {
                                if (id == R.id.nav_admin) {
                                    selectedFragment = new AdminFragment();
                                } else if (id == R.id.nav_muser) {
                                    selectedFragment = new ManageUserFragment();
                                } else if (id == R.id.nav_account1) {
                                    selectedFragment = new AccountFragment();
                                } else if (id == R.id.menu_booking) {
                                    selectedFragment = new BookingFragment();
                                }
                            } else {
                                if (id == R.id.nav_home) {
                                    selectedFragment = new HomeFragment();
                                } else if (id == R.id.nav_order) {
                                    selectedFragment = new OrderFragment();
                                } else if (id == R.id.nav_history) {
                                    selectedFragment = new HistoryFragment();
                                } else if (id == R.id.nav_account) {
                                    selectedFragment = new AccountFragment();
                                }
                            }

                            if (selectedFragment != null) {
                                loadFragment(selectedFragment);
                                return true;
                            }
                            return false;
                        });

                    } else {
                        Toast.makeText(this, "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Lỗi Firestore: " + e.getMessage());
                    Toast.makeText(this, "Không thể lấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnbottom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;

        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        if (!isFinishing() && !isDestroyed()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
        }
    }
}
