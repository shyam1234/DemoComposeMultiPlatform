# Play-CMS API Definition, Architecture & Job Roles

**OttPlatform** integrates the **Play-CMS** backend (`https://cms.play-cms.com`) as its single source of truth for authentication, dynamic navigation menus, screen layouts, content rails, rich media metadata, and artwork.

All legacy `iptv-org` endpoints and M3U parsers have been removed.

---

## 1. End-to-End API Flow

```mermaid
sequenceDiagram
    participant App as OttApp / Shared Core
    participant Auth as CmsAuthManager
    participant API as Play-CMS API
    participant DB as SQLDelight / localStorage

    App->>Auth: getAccessToken()
    Auth->>API: POST /v1/login?devicetype=atv_ott
    API-->>Auth: { access, refresh }
    App->>API: GET /v1/menu?devicetype=androidTV&lang_code=en (Bearer)
    API-->>App: menuGroup -> menuL1[] -> layoutId
    App->>DB: Cache dynamic menu (6h TTL)
    App->>API: GET /v1/layouts/{layoutId} (Bearer)
    API-->>App: theme_url + sortrail[]
    par Concurrent Rail Hydration
        App->>API: GET /v2/rails?id={railId_1}
        App->>API: GET /v2/rails?id={railId_N}
    end
    API-->>App: rail_display_type, rail_content_type, existing_asset[]
    App->>DB: Persist layout, rail metadata, and assets
    App-->>App: Render dynamic tabs, Hero, and Rail shelves
```

If any authenticated `GET` receives **`401 Unauthorized`**, `CmsCatalogRemoteDataSource` automatically invalidates the cached token, invokes `CmsAuthManager.getAccessToken(forceRefresh = true)`, and retries the request once transparently.

---

## 2. API Endpoint Definitions

### 2.1 Internal Login (Token Acquisition)

- **Method & URL:** `POST https://cms.play-cms.com/v1/login?devicetype=atv_ott`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "username": "dGF0YQ==",
    "password": "U2FsdGVkX1974CQPT3eKjNplPPsojssg7HDC99WkzZU="
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "refresh": "<jwt-refresh-token>",
    "access": "<jwt-access-token>"
  }
  ```
- **App Behavior:** Called lazily and synchronized via a `Mutex` in `CmsAuthManager` so concurrent rail requests share a single login handshake. Credentials are never prompted to the user.

---

### 2.2 Dynamic Menu

- **Method & URL:** `GET https://cms.play-cms.com/v1/menu?devicetype=androidTV&lang_code=en`
- **Headers:** `Authorization: Bearer <access_token>`
- **Key Response Fields:**
  - `results[].publish_now`: only published menu groups (`!= 0`) are included.
  - `results[].menuL1[]`: top-level navigation tabs:
    - `menuL1Id`, `menuL1Title`
    - `menuL1DeeplinkUrl`: accepts both full deeplink URI (`"http://layouts/6af4302c6368a4cce6863ca6"`) and raw layout ID (`"6af4302c6368a4cce6863ca6"`).
    - `defaultImageIcon`, `focusedImageIcon`, `selectedImageIcon`: optional CMS icon URLs.
    - `menuL2[]`: optional nested sub-menu items (rendered automatically as a secondary pill bar in `HomeScreen` when present).
- **App Behavior:** `AppMenuViewModel` fetches menus via `GetMenuUseCase` and drives both `OttTopMenu` (TV / wide Web) and `OttBottomMenu` (Mobile / tablet) dynamically, alongside built-in `Search` and `Library` (Watchlist) utilities.

---

### 2.3 Layout

- **Method & URL:** `GET https://cms.play-cms.com/v1/layouts/{layoutId}`
- **Headers:** `Authorization: Bearer <access_token>`
- **Key Response Fields:**
  - `_id`, `name`, `layout_type`
  - `theme.theme_url`: optional atmospheric backdrop URL rendered softly behind the top of the page.
  - `sortrail` (primary) / `rail_id` (fallback): ordered list of rail IDs to render vertically.
- **App Behavior:** `CmsCatalogRepository.getLayoutFeed(layoutId)` caches the layout structure in `cms_layout` and resolves all rails concurrently in `sortrail` order.

---

### 2.4 Rail & Assets

- **Method & URL:** `GET https://cms.play-cms.com/v2/rails?id={railId}`
- **Headers:** `Authorization: Bearer <access_token>`
- **Key Rail Fields:**
  - `_id`, `name`
  - `rail_display_type`: e.g. `"Landscape"`, `"Portrait"`, or future types like `"Square"`, `"Banner"`.
  - `rail_content_type`: e.g. `"new"`, `"generic"`, `"top_10"`, `"mosaic"`, `"grid"`, `"editorial"`.
  - `rail_type`: `"Manual Multi Category"`, `"Multi Filter"`, etc.
  - `existing_asset.existing_asset[]`: list of assets in the rail.
- **Key Asset Fields & Quirks Handled:**
  - `asset_id` / `show_id` / `content_id`: resolved in priority order as stable `ContentId`.
  - `title` / `name` / `regional_title`: `name` is polymorphic across rail types (`""` string in `Manual Multi Category`, `{}` object in `Multi Filter`), handled safely via `JsonElement?`.
  - `is_hd` / `dolby` / `Dolby`: polymorphic (`0` int vs `"0"` string), handled via `JsonPrimitive?`.
  - `images[]`: list of `{ imagetype, imgurl, imageurl, imagewidth, imageheight, lang_code }`.
  - `playback_urls.hls_url` / `hls_url` / `dash_url` / `trailer_urls.hls_url`: stream URLs for Media3 / AVPlayer / `hls.js`.
  - `long_description` / `synopsis` / `short_description`, `genres[]`, `cast[]`, `director`, `runtime` (seconds → minutes), `release_date`, `parental_rating`, `imdb_rating`, `tags[]`.

