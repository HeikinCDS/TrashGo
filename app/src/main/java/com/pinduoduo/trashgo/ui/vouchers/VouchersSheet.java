package com.pinduoduo.trashgo.ui.vouchers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.MyVoucher;
import com.pinduoduo.trashgo.data.model.Voucher;
import com.pinduoduo.trashgo.data.repository.MyVoucherManager;
import com.pinduoduo.trashgo.data.repository.PointsRepositoryImpl;
import com.pinduoduo.trashgo.databinding.SheetVouchersBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VouchersSheet extends BottomSheetDialogFragment {

    public static final String TAG = "VouchersSheet";

    private SheetVouchersBinding binding;
    private PointsRepositoryImpl pointsRepository;
    private VoucherAdapter availableAdapter;
    private MyVoucherAdapter myVoucherAdapter;
    private int currentPointsBalance = 0;

    public static void show(@NonNull FragmentManager fm) {
        new VouchersSheet().show(fm, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetVouchersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pointsRepository = new PointsRepositoryImpl();

        // Available Rewards Adapter
        availableAdapter = new VoucherAdapter(this::onRedeemVoucher);
        binding.vouchersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.vouchersRecyclerView.setAdapter(availableAdapter);

        // Your Vouchers Adapter
        myVoucherAdapter = new MyVoucherAdapter();
        binding.myVouchersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.myVouchersRecyclerView.setAdapter(myVoucherAdapter);

        // Tab click listeners
        binding.btnTabAvailable.setOnClickListener(v -> switchToTab(true));
        binding.btnTabMyVouchers.setOnClickListener(v -> switchToTab(false));

        binding.btnClaimCode.setOnClickListener(v -> claimPromoCode());

        loadAvailableVouchers();
        loadMyVouchers();
        loadUserBalance();

        switchToTab(true);
    }

    private void switchToTab(boolean showAvailable) {
        if (showAvailable) {
            binding.layoutAvailableRewards.setVisibility(View.VISIBLE);
            binding.layoutYourVouchers.setVisibility(View.GONE);
            binding.btnTabAvailable.setBackgroundColor(requireContext().getColor(R.color.trashgo_primary));
            binding.btnTabAvailable.setTextColor(requireContext().getColor(R.color.trashgo_on_primary));
            binding.btnTabMyVouchers.setBackgroundColor(requireContext().getColor(R.color.trashgo_surface_sunken));
            binding.btnTabMyVouchers.setTextColor(requireContext().getColor(R.color.trashgo_on_surface));
        } else {
            binding.layoutAvailableRewards.setVisibility(View.GONE);
            binding.layoutYourVouchers.setVisibility(View.VISIBLE);
            binding.btnTabMyVouchers.setBackgroundColor(requireContext().getColor(R.color.trashgo_primary));
            binding.btnTabMyVouchers.setTextColor(requireContext().getColor(R.color.trashgo_on_primary));
            binding.btnTabAvailable.setBackgroundColor(requireContext().getColor(R.color.trashgo_surface_sunken));
            binding.btnTabAvailable.setTextColor(requireContext().getColor(R.color.trashgo_on_surface));
            loadMyVouchers();
        }
    }

    private void loadUserBalance() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null || snapshot == null) return;
                    Long pts = snapshot.getLong("totalPoints");
                    currentPointsBalance = pts != null ? pts.intValue() : 0;
                    binding.voucherBalanceText.setText("⭐️ " + currentPointsBalance + " Pts");
                });
    }

    private void loadAvailableVouchers() {
        List<Voucher> list = new ArrayList<>();
        list.add(new Voucher("v1", "☕️ $5 Eco Coffee Voucher", "Valid at participating eco cafes", 200, "COFFEE5"));
        list.add(new Voucher("v2", "🛒 $10 Supermarket Coupon", "Valid for organic & recycled goods", 350, "GROCERY10"));
        list.add(new Voucher("v3", "🎒 Eco Canvas Tote Bag", "Claim at any TrashGo drop-off station", 500, "BAG500"));
        list.add(new Voucher("v4", "🎟️ $20 Green Merchant Pass", "Valid across green stores nation-wide", 750, "GREEN20"));
        availableAdapter.submitList(list);
    }

    private void loadMyVouchers() {
        List<MyVoucher> claimedList = MyVoucherManager.getClaimedVouchers(requireContext());
        if (binding == null) return;

        int count = claimedList.size();
        binding.btnTabMyVouchers.setText("Your Vouchers (" + count + ")");

        if (claimedList.isEmpty()) {
            binding.txtEmptyMyVouchers.setVisibility(View.VISIBLE);
            binding.myVouchersRecyclerView.setVisibility(View.GONE);
        } else {
            binding.txtEmptyMyVouchers.setVisibility(View.GONE);
            binding.myVouchersRecyclerView.setVisibility(View.VISIBLE);
            myVoucherAdapter.submitList(claimedList);
        }
    }

    private void onRedeemVoucher(@NonNull Voucher voucher) {
        pointsRepository.redeemVoucher(voucher.getTitle(), voucher.getPointsCost(), new PointsRepositoryImpl.ActionCallback() {
            @Override
            public void onSuccess(@NonNull String message, int newBalance) {
                if (binding == null) return;
                currentPointsBalance = newBalance;
                binding.voucherBalanceText.setText("⭐️ " + newBalance + " Pts");

                String uniqueCode = voucher.getVoucherCode() + "-" + (1000 + new Random().nextInt(9000));
                MyVoucherManager.addClaimedVoucher(requireContext(), voucher.getTitle(), uniqueCode, voucher.getPointsCost());
                loadMyVouchers();

                Toast.makeText(requireContext(), message + "\nCode: " + uniqueCode + " added to Your Vouchers! 🎟️", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull String error) {
                if (binding == null) return;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void claimPromoCode() {
        String code = binding.editPromoCode.getText() != null ? binding.editPromoCode.getText().toString() : "";
        if (code.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a voucher/promo code.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String cleanCode = code.trim().toUpperCase();

        pointsRepository.claimVoucherCode(cleanCode, new PointsRepositoryImpl.ActionCallback() {
            @Override
            public void onSuccess(@NonNull String message, int newBalance) {
                if (binding == null) return;
                currentPointsBalance = newBalance;
                binding.voucherBalanceText.setText("⭐️ " + newBalance + " Pts");
                binding.editPromoCode.setText("");

                MyVoucherManager.addClaimedVoucher(requireContext(), "🎟️ Promo Bonus: " + cleanCode, cleanCode + "-BONUS", 0);
                loadMyVouchers();

                Toast.makeText(requireContext(), message + " Added to Your Vouchers! 🎟️", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull String error) {
                if (binding == null) return;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
