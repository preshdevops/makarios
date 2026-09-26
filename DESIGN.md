# Makarios — Design System & Craft Specification

*“Create. Declare. Share.”*  
*Gospel. Tech. Precious.*

---

## 1. Aesthetic Identity & Signature Concept

Makarios is a sacred, typographic-led Christian affirmation app. Its signature feel is **liturgical restraint meeting modern editorial craft**. It is not a sterile wellness tracker, nor is it a noisy social app with vanity metrics or gamified streaks.

### Core Design Rules
- **Photo-Forward Sacred Sanctuary**: Every affirmation is paired with breathtaking, emotionally resonant sacred photography (cinematic dawn light, mountain ridges, still waters, sunbeams). Rich dark scrims let Cormorant Garamond typography float with high contrast and sacred emotional weight, avoiding sterile text-only card lists.
- **Visual Mood Exploration**: Categorical discovery is driven by photographic mood tiles (`MoodTile`) that invite the soul to pause and select their spiritual season (*Peace*, *Strength*, *Stillness*, *Courage*, *Joy*).
- **Mandatory Scripture Grounding**: Every affirmation — whether user-authored or curated — **must** display its grounding Bible verse text and reference beneath it. No affirmation exists in isolation from scripture.
- **No AI Slop Patterns**: No purple/indigo gradient accents, no arbitrary dark-mode glows, no emoji used as icons or bullet points, no kicker eyebrows stacked over titles, no colored top/left borders on cards.
- **Native Android Integration**: True system share sheet (`ACTION_SEND`) and one-tap Glance Home Screen widget pinning (`requestPinAppWidget`).
- **Creation First**: The primary action is authoring personal declarations and matching them to God's word, then formatting them into beautiful typographic artifacts for social sharing, widgets, and wallpapers.

---

## 2. Color Palette & Semantic Tokens

| Token | Hex / Value | Semantic Role |
|---|---|---|
| `Porcelain` | `#FBF9F5` | Primary warm ground — warm alabaster linen |
| `PorcelainWarm` | `#F3EFE8` | Soft almond/oat neutral secondary background, indicator pills |
| `Surface` | `#FFFFFF` | Pure crisp white card surface |
| `SurfaceMuted` | `#F6F3EE` | Muted input and search fields |
| `Espresso` | `#2C2622` | Deep warm charcoal-umber ink for primary text and dark elements |
| `EspressoLight` | `#453E38` | Secondary display text |
| `Stone` | `#787069` | Scripture text, body copy — warm driftwood |
| `StoneMuted` | `#A39B93` | Captions, metadata, search placeholder |
| `Terracotta` | `#A85842` | Primary brand accent — quiet earthy terracotta clay |
| `TerracottaLight` | `#F7EBE7` | Soft terracotta blush wash |
| `Sage` | `#607768` | Secondary accent — muted eucalyptus sage |
| `SageLight` | `#F0F4F1` | Soft sage mist wash |
| `AmberGold` | `#C49B45` | Sacred warm amber/ochre accent |
| `Border` | `#ECE7DF` | Delicate warm hairline border |
| `BorderSubtle` | `Color(0x0A2C2622)` | 4% alpha translucent border (~0.5dp) replacing harsh 1dp borders |
| `AtmosphericGradient` | `#342E2B` → `#24201D` → `#171513` | Candlelit twilight sanctuary gradient |

---

## 3. Typography Hierarchy

| Style | Font Family | Weight | Size / Line Height | Role |
|---|---|---|---|---|
| `displayLarge` | Cormorant Garamond | SemiBold | 27sp / 35sp | Screen titles, hero prompt |
| `headlineMedium` | Cormorant Garamond | Medium | 23sp / 32sp | Affirmation declarations |
| `titleLarge` | Cormorant Garamond | Medium | 20sp / 26sp | Wordmark, section headers |
| `titleMedium` | Work Sans | SemiBold | 15sp / 22sp | Card titles, tab labels |
| `bodyLarge` | Work Sans | Normal | 14.5sp / 23sp | Body descriptions |
| `bodyMedium` | Cormorant Garamond | Italic Normal | 15.5sp / 23sp | Scripture verses |
| `labelLarge` | Work Sans | SemiBold | 14sp / 18sp | Button text, action pills |
| `labelSmall` | Work Sans | SemiBold | 10.5sp / 14sp (track 1.6sp) | Scripture references, category tags |

---

## 4. App Screen Map

