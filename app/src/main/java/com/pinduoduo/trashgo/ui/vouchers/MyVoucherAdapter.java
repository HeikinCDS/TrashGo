package com.pinduoduo.trashgo.ui.vouchers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pinduoduo.trashgo.data.model.MyVoucher;
import com.pinduoduo.trashgo.databinding.ItemMyVoucherBinding;

import java.util.ArrayList;
import java.util.List;

public class MyVoucherAdapter extends RecyclerView.Adapter<MyVoucherAdapter.MyVoucherViewHolder> {
    private final List<MyVoucher> vouchers = new ArrayList<>();

    public void submitList(List<MyVoucher> newList) {
        vouchers.clear();
        if (newList != null) {
            vouchers.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyVoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyVoucherBinding binding = ItemMyVoucherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyVoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVoucherViewHolder holder, int position) {
        holder.bind(vouchers.get(position));
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class MyVoucherViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyVoucherBinding binding;

        public MyVoucherViewHolder(@NonNull ItemMyVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MyVoucher voucher) {
            binding.myVoucherTitle.setText(voucher.getTitle());
            binding.myVoucherDate.setText("Claimed: " + voucher.getClaimDate());
            binding.myVoucherCode.setText("CODE: " + voucher.getCode());

            binding.btnCopyVoucherCode.setOnClickListener(v -> {
                Context context = binding.getRoot().getContext();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Voucher Code", voucher.getCode());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Voucher code copied to clipboard! 📋", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
