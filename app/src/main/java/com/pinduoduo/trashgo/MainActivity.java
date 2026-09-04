package com.pinduoduo.trashgo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.pinduoduo.trashgo.databinding.ActivityMainBinding;
import com.pinduoduo.trashgo.data.seed.DropOffPointSeeder;
import com.pinduoduo.trashgo.ui.home.HomeFragment;
import com.pinduoduo.trashgo.ui.leaderboard.LeaderboardFragment;
import com.pinduoduo.trashgo.ui.map.MapFragment;
import com.pinduoduo.trashgo.ui.profile.ProfileFragment;
import com.pinduoduo.trashgo.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (BuildConfig.DEBUG) {
            DropOffPointSeeder.seedIfEmpty();
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment destination;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
                destination = new MapFragment();
            } else if (itemId == R.id.nav_leaderboard) {
                destination = new LeaderboardFragment();
            } else if (itemId == R.id.nav_profile) {
                destination = new ProfileFragment();
            } else {
                destination = new HomeFragment();
            }
            show(destination);
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
