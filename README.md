# SawtFlow AI — Arabic Voice Intelligence for Business

**SawtFlow** is a modern Android application built with **Jetpack Compose** and **Material Design 3**, porting the full capability of SawtFlow's Arabic voice intelligence workspace.

## Core Features Ported

1. **Voice Intelligence Studio (5-Step Pipeline)**
   - **Step 1 (Input)**: Colloquial dialect selector (Gulf/Saudi, Egyptian, Levantine, Maghrebi), real-time simulated audio recorder with timer and live waveform animation, and sample message loaders.
   - **Step 2 (Understand)**: Speech recognition transcript viewer and in-place correction editor ("Correct the meaning, not the speaker"), with confidence scoring and intent extraction triggers.
   - **Step 3 (Intent Intelligence)**: Intent classification (`status`, `cancel`, `modify`), Arabic/English mixed language detection, urgency levels, and order entity extraction (`SF-XXXX`).
   - **Step 4 (Action & Retrieval)**: Read-only evidence query against 11 operational records (including delayed, canceled, transit, delivered, and customs states) with quick-select test chips and last-scan timeline.
   - **Step 5 (Approval / Human-in-the-Loop)**: Draft contextual response generation and human approval gate with immutable audit log recording.

2. **Operational Database & Evidence Base**
   - 11 pre-loaded mock orders with delivery ETA, last scan hub, status badges, and search/filter chips.
   - Direct 1-tap test bridging: any order can be transferred to Studio for voice validation.

3. **Immutable Audit Trail & Governance**
   - Full append-only event logging for speech recording, transcript correction, order lookup, and response approval.

4. **Overview & Product Brief**
   - Core capabilities, 5-step workflow overview, use cases, and interactive demonstration request form.

5. **Bilingual RTL & LTR Support**
   - Instant language toggle between English and Arabic (`عربي` / `EN`) with full layout direction adaptation (`LayoutDirection.Rtl` / `Ltr`).

## Technical Stack
- **Framework**: Android Kotlin + Jetpack Compose
- **Target SDK**: 36, Min SDK: 26, Java: 21
- **UI & Theming**: Material Design 3, custom SawtFlow Navy & Cyan/Amber palette
- **Adaptive Icon**: Custom SawtFlow audio wave & verified amber node adaptive launcher icon
