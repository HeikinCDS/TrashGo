package com.pinduoduo.trashgo.data.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.util.PointsTable;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class DropOffVerifierTest {

    private static final double LAT = 4.3366214;
    private static final double LNG = 101.1421110;
    private static final long NOW = 1_700_000_000_000L;

    @Before
    public void enableAllRules() {
        DropOffVerifier.enforceDistance = true;
        DropOffVerifier.enforceCooldown = true;
    }

    private DropOffPoint point() {
        return new DropOffPoint("fict_block", "FICT Block", LAT, LNG,
                Arrays.asList(WasteCategory.PLASTIC, WasteCategory.PAPER),
                "8am - 8pm", "TRASHGO:DROP_OFF:fict_block");
    }

    private VerificationResult verify(String payload, Double lat, Double lng,
                                      boolean accepts, boolean mocked, long lastClaim) {
        return DropOffVerifier.verify(payload, point(), accepts, lat, lng, mocked, lastClaim, NOW);
    }

    @Test
    public void parsesValidPayload() {
        assertEquals("fict_block", DropOffVerifier.parseId("TRASHGO:DROP_OFF:fict_block"));
        assertEquals("fict_block", DropOffVerifier.parseId("  TRASHGO:DROP_OFF:fict_block  "));
        assertEquals("fict_block", DropOffVerifier.parseId("trashgo:drop_off:fict_block"));
    }

    @Test
    public void rejectsMalformedPayload() {
        assertNull(DropOffVerifier.parseId(null));
        assertNull(DropOffVerifier.parseId(""));
        assertNull(DropOffVerifier.parseId("TRASHGO:DROP_OFF:"));
        assertNull(DropOffVerifier.parseId("TRASHGO:DROP_OFF:   "));
        assertNull(DropOffVerifier.parseId("https://example.com"));
    }

    @Test
    public void acceptsLegitimateClaim() {
        assertEquals(VerificationResult.OK,
                verify("TRASHGO:DROP_OFF:fict_block", LAT, LNG, true, false, 0L));
    }

    @Test
    public void acceptsUserWithinRange() {
        assertEquals(VerificationResult.OK,
                verify("TRASHGO:DROP_OFF:fict_block", LAT + 0.0008, LNG, true, false, 0L));
    }

    @Test
    public void rejectsUserTooFarAway() {
        assertEquals(VerificationResult.TOO_FAR,
                verify("TRASHGO:DROP_OFF:fict_block", LAT + 0.002, LNG, true, false, 0L));
    }

    @Test
    public void rejectsAnotherPointsQrCode() {
        assertEquals(VerificationResult.WRONG_POINT,
                verify("TRASHGO:DROP_OFF:library", LAT, LNG, true, false, 0L));
    }

    @Test
    public void rejectsUnrelatedQrCode() {
        assertEquals(VerificationResult.BAD_PAYLOAD,
                verify("https://google.com", LAT, LNG, true, false, 0L));
    }

    @Test
    public void rejectsMaterialThePointDoesNotAccept() {
        assertEquals(VerificationResult.CATEGORY_NOT_ACCEPTED,
                verify("TRASHGO:DROP_OFF:fict_block", LAT, LNG, false, false, 0L));
    }

    @Test
    public void rejectsMockLocationEvenWhenStandingThere() {
        assertEquals(VerificationResult.MOCK_LOCATION,
                verify("TRASHGO:DROP_OFF:fict_block", LAT, LNG, true, true, 0L));
    }

    @Test
    public void rejectsWhenThereIsNoFix() {
        assertEquals(VerificationResult.NO_LOCATION,
                verify("TRASHGO:DROP_OFF:fict_block", null, null, true, false, 0L));
    }

    @Test
    public void blocksRepeatClaimWithinCooldown() {
        assertEquals(VerificationResult.COOLDOWN,
                verify("TRASHGO:DROP_OFF:fict_block", LAT, LNG, true, false,
                        NOW - 5 * 60 * 1000L));
    }

    @Test
    public void allowsClaimOnceCooldownExpires() {
        assertEquals(VerificationResult.OK,
                verify("TRASHGO:DROP_OFF:fict_block", LAT, LNG, true, false,
                        NOW - 31 * 60 * 1000L));
    }

    @Test
    public void allowsRepeatClaimWhenCooldownIsSwitchedOff() {
        DropOffVerifier.enforceCooldown = false;
        assertEquals(VerificationResult.OK,
                verify("TRASHGO:DROP_OFF:fict_block", LAT, LNG, true, false,
                        NOW - 5 * 60 * 1000L));
    }

    @Test
    public void checksPayloadBeforeAnythingElse() {
        assertEquals(VerificationResult.BAD_PAYLOAD,
                DropOffVerifier.verify("junk", null, false, null, null, true, NOW, NOW));
    }

    @Test
    public void awardsPointsByMaterial() {
        assertEquals(25, PointsTable.forCategory(WasteCategory.EWASTE));
        assertEquals(10, PointsTable.forCategory(WasteCategory.PLASTIC));
        assertEquals(2, PointsTable.forCategory(WasteCategory.GENERAL));
        assertEquals(2, PointsTable.forCategory(null));
    }
}
