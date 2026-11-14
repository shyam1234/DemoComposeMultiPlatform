# IPTV Free Data Source

The demo catalog uses the public **[iptv-org](https://github.com/iptv-org/iptv)** project. No API key and no paid backend.

## Endpoints used

| Resource | URL | Role in app |
|----------|-----|-------------|
| Categories JSON | `https://iptv-org.github.io/api/categories.json` | Category names / metadata |
| Category playlists | `https://iptv-org.github.io/iptv/categories/{id}.m3u` | Home shelves + search index |

Home shelves load these categories in parallel:

`news`, `movies`, `series`, `sports`, `music`, `documentary`, `entertainment`, `kids`

## Android OTT presentation

- Dark cinematic theme (`OttTheme`) inspired by lean-back / streaming TV apps
- Hero banner + horizontal content shelves
- Phone: bottom navigation (Home / Search / Watchlist)
- Android TV app: larger cards/hero (`isTv = true`), Leanback launcher

## Important disclosures

- Streams are **third-party**. Many are geo-blocked, offline, or require special headers.
- Geo-blocked entries are filtered from home shelves when marked in the playlist title/group.
- This is a **demo**, not a production IPTV service and does not redistribute video itself.
- Prefer category M3U slices over full `channels.json` / `streams.json` (multi‑MB) for cold start.

## Code map

| Piece | Module |
|-------|--------|
| Endpoints | `core/data/.../IptvOrgEndpoints.kt` |
| M3U parser | `core/data/.../M3uPlaylistParser.kt` |
| Remote source | `core/data/.../IptvCatalogRemoteDataSource.kt` |
| Repository | `core/data/.../IptvCatalogRepository.kt` |
