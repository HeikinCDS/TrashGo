package com.pinduoduo.trashgo.ui.vouchers;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pinduoduo.trashgo.data.model.Voucher;
import com.pinduoduo.trashgo.databinding.ItemVoucherBinding;

import java.util.ArrayList;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    public interface OnRedeemClickListener {
        void onRedeemClick(@NonNull Voucher voucher);
    }

    private final List<Voucher> vouchers = new ArrayList<>();
    private final OnRedeemClickListener listener;

    public VoucherAdapter(@NonNull OnRedeemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Voucher> newList) {
        vouchers.clear();
        if (newList != null) {
            vouchers.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVoucherBinding binding = ItemVoucherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        holder.bind(vouchers.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        private final ItemVoucherBinding binding;

        public VoucherViewHolder(@NonNull ItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Voucher voucher, OnRedeemClickListener listener) {
            binding.voucherTitle.setText(voucher.getTitle());
            binding.voucherDesc.setText(voucher.getDescription());
            binding.btnRedeemVoucher.setText("Redeem (" + voucher.getPointsCost() + " pts)");

            binding.btnRedeemVoucher.setOnClickListener(v -> listener.onRedeemClick(voucher));
        }
    }
}
