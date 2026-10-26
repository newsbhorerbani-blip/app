# To-Let Gopalganj Native Android Application

A high-performance, native Android application for **To-Let Gopalganj** built with Kotlin and Jetpack Compose. It synchronizes in real time with the existing Firebase Firestore backend (`to-let-gopalganj`), offering seamless house and mess rental discovery, a second-hand goods marketplace, tenant rental requests ("বাসা চাই"), 1-on-1 real-time text and voice chat with audio waveforms, smart matching algorithms, and community reporting—with all administrative interfaces cleanly excluded.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed during Phase 1 clarification:

- **Authentication Strategy**: User authentication will directly validate credentials against the existing `users` collection in Firestore (matching mobile/email and password credentials with secure session persistence in Android DataStore), preserving existing accounts from the web portal without forcing Google Sign-In or modifying the user database schema.
- **In-App Messaging & Audio Voice Notes**: Realtime 1-on-1 private chat will support both live text messaging and recorded voice notes using Android's MediaRecorder / MediaPlayer, rendering custom animated audio waveform bars and progress playback.
- **Primary Mobile Navigation**: Clean Material 3 Navigation Bar featuring 5 dedicated destinations:
  1. **Home (হোম)**: House, flat, bachelor, and mess rental listings with smart Bengali/phonetic search and area filters.
  2. **Marketplace (পুরনো জিনিস)**: Second-hand furniture, electronics, and study goods buy/sell marketplace.
  3. **Requests (বাসা চাই)**: Tenant requirements with multi-area selection and direct owner contact.
  4. **Inbox (ইনবক্স)**: Realtime conversation list with unread message badges and active listing context.
  5. **Profile (প্রোফাইল)**: User profile, "আমার বিজ্ঞাপন" (personal listing management with status toggles), password updater, and session controls.
- **Strict Admin Exclusion**: All administrative entry points, admin dashboard tabs, Excel upload/download, banner editor, and role management are completely excluded from the mobile client.

---

## 1. Overview & Core Concept

### What It Does
To-Let Gopalganj is the premier digital rental and student mess portal for Gopalganj district and BSMRSTU (Bangabandhu Sheikh Mujibur Rahman Science and Technology University) university area. The mobile app lets tenants browse verified house rentals, bachelor rooms, mess seats, and sublets with photos, videos, and specs; chat directly or send voice notes to landlords; post "বাসা চাই" (housing wanted) requests across multiple neighborhoods; and buy or sell used goods when moving in or out.

### Target Audience & Persona
- **University Students & BSMRSTU Candidates**: Looking for budget bachelor mess seats, shared rooms, study tables, bicycles, and laptops.
- **Job Holders & Families**: Moving to Gopalganj looking for family apartments, 2-bed/3-bed flats, or sublets.
- **Property Owners & Mess Managers**: Posting vacant flats and responding to tenant inquiries via phone, WhatsApp, or in-app chat.

### Key Value Delivered
- **Zero Intermediaries**: Direct owner connection via phone, WhatsApp, and in-app chat.
- **Realtime Cloud Sync**: Instant live updates across both web and mobile platforms via Firestore snapshot listeners.
- **Smart Matching**: Proactively notifies tenants and landlords when a newly posted listing matches their exact neighborhood and rental criteria.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                        App Launch & Splash                             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                  Session Check (DataStore / Firestore)                 │
└───────────────┬────────────────────────────────────────┬───────────────┘
                │ Logged In                              │ Guest / New
                ▼                                        ▼
┌────────────────────────────────┐       ┌───────────────────────────────┐
│       Main Application         │       │    Login / Registration       │
│  (5-Tab Bottom Navigation Bar) │◄──────┤ (Phone/Email + Password + OTP)│
└───────────────┬────────────────┘       └───────────────────────────────┘
                │
                ├─► [Tab 1: Home] ──► Filter by Area/Type ──► Detail / Call / WhatsApp / Chat
                │
                ├─► [Tab 2: Market] ──► Category Filter ──► Post / Edit Sell Ad ──► Call / WA
                │
                ├─► [Tab 3: Requests] ──► Multi-Area Chips ──► Submit "বাসা চাই" ──► Match Alert
                │
                ├─► [Tab 4: Inbox] ──► Active Conversations ──► 1-on-1 Chat (Text & Voice Note)
                │
                └─► [Tab 5: Profile] ──► "আমার বিজ্ঞাপন" ──► Mark "ভাড়া/বিক্রি হয়েছে" ──► Settings
```

### Visual Identity & Theme
- **Dominant Palette**:
  - Primary: Emerald Green (`#059669`) — representing freshness, trust, and growth.
  - Secondary Accent: Teal (`#0D9488`) — providing depth to headers and cards.
  - Accent / Alerts: Warm Amber (`#D97706` for negotiable price/badges) and Rose (`#E11D48` for reports and sold badges).
  - Background: Clean Slate Tint (`#F8FAFC`) with crisp White (`#FFFFFF`) elevated surface cards.
- **Typography & Font Pairing**:
  - Bengali & Display Font: Hind Siliguri (`hind_siliguri.ttf`) for authentic, highly readable Bengali typography with full Unicode normalization.
  - Numbers & Badges: High-contrast Bengali numerals (`৳৬৫০০/মাস`) and localized badge formatting.
- **Micro-Interactions & Feedback**:
  - Smooth spring transitions between bottom tabs.
  - Interactive audio waveform bars animating during recording and voice playback.
  - Full-screen image viewer with pinch-to-zoom and carousel indicators.
  - Native video player sheet with playback controls.
  - Pull-to-refresh and shimmer skeletons for live listing updates.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Direct Firestore SDK Integration with Custom Configuration