---

## 3. Extensibility Design (Future Image Types & Dynamic Menus)

### 3.1 Adding or Receiving New `imagetype` Values
Instead of hardcoding two image fields (`posterUrl` / `thumbnailUrl`), every asset stores the full list of `MediaImage(type, url, width, height, langCode)` and resolves artwork through `ImageSlot` (`core/domain/.../ImageResolver.kt`):

1. **Zero-code automatic matching:** `ImageSlot.forRail(displayType, contentType)` dynamically prepends the rail's `rail_display_type` (case-insensitive, ignoring `_`/`-`/spaces) before falling back to orientation-aware dimensions (`width >= height` vs `height >= width`).
   - Example: If CMS adds `rail_display_type = "Square"` with asset images having `imagetype = "Square"`, `ChannelCard` automatically selects the `Square` artwork and `1:1` dimensions (`ShelfCardStyle.Square`) with **no parser or repository changes**.
2. **Adding a new custom presentation style:** Add one entry to `ShelfCardStyle` + `OttDimens` + `when (style)` branch in `ChannelCard.kt`.

### 3.2 Adding New Menus (`menuL1` & `menuL2`)
- Adding new `menuL1` tabs (e.g., `Movies`, `TV Shows`, `Kids`, `Sports`) in CMS immediately renders new tabs in `OttTopMenu` (horizontally scrollable) and `OttBottomMenu` (adaptive / horizontally scrollable for > 5 tabs) and routes them to `OttRoutes.layout(layoutId)`.
- Adding `menuL2` child items under any `menuL1` item renders a secondary pill bar (`home_submenu`) in `HomeScreen` to switch layouts in-place.

---

## 4. Offline Persistence (`:core:database`)

- **Native (Android, Android TV, iOS, Desktop):** SQLDelight schema v3 (`OttDatabase.sq` + `1.sqm` + `2.sqm`) stores:
  - `cms_menu` (serialized `AppMenuItem` list + timestamp)
  - `cms_layout` (layout ID, name, `theme_url`, ordered `rail_ids_json`)
  - `categories` (rail ID, title, `rail_display_type`, `rail_content_type`)
  - `channels` (composite primary key `(category_id, id)` so assets appearing in multiple rails are preserved per rail without overwriting, plus `metadata_json` for images/genres/synopsis/cast)
  - `watchlist` (user-saved assets with full `metadata_json`)
- **Web (`wasmJs`):** `WebCatalogLocalDataSource` and `WebWatchlistLocalDataSource` mirror the same contracts using browser `localStorage` (`ott_web_cms_*_v2`).
- **TTL & Fallback:** 6-hour freshness window; when offline or when a remote call fails, cached menu, layout, rails, and search results are served automatically.

---

## 5. Job Roles & Ownership Matrix

| Job Role | Primary Responsibilities | Modules / Artifacts Owned |
|----------|--------------------------|---------------------------|
| **CMS / Content Operations** | Configure `menuL1`/`menuL2` groups, publish layouts (`sortrail`, `theme_url`), curate rails (`rail_display_type`, `rail_content_type`), upload artwork (`Portrait`, `Landscape`, `tile_artwork`, `Logo`, future types), and attach `playback_urls`. | Play-CMS Admin Console (`cms.play-cms.com`) |
| **Backend / API Platform Engineer** | Maintain `/v1/login`, `/v1/menu`, `/v1/layouts/{id}`, `/v2/rails` endpoints, JWT expiry/refresh contracts, CORS headers (`Access-Control-Allow-Origin`), and CDN image resizing (`/v1/imageresize`). | Play-CMS Backend Services |
| **KMP / Core Data Engineer** | Own `CmsAuthManager` (token lifecycle + 401 retry), `CmsDtos` (polymorphic JSON resilience), `CmsCatalogMapper`, `CmsCatalogRepository` (TTL + offline fallback), and SQLDelight migrations (`OttDatabase.sq`, `.sqm`). | `core/domain`, `core/data`, `core/database`, `core/network`, `core/di` |
| **CMP / UI Engineer** | Build and maintain dynamic `OttTopMenu`/`OttBottomMenu`, `HeroBannerCarousel`, `ContentShelfRow` (`Top10`, `Grid`, `Editorial`, `Portrait`, `Landscape`, `Square`, `Banner`), `ChannelCard`, `DetailsScreen`, `SearchScreen`, and TV D-pad focus UX. | `core/designsystem`, `core/ui`, `core/navigation`, `features/*`, `apps/shared` |
| **Native Player / Platform Engineer** | Implement and tune `PlayerEngine` bindings (Media3 ExoPlayer on Android/TV, AVPlayer on iOS/macOS/tvOS, HTML5 `<video>` + `hls.js` on Web) and handle unplayable/coming-soon states cleanly. | `core/player-api`, `platform/android`, `platform/ios`, `platform/web`, `platform/tvos` |
| **QA / Release Engineer** | Maintain `commonTest` / `desktopTest` / `testDebugUnitTest` coverage, verify live CMS hydration + offline fallback on physical devices (iPhone, Android Phone/Tablet, Android TV, Web), and validate release artifacts. | `core/testing`, `*Test.kt`, `.github/workflows/deploy-web.yml` |
