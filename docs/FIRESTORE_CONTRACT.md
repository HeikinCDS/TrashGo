# TrashGo Firestore Contract

This contract is shared by the App Foundation, Map, Scanning, and Points modules.
Do not rename collections or fields without agreement from all team members.

## `dropOffPoints/{dropOffId}`

| Field | Firestore type | Example |
|---|---|---|
| `id` | string | `kpr_library_entrance` |
| `name` | string | `Library Entrance Recycling Station (Simulated)` |
| `latitude` | number | `4.3372014` |
| `longitude` | number | `101.1415710` |
| `acceptedCategories` | array of strings | `["PLASTIC", "PAPER", "METAL"]` |
| `openingHours` | string | `Daily, 8:00 AM–10:00 PM` |
| `qrPayload` | string | `TRASHGO:DROP_OFF:kpr_library_entrance` |

Waste-category strings must exactly match `WasteCategory.java`:

`PLASTIC`, `PAPER`, `GLASS`, `METAL`, `EWASTE`, `ORGANIC`, `GENERAL`.

## Simulation behaviour

- The app seeds eight fixed UTAR Kampar simulation records only in debug builds.
- Seeding runs only when the `dropOffPoints` collection is empty.
- Fixed document IDs make the operation repeatable without creating duplicates.
- Names include `(Simulated)` because these are not claims about real facilities.
- QR payload format is `TRASHGO:DROP_OFF:<dropOffId>`.

The Map module reads this collection and filters by `acceptedCategories`. The
Points module validates the scanned QR payload against the document ID.