- **Chosen Approach**: Use the official Android Firebase Firestore SDK configured to target the existing project `to-let-gopalganj` with realtime Snapshot listeners.
- **Why**: Keeps web and mobile completely in sync in real time without introducing any custom middle-tier proxy servers or breaking existing web user data.
- **Trade-Off**: Firestore rules on the existing project must support authenticated write operations; the app handles schema serialization with default values for all properties.

### Decision 2: Voice Note Recording Strategy
- **Chosen Approach**: Record standard M4A / AAC audio via Android `MediaRecorder`, encode as base64 data URI matching the web platform format (`data:audio/webm;base64,...` or `data:audio/mp4;base64,...`) for instant Firestore document delivery under 300KB (capped at 30 seconds).
- **Why**: 100% interoperable with the existing web app's HTML5 Audio playback without requiring additional cloud storage bucket setups or signed URLs.
- **Trade-Off**: 30-second cap prevents Firestore 1MB document size limit from being exceeded while preserving voice clarity.

### Decision 3: Smart Search & Banglish Phonetic Matching
- **Chosen Approach**: Implement an in-memory phonetic mapping engine in Kotlin that normalizes Bengali characters and Latin transliterations (Banglish phonetic equivalence) matching area names like "Nobinbag" <-> "নবীনবাগ", "Ghonapara" <-> "ঘোনাপাড়া".
- **Why**: Users in Bangladesh frequently type Bengali words using English keyboards; this prevents missing listings due to spelling or language differences.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer (UI)                         │
│   Jetpack Compose M3 • Hind Siliguri Typography • Material Theme       │
├───────────────┬────────────────┬───────────────┬───────────────────────┤
│  HomeScreen   │ MarketplaceUI  │ RequestsScreen│ Inbox & ChatScreen    │
└───────┬───────┴────────┬───────┴───────┬───────┴───────────┬───────────┘
        │                │               │                   │
        ▼                ▼               ▼                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          ViewModels Layer                              │
│       ListingViewModel • MarketViewModel • ChatViewModel • AuthViewModel│
│          StateFlow<UiState<T>> • Coroutines • Audio Recorder Engine    │
├────────────────────────────────────────────────────────────────────────┤
│                         Repository Layer                               │
│  ListingRepository • MarketRepository • RequestRepository • ChatRepo   │
│             Phonetic Search Engine • Media Handler (ImgBB)             │
├────────────────────────────────────────────────────────────────────────┤
│                       Data Layer & Remote Sync                         │
│  Firebase Firestore (to-let-gopalganj) • Android DataStore Preferences │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Models & Collections

```
┌───────────────────────────┐
│     listings              │
├───────────────────────────┤
│ id: String (BS-XXXX)      │
│ title: String             │
│ type: String              │
│ area: String              │
│ address: String           │
│ rent: Long                │
│ rentNegotiable: Boolean   │
│ available: String         │
│ phone: String             │
│ specs: String             │
│ desc: String              │
│ images: List<String>      │
│ video: String             │
│ rented: Boolean           │
│ rentedAt: Long            │
│ ownerPhone: String        │
│ createdAt: Long           │
└───────────────────────────┘

┌───────────────────────────┐
│     market_items          │
├───────────────────────────┤
│ id: String (PJ-XXXX)      │
│ title: String             │
│ category: String          │
│ condition: String         │
│ price: Long               │
│ negotiable: Boolean       │
│ area: String              │
│ phone: String             │
│ usage: String             │
│ desc: String              │
│ images: List<String>      │
│ status: "available"|"sold"│
│ soldAt: Long              │
│ ownerPhone: String        │
└───────────────────────────┘

┌───────────────────────────┐
│     rent_requests         │
├───────────────────────────┤
│ id: String (BC-XXXX)      │
│ name: String              │
│ phone: String             │
│ areas: List<String>       │
│ type: String              │
│ budget: Long              │
│ needFrom: String          │
│ members: String           │
│ desc: String              │
│ status: "open"|"done"     │
│ doneAt: Long              │
│ ownerPhone: String        │
└───────────────────────────┘

┌───────────────────────────┐
│     chats                 │
├───────────────────────────┤
│ id: Long                  │
│ roomKey: String           │
│ listingId: String         │
│ listingTitle: String      │
│ senderPhone: String       │
│ recipientPhone: String    │
│ text: String              │
│ type: "text"|"voice"      │
│ audioData: String (base64)│
│ duration: String          │
│ time: String              │
└───────────────────────────┘
```

### Interactive State & Component Mapping

1. **Authentication & Session**:
   - `UserSessionManager` stores logged-in user profile (`phone`, `name`, `email`) in Proto DataStore or encrypted preferences.
   - All listing creation, chat sending, and profile actions prompt login if not authenticated.
2. **Listings Feed & Filtering**:
   - Reactive `combine` pipeline joining query keyword, selected area chip, and rental type filter against real-time Firestore flow.
   - Bengali phonetic matcher runs instantaneously on client memory.
3. **Audio Voice Recorder**:
   - `VoiceRecorderManager` wraps Android `AudioRecord`/`MediaRecorder` with amplitude sampling flow emitting 5 waveform levels to Compose Canvas.
   - `VoicePlayerManager` wraps `MediaPlayer` with synchronized progress scrubber and waveform playhead.
4. **Smart Match Suggestions**:
   - Triggers a bottom sheet notification when an active user requirement has overlapping areas and budgets with newly posted rental listings.
5. **Auto-Deletion Calculator**:
   - Items marked as "ভাড়া হয়ে গেছে" (rented) or "বিক্রি হয়ে গেছে" (sold) calculate and display remaining days until automatic cleanup (7-day window).
