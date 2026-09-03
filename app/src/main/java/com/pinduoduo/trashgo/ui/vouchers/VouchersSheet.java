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
import com.pinduoduo.trashgo.data.model.Voucher;
import com.pinduoduo.trashgo.data.repository.PointsRepositoryImpl;
import com.pinduoduo.trashgo.databinding.SheetVouchersBinding;

import java.util.ArrayList;
import java.util.List;

public class VouchersSheet extends BottomSheetDialogFragment {

    public static final String TAG = "VouchersSheet";

    private SheetVouchersBinding binding;
    private PointsRepositoryImpl pointsRepository;
    private VoucherAdapter adapter;
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

        adapter = new VoucherAdapter(this::onRedeemVoucher);
        binding.vouchersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.vouchersRecyclerView.setAdapter(adapter);

        binding.btnClaimCode.setOnClickListener(v -> claimPromoCode());

        loadAvailableVouchers();
        loadUserBalance();
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
        adapter.submitList(list);
    }

    private void onRedeemVoucher(@NonNull Voucher voucher) {
        pointsRepository.redeemVoucher(voucher.getTitle(), voucher.getPointsCost(), new PointsRepositoryImpl.ActionCallback() {
            @Override
            public void onSuccess(@NonNull String message, int newBalance) {
                if (binding == null) return;
                currentPointsBalance = newBalance;
                binding.voucherBalanceText.setText("⭐️ " + newBalance + " Pts");
                Toast.makeText(requireContext(), message + "\nCode: " + voucher.getVoucherCode(), Toast.LENGTH_LONG).show();
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

        pointsRepository.claimVoucherCode(code, new PointsRepositoryImpl.ActionCallback() {
            @Override
            public void onSuccess(@NonNull String message, int newBalance) {
                if (binding == null) return;
                currentPointsBalance = newBalance;
                binding.voucherBalanceText.setText("⭐️ " + newBalance + " Pts");
                binding.editPromoCode.setText("");
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
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
