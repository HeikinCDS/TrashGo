# Upgrade Gemini Model to 3.6 Flash

The 404 error is caused by the decommissioning of `gemini-2.5-flash` for new users. The API specifically recommends upgrading to `gemini-3.6-flash`.

## User Review Required

> [!NOTE]
> I am updating the model name to `gemini-3.6-flash` as recommended by the API error message.
> Although the API also suggested the new stateful "Interactions API", our current implementation using `generateContent` remains fully supported for single-shot tasks like waste identification and is significantly simpler to maintain.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [GeminiAPI.java](file:///C:/Users/User/Documents/School resources/Chin_JingLie/MobileAppGroupAssignment/TrashGo/app/src/main/java/com/pinduoduo/trashgo/data/remote/GeminiAPI.java)
- Update the `@POST` annotation to use `gemini-3.6-flash`.

## Verification Plan

### Automated Tests
- Run `app:assembleDebug` to ensure successful compilation.

### Manual Verification
1. Launch the app and go to the Scan screen.
2. Capture a photo.
3. Verify that the identification results are returned correctly using the 3.6 model.
