package com.pinduoduo.trashgo.data.model;
import org.junit.Test;
import static org.junit.Assert.*;
public class ScanHistoryEntryTest {
    @Test public void onlySelectedPendingScanCanReceiveDropOff() {
        ScanHistoryEntry entry = new ScanHistoryEntry();
        entry.id = "scan-1";
        assertTrue(entry.canComplete("scan-1"));
        assertFalse(entry.canComplete("scan-2"));
        assertFalse(entry.canComplete(null));
        entry.droppedOffAt = 123L;
        assertFalse(entry.canComplete("scan-1"));
    }
}