```
App Shell (MainActivity)
 ├── 0. Home (HomeScreen)
 │    ├── Time-aware Header ("Good morning, Precious")
 │    ├── CreatorPromptCard ("What do you need to hear today?")
 │    ├── Featured Declaration Hero (AtmosphericGradient + Scripture)
 │    ├── Category Filter Pills (11 categories, text-only)
 │    ├── Curated Declarations Feed (AffirmationCard with mandatory verse container)
 │    ├── WidgetPreviewCard (4×2 Android Glance widget preview)
 │    └── Terracotta FAB (Quick link to Create)
 ├── 1. Library (LibraryScreen)
 │    ├── Multi-attribute live search (declaration, scripture, reference, category)
 │    ├── Filter Pills
 │    ├── "Life Seasons & Themes" 2-column overview grid
 │    ├── Filtered Declarations Feed
 │    └── "Author a Declaration" invitation banner
 ├── 2. Create (CreateScreen — Affirmation Creator & Design Studio)
 │    ├── Stage 1: WRITE (First-person declaration input + curated seeds + tone picker)
 │    ├── Stage 2: MATCH (AI/algorithmic scripture matching + grounding explanation note)
 │    └── Stage 3: DESIGN (Format selector: 9:16 Story, 1:1 Square, 4:5 Status, 16:9 X, Wallpaper; 4 styles; live canvas preview; export to social/gallery/wallpaper)
 ├── 3. Saved (SavedScreen)
 │    ├── Tab 1: Declarations (Saved affirmations with AffirmationCard)
 │    ├── Tab 2: Wallpapers (2-column phone wallpaper gallery)
 │    └── Tab 3: Personal (User-authored declarations with create CTA)
 ├── 4. Profile (ProfileScreen)
 │    ├── Profile identity
 │    ├── Home & Lock Screen Widgets (source selector, Glance sync status)
 │    ├── Gentle Notifications (Dawn 06:30 AM, Midday 12:30 PM, Evening 08:30 PM)
 │    ├── Current Spiritual Focus (Philippians 4:7, Hebrews 13:6, Matthew 11:28, etc.)
 │    └── Makarios brand stamp ("Gospel. Tech. Precious.")
 ├── Overlay: Widget Studio (WidgetStudioScreen / ExploreScreen)
 │    ├── Surface toggle: Home Screen vs Lock Screen
 │    ├── Size selector: Small (2×2), Medium (4×2), Large (4×4)
 │    ├── Interactive phone canvas: status bar, dock icons, live widget render
 │    ├── Lock Screen simulation: clock (09:41), date, translucent widget
 │    ├── Feed source: Declaration of the Day, Saved Only, Specific Category
 │    ├── Refresh schedule: Every Dawn (06:30 AM), Twice Daily, Every 4 Hours
 │    └── "Add Widget to Home Screen" action and instructional guidance
 ├── Overlay: Welcome Journey (OnboardingScreen)
 │    ├── Step 1: The Invitation ("Speak truth over your life", Cormorant Garamond typography, hero mockup)
 │    ├── Step 2: Encouragement Areas (multi-select chip grid: Peace, Identity, Calling, etc.)
 │    ├── Step 3: Aesthetic Choice (Espresso Plum, Warm Porcelain, Terracotta Sunset, Eucalyptus Sage)
 │    └── Step 4: First Declaration (Inaugural declaration card grounded in Philippians 4:6–7)
 ├── Overlay: Makarios+ Membership (SubscriptionScreen)
 │    ├── Feature breakdown: unlimited authoring, complete category library, 4K exports, widget themes
 │    ├── Billing plan selector: Annual ($39.99/yr, save 35%) vs Monthly ($4.99/mo)
 │    ├── 7-day free trial CTA & Google Play Store cancellation terms
 │    └── Restore purchases & legal disclosures (Terms of Service, Privacy Policy)
 └── Fullscreen Overlay (AffirmationDetailScreen)
      ├── Atmospheric plum-espresso gradient
      ├── Category badge
      ├── Large Cormorant Garamond declaration
      ├── Mandatory Scripture Container (Italic Cormorant Garamond verse + tracked reference)
      ├── Personal confession card
      └── Design Studio & Wallpaper CTA + Widget & Reminder shortcuts
```

---

## 5. Craft Floor Checklist

- [x] No emoji used as icons or bullet points (replaced with Material Icons or typographic labels)
- [x] Every single affirmation displays grounding scripture text + reference
- [x] No fake counts or hardcoded numbers (dynamic reactive counts used)
- [x] No kicker eyebrow badges stacked above headings
- [x] No colored top or left card borders
- [x] Text contrast clears WCAG AA across all surfaces
- [x] All 5 navigation tabs wired and functional
- [x] Personal declarations automatically persist to repository upon creation
