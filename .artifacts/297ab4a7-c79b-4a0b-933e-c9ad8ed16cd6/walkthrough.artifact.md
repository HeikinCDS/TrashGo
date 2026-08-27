# Walkthrough: Fixed Camera Scanning and Navigation

I have fixed the issue where the camera preview was not showing. The primary cause was that the "Scan waste" button on the Home screen was not yet linked to the `ScanFragment`, and the camera initialization was occurring before the view was fully ready.

## Changes Made

### Layout Enhancements
- **[fragment_home.xml](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/res/layout/fragment_home.xml)**: Added `android:id="@+id/btnScanWaste"` to the scan button.
- **[fragment_scan.xml](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/res/layout/fragment_scan.xml)**: Added a "Close" button (top-left) to allow navigation back to Home. Fixed the hardcoded "Scan" text to use the `@string/scan_waste` resource.
- **[strings.xml](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/res/values/strings.xml)**: Added `close` string resource.

### Implementation Logic
- **[HomeFragment.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/ui/home/HomeFragment.java)**: Implemented a click listener for `btnScanWaste` that navigates to `ScanFragment`.
- **[ScanFragment.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/ui/scan/ScanFragment.java)**:
    - Moved camera initialization logic to `onViewCreated` to ensure the `PreviewView` is ready.
    - Added a click listener for the `btnClose` button to pop the fragment backstack.
    - Improved camera lifecycle binding.

## Verification Results

### Automated Tests
- `gradlew app:assembleDebug` completed successfully, confirming no compilation errors.

### Manual Verification Required
> [!IMPORTANT]
> Please deploy the app to your device and test the following flow:
> 1. Click **Scan waste** on the Home screen.
> 2. Grant camera permissions if prompted.
> 3. Verify the **live camera preview** appears.
> 4. Click the **Close** button (top-left) to return to the Home screen.
