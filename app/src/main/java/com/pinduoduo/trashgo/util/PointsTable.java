package com.pinduoduo.trashgo.util;

import androidx.annotation.Nullable;

import com.pinduoduo.trashgo.data.model.WasteCategory;

public final class PointsTable {

    private PointsTable() {}

    public static int forCategory(@Nullable WasteCategory category) {
        if (category == null) {
            return 2;
        }
        switch (category) {
            case EWASTE:  return 25;
            case METAL:   return 15;
            case GLASS:   return 12;
            case PLASTIC: return 10;
            case PAPER:   return 8;
            case ORGANIC: return 5;
            case GENERAL: return 2;
            default:      return 2;
        }
    }
}