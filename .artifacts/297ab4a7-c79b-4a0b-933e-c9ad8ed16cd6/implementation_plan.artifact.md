# Fix Camera Scanning and Navigation

The camera scanning functionality is currently not working because:
1.  **Missing Navigation**: The "Scan waste" button on the Home screen is not wired to open the `ScanFragment`.
2.  **Implementation Lifecycle**: `ScanFragment` initializes the camera in `onCreateView`, which can sometimes lead to issues with the `PreviewView` not being ready.
3.  **Missing ID**: The button in `fragment_home.xml` lacks an ID for programmatic access.

## User Review Required

> [!IMPORTANT]
> I will modify `fragment_home.xml` to add an ID to the scan button and `HomeFragment.java` to handle the navigation. I will also refactor `ScanFragment.java` to improve reliability.

## Proposed Changes

### [Layouts]

#### [MODIFY] [fragment_home.xml](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/res/layout/fragment_home.xml)
- Add `android:id="@+id/btnScanWaste"` to the MaterialButton.

#### [MODIFY] [fragment_scan.xml](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/res/layout/fragment_scan.xml)
- Add a "Close" button to allow users to return to the Home screen.

### [UI Components]

#### [MODIFY] [HomeFragment.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/ui/home/HomeFragment.java)
- Set a click listener on `btnScanWaste` to replace the current fragment with `ScanFragment`.

#### [MODIFY] [ScanFragment.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/ui/scan/ScanFragment.java)
- Move camera initialization logic to `onViewCreated`.
- Add a click listener for the new "Close" button.
- Ensure `binding.previewView.getSurfaceProvider()` is used correctly when the view is ready.

## Verification Plan

### Automated Tests
- N/A

### Manual Verification
1.  Launch the app.
2.  Click the "Scan waste" button on the Home screen.
3.  Verify that the app requests camera permission (if not already granted).
4.  Verify that the live camera preview is displayed.
5.  Click the "Close" button and verify it returns to the Home screen.
