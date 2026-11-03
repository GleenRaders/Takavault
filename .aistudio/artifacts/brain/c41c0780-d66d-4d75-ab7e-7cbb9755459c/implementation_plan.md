# টাকাভল্ট ৪.০ (TakaVault 4.0) — স্মার্ট বিকাশ ও ভাঙতি নোট অ্যান্ড্রয়েড অ্যাপ

TakaVault 4.0 transforms the cyber-fintech Bangladeshi wallet and banknote denomination tracker into a high-performance native Android application built with Jetpack Compose and offline Room persistence. It replaces the "আসল টাকা / আসল নোট" section and navigation branding with **"ভাঙতি"** while ensuring a responsive, clutter-free, non-overlapping mobile interface across all screen sizes.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product and architectural choices have been confirmed from your requirements and answers:

- **Confirmed Architecture & Persistence**: Native Jetpack Compose UI backed by a fast, offline-first **Room SQLite Database** (with JSON backup/restore support and instant state updates).
- **Confirmed Section & Tab Rename ("ভাঙতি")**: The second bottom navigation tab (`💵 ভাঙতি`) and the vault inventory section header/descriptions are renamed from "আসল টাকা / আসল নোট" to **"ভাঙতি"** (while keeping "নিজের আসল টাকা" intact in the Temporary Money / অস্থায়ী টাকা card on the Home tab).
- **Confirmed Initial Data State**: Pre-loaded on first launch with the rich sample Bangladeshi banknote inventory (`৳১০০০` down to `৳১`), bKash balance (`৳৮,৪৫০`), sample transactions, and shopkeeper change dues, plus a one-tap option in Settings to reset everything to `০` or reload demo data anytime.

---

## 1. Overview & Core Concept

- **What It Does**: Tracks both digital **bKash** funds and physical **Bangladeshi banknotes (`৳১০০০`, `৳৫০০`, `৳২০০`, `৳১০০`, `৳৫০`, `৳২০`, `৳১০`, `৳৫`, `৳২`, `৳১`)** with an intelligent **Smart Change ("ভাঙতি") & Expense Engine**. When paying in cash, users select the exact notes handed to the shopkeeper and the exact change notes received back; any shortfall or excess is automatically logged in the **Shopkeeper & Personal Debt Ledger (বাকি ও পাওনা খাতা)**.
- **Target Audience / Persona**: Everyday Bangladeshi mobile users, commuters, students, and household budgeters who manage daily cash change (rickshaw/auto fares, bazaar shopping) alongside bKash payments.
- **Key Value**: Eliminates mental math and forgotten shopkeeper change ("ভাঙতি বকেয়া") with fast, single-hand mobile ergonomics, zero overlapping elements, and tactile audio/haptic feedback.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Home & Vault Overview (`🏠 হোম ও ভল্ট`)**: Displays the monthly budget progress & daily safe-spend limit, the interactive bKash wallet card (tap-to-reveal balance + 4-action dock: Add Money, bKash Payment, Cash-Out, Cash-In), the compact 10-denomination **ভাঙতি** counter strip, quick 1-tap expense categories, recent transactions, debt summary, and the **অস্থায়ী টাকা (Temporary Money)** tracker.
  2. **ভাঙতি Inventory (`💵 ভাঙতি`)**: Dedicated 2-column responsive grid of all 10 Bangladeshi denominations (`৳১০০০` — `৳১`) with custom Canvas/vector Bangladeshi banknote artwork, live note counts, subtotal calculations, `+`/`−` steppers with 48dp minimum touch targets, and a tap-to-inspect **HD Note Preview Modal**.
  3. **Smart 4-Step Expense & Change Engine (`⚡ নতুন খরচ`)**:
     - *Step 1*: Pick payment method (bKash vs. Cash).
     - *Step 2A (bKash)*: Enter amount + quick presets (`৳৫০`–`৳১,০০০`), view live post-payment balance preview, and confirm.
     - *Step 2B-1 & 2B-2 (Cash Bill & Tendered Notes)*: Enter bill amount, tap `✨ অটো নোট নির্বাচন` or manually pick tendered notes from available vault stock.
     - *Step 2B-3 (Change Verification)*: View expected change (`প্রদত্ত টাকা − বিল`), tap `⚡ সঠিক ভাংতি অটো-পূরণ` or select received notes, and see real-time discrepancy alerts (exact match, shopkeeper owes change, or extra received).
     - *Step 2B-4 (Final Review)*: Inspect outgoing/incoming note chips and commit to update the vault and auto-record any unpaid change into the Debt Ledger.
  4. **Categories & 1-Tap Recurring Bills (`⚡ খরচ ও খাত`)**: Browse/add custom Bengali categories with emoji icons and trigger 1-tap monthly presets (House Rent, Electricity/Gas, Broadband WiFi, Auto/Rickshaw fare).
  5. **Debt Ledger, bKash Fee Calculator & Smart Change Advisor (`📒 বাকি ও টুলস`)**: Add or settle receivable/payable dues, calculate live bKash Cash-Out fees (Regular `1.85%` vs. Priyo Agent/ATM `1.49%`), and check vault change health (`৳২০`–`৳১` note count).
  6. **Analytics & Filterable History (`📊 ইতিহাস ও চার্ট`)**: Interactive Compose Canvas Donut Chart for category spending, bKash vs. Cash split bars, note tier distribution, multi-filter transaction feed (search, method, category, date range), and Digital Receipt generator with share/export support.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Cyber-Fintech Dark UI tailored for high contrast and zero eye strain, featuring glassmorphic dark slate cards with subtle neon borders.
  - *Color Palette & Mood*:
    - `--background`: Deep Obsidian (`#0B0F19`) with dark slate surfaces (`#0F172A` / `#111827`).
    - `--primary`: Cyber Neon Cyan (`#00F0FF`) for primary navigation, focus rings, and analytical accents.
    - `--secondary`: Neon Mint (`#00FFA3`) for cash vault totals, positive inflows, and change verification.
    - `--accent`: Authentic bKash Pink (`#E2136E`) & Neon Pink (`#FF0055`) for bKash wallet actions, expenses, and budget alerts.
    - `--warning`: Amber Gold (`#FBBF24`) for temporary money (`অস্থায়ী টাকা`) and low-change warnings.
  - *Typography & Hierarchy*: Bundled Google Font (`Noto Sans Bengali` / `Hind Siliguri`) paired with tabular numerals so Bengali digits (`০-৯`) and currency amounts (`৳`) never clip or wrap awkwardly.
  - *Clutter-Free, Non-Overlapping Layout Architecture*:
    - Proper `Scaffold` structure handling `WindowInsets.safeDrawing` and `WindowInsets.navigationBars` so top app bars, bottom navigation tabs, and modal sheets never overlap system bars or each other.
    - Adaptive 2-column and 5-column grids using `FlowRow` and responsive column counts so buttons, badges, and Bengali labels breathe cleanly on compact phones and tablets alike.

