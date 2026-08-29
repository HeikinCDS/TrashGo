# Fix Scan Navigation and AI Integration

The current code has several compilation errors and architectural issues, including mismatched filenames, redundant API logic in the Fragment, and invalid fragment constructor usage.

## User Review Required

> [!IMPORTANT]
> - I will rename `ScanFragmentResult.java` to `ScanResultFragment.java` to match the class name.
> - I will refactor `ScanResultFragment` to use the recommended `newInstance` pattern instead of a custom constructor with arguments, which prevents crashes during fragment recreation.
> - I will simplify `ScanFragment` by using the existing `GeminiRepository` instead of reimplementing the Retrofit logic inside the fragment.

## Proposed Changes

### [UI Components]

#### [MODIFY] [ScanFragment.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/ui/scan/ScanFragment.java)
- Remove redundant `sendToGemini`, `parseGeminiResponse` methods.
- Update `processCapturedImage` to call `geminiRepository.classifyWaste`.
- Implement `openResultPage` to navigate to `ScanResultFragment` using `newInstance`.
- Show/hide the loading overlay during the AI call.

#### [RENAME] `ScanFragmentResult.java` -> [ScanResultFragment.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/ui/scan/ScanResultFragment.java)
- Rename file to match class name.
- Remove the argument-based constructor.
- Implement `newInstance(Uri imageUri, String category, double confidence, String tip)`.
- Update `onViewCreated` to read from arguments.

### [Layouts]

#### [MODIFY] [fragment_scan_result.xml](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/res/layout/fragment_scan_result.xml)
- Add missing XML header.

## Verification Plan

### Automated Tests
- Run `analyze_file` on all modified files to ensure zero errors.

### Manual Verification
1. Launch the app and go to the Scan screen.
2. Capture a photo.
3. Verify that the "Identifying waste..." overlay appears.
4. Verify that the app navigates to the Result screen showing the image, category, confidence, and tip.
5. Click "Scan Again" and verify it returns to the camera.
