package com.pinduoduo.trashgo.ui.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.DialogDisposalSuccessBinding;

public class DisposalSuccessDialog extends BottomSheetDialogFragment {
    public static final String TAG = "DisposalSuccessDialog";

    private static final String ARG_POINTS = "arg_points";
    private static final String ARG_STATION = "arg_station";
    private static final String ARG_QUEST_NOTICE = "arg_quest_notice";

    private DialogDisposalSuccessBinding binding;

    public static void show(@NonNull FragmentManager fm, int pointsEarned, @NonNull String stationName, @Nullable String questNotice) {
        DisposalSuccessDialog dialog = new DisposalSuccessDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_POINTS, pointsEarned);
        args.putString(ARG_STATION, stationName);
        args.putString(ARG_QUEST_NOTICE, questNotice);
        dialog.setArguments(args);

        if (fm.isDestroyed()) {
            return;
        }
        if (fm.isStateSaved()) {
            FragmentTransaction tx = fm.beginTransaction();
            tx.add(dialog, TAG);
            tx.commitAllowingStateLoss();
        } else {
            dialog.show(fm, TAG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogDisposalSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        int points = args != null ? args.getInt(ARG_POINTS, 0) : 0;
        String station = args != null ? args.getString(ARG_STATION, "Disposal Station") : "Disposal Station";
        String questNotice = args != null ? args.getString(ARG_QUEST_NOTICE) : null;

        binding.successPointsText.setText("+" + points + " Points Earned! 🎉");
        binding.successStationText.setText("Disposed at " + station);

        if (questNotice != null && !questNotice.trim().isEmpty()) {
            binding.successQuestNotice.setText(questNotice);
            binding.successQuestNotice.setVisibility(View.VISIBLE);
        } else {
            binding.successQuestNotice.setVisibility(View.GONE);
        }

        binding.btnDoneDisposal.setOnClickListener(v -> {
            dismiss();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