- **Interactive Feedback & Motion**:
  - Built-in Android tone generator / sound synthesis and `HapticFeedback` on every `+`/`−` stepper tap, modal confirmation, and warning alert (toggleable from the top bar).
  - Non-blocking top toast banners for instant confirmation of transactions and vault updates.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Replacing "আসল টাকা / আসল নোট" with "ভাঙতি"**
  - *Chosen Approach*: Update the bottom navigation tab label to `ভাঙতি`, the Vault tab header to `🇧🇩 বাংলাদেশি ভাঙতি টাকার নোট ইনভেন্টরি (৳১০০০ — ৳১)`, and related vault descriptors to `ভাঙতি`, while preserving `নিজের আসল টাকা` in the Temporary Money (`অস্থায়ী টাকা`) card on the Home screen.
  - *Why*: Directly fulfills your terminology preference so the vault section focuses on change/denomination management ("ভাঙতি") without altering the net-own-money calculation label in Temporary Money.
- **Decision 2: Native Jetpack Compose + Offline Room Database**
  - *Chosen Approach*: Store wallet state, note counts, categories, transactions, debt entries, and temporary money records in local Room SQLite tables, with full JSON Export/Import support via Android's Storage Access Framework.
  - *Why*: Guarantees instant 60fps scrolling, zero WebView touch lag, reliable offline persistence, and responsive layouts that never overlap on small mobile screens.
- **Decision 3: Custom Vector/Canvas Bangladeshi Banknote Cards**
  - *Chosen Approach*: Render each denomination (`৳১০০০` to `৳১`) using dedicated Compose Canvas/Vector artwork matching the exact Bangladeshi banknote color palettes, monuments, and security thread aesthetics.
  - *Why*: Loads instantaneously offline at any resolution (including inside the HD Note Preview zoom modal) without blurry raster bitmaps.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                     MainActivity (Edge-to-Edge Scaffold)                  │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ TopAppBar: Brand, Net Worth Pill (মোট তহবিল), ⚡ নতুন খরচ,            │  │
│  │            🧮 Quick Calc, 📱/🖥️ View Mode, 🔊 Sound, ⚙️ Settings    │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                     │                                     │
│  ┌──────────────────────────────────▼──────────────────────────────────┐  │
│  │            Main Viewport (Tab Mode vs. Full Dashboard Mode)         │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐  │  │
│  │  │ 🏠 হোম ও ভল্ট │ │  💵 ভাঙতি    │ │ ⚡ খরচ ও খাত │ │ 📒 বাকি ও  │  │  │
│  │  │ - Budget Bar │ │ - 10 BDT     │ │ - Category   │ │    টুলস    │  │  │
│  │  │ - bKash Card │ │   Note Cards │ │   Grid       │ │ - Debt     │  │  │
│  │  │ - ভাঙতি Strip│ │ - + / -      │ │ - 1-Tap      │ │   Ledger   │  │  │
│  │  │ - Quick Cats │ │   Steppers   │ │   Monthly    │ │ - bKash    │  │  │
│  │  │ - Recent 3   │ │ - HD Zoom    │ │   Bills      │ │   Fee Calc │  │  │
│  │  │ - অস্থায়ী টাকা│ │   Inspector  │ └──────────────┘ │ - Advisor  │  │  │
│  │  └──────────────┘ └──────────────┘                  └────────────┘  │  │
│  │  ┌───────────────────────────────────────────────────────────────┐  │  │
│  │  │ 📊 ইতিহাস ও চার্ট: Donut Chart, bKash/Cash Split, Filter Feed │  │  │
│  │  └───────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────┬──────────────────────────────────┘  │
│                                     │                                     │
│  ┌──────────────────────────────────▼──────────────────────────────────┐  │
│  │   Interactive Modals: Smart Expense Flow (1 -> 2A / 2B-1..4),       │  │
│  │   Add bKash, Add Cash, bKash Bridge (Cash-Out/In), Note Exchanger,  │  │
│  │   HD Note Preview, Quick Market Calculator, Digital Receipt, Backup │  │
│  └──────────────────────────────────┬──────────────────────────────────┘  │
└─────────────────────────────────────┼─────────────────────────────────────┘
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                  TakaVaultViewModel (StateFlow + Sound/Haptics)           │
│  - Bengali Numeral Formatter & Parser (০-৯ <-> 0-9)                       │
│  - Smart Auto-Tender & Greedy Change Calculation Engine                   │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│               TakaVaultRepository & Room SQLite Database                  │
│  - WalletConfigEntity (bkashBalance, bkashVisible, budget, sound, mode)   │
│  - NoteCountEntity (denom: 1000..1, count)                                │
│  - CategoryEntity, TransactionEntity, DebtEntity, TempMoneyEntity         │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **Wallet Settings & State (`WalletConfigEntity`)**: Stores `bkashBalance`, `bkashVisible`, `monthlyBudget` (`৳২৫,০০০` default), `soundEnabled`, and `layoutMode` (`tabs` vs `full`).
- **ভাঙতি Note Inventory (`NoteCountEntity`)**: Maps each of the 10 BDT denominations (`1000, 500, 200, 100, 50, 20, 10, 5, 2, 1`) to its non-negative integer count in the vault, plus metadata (colors, monument title, dimensions).
- **Expense Categories (`CategoryEntity`)**: Pre-populated with the 8 default Bengali categories (`বাড়ি ভাড়া`, `বিল ও ইউটিলিটি`, `মুদি ও বাজার`, `পোশাক`, `খাবার ও পানীয়`, `অটো / যাতায়াত ভাড়া`, `হঠকারী খরচ`, `বিবিধ`) and extensible with custom user categories.
- **Transactions (`TransactionEntity`)**: Stores `id`, `timestamp`, `type` (`expense` / `inflow`), `method` (`cash` / `bkash`), `categoryId`, `amount`, `note`, serialized `tenderedNotes` & `receivedNotes` maps, `tenderedTotal`, `receivedTotal`, `expectedChange`, and `discrepancy`.
- **Debt Ledger (`DebtEntity`)**: Tracks `receivable` (`আমি পাবো`) and `payable` (`আমাকে দিতে হবে`) entries, automatically created when cash change has a discrepancy or added manually, with 1-tap settlement.
- **Temporary Money (`TempMoneyEntity`)**: Tracks `title`, `amount`, `where` (`cash`, `bkash`, `other`), `timestamp`, and `done` status without mutating actual vault note counts, while computing `নিজের আসল টাকা = মোট তহবিল − চলমান অস্থায়ী টাকা (নগদ/বিকাশ)`.

### Interactive Component & State Mapping
- **Bengali Input & Numeral Engine**: All text fields accept both English (`0-9`) and Bengali (`০-৯`) digits seamlessly and display formatted BDT strings (`৳১,৪৫০`).
- **Smart Expense Multi-Step Validation**:
  - Prevents bKash payments exceeding `bkashBalance` with an inline alert.
  - Prevents cash tender progression if `tenderTotal < billAmount` or if selected notes exceed available vault quantities.
  - Auto-selects optimal tendered notes (`✨ অটো নোট নির্বাচন`) and greedy change breakdown (`⚡ সঠিক ভাংতি অটো-পূরণ`).
- **bKash Bridge & Note Breaker**:
  - *Cash-Out*: Deducts `amount + fee` (`1.85%`, `1.49%`, or `0%`) from bKash and adds selected physical notes to the ভাঙতি vault.
  - *Cash-In*: Deducts selected physical notes from the ভাঙতি vault and credits bKash.
  - *Note Exchanger (`🔄 ভাংতি করুন`)*: Validates that the target smaller notes sum exactly to the selected large note (`৳১০০০`, `৳৫০০`, `৳২০০`, `৳১০০`, `৳৫০`) before swapping counts in the vault.
- **Digital Receipt & JSON Backup**: Renders a high-contrast receipt bitmap (`Canvas` to `Bitmap`) that can be saved/shared as a PNG image, alongside JSON export/import and hard reset controls in Settings.
