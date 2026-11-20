package com.ott.platform.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageResolverTest {

    private val multiImageList = listOf(
        MediaImage(ImageTypes.PORTRAIT, "https://cdn/portrait.png", 729, 1080),
        MediaImage(ImageTypes.LANDSCAPE, "https://cdn/landscape.png", 1920, 1080),
        MediaImage(ImageTypes.TILE_ARTWORK, "https://cdn/tile.png", 1920, 1080),
        MediaImage(ImageTypes.LOGO, "https://cdn/logo.png", 80, 80),
    )

    @Test
    fun `resolves exact image type for landscape and portrait slots`() {
        assertEquals("https://cdn/landscape.png", multiImageList.resolveUrl(ImageSlot.LandscapeCard))
        assertEquals("https://cdn/portrait.png", multiImageList.resolveUrl(ImageSlot.PortraitCard))
        assertEquals("https://cdn/logo.png", multiImageList.resolveUrl(ImageSlot.Logo))
    }

    @Test
    fun `automatically matches newly introduced cms imagetype by rail display type`() {
        val withFutureSquare = multiImageList + MediaImage("Square", "https://cdn/square.png", 800, 800)
        val squareSlot = ImageSlot.forRail(displayType = "Square")
        assertEquals("https://cdn/square.png", withFutureSquare.resolveUrl(squareSlot))

        val withFutureBillboard = multiImageList + MediaImage("Billboard_4K", "https://cdn/billboard.png", 3840, 2160)
        val billboardSlot = ImageSlot.forRail(displayType = "billboard-4k")
        assertEquals("https://cdn/billboard.png", withFutureBillboard.resolveUrl(billboardSlot))
    }

    @Test
    fun `falls back gracefully when preferred landscape image is missing`() {
        val portraitOnly = listOf(
            MediaImage(ImageTypes.PORTRAIT, "https://cdn/only-portrait.png", 1000, 1500),
        )
        assertEquals("https://cdn/only-portrait.png", portraitOnly.resolveUrl(ImageSlot.LandscapeCard))
        assertNull(portraitOnly.resolveUrl(ImageSlot.Logo))
    }

    @Test
    fun `maps cms rail display and content types to card styles`() {
        assertEquals(ShelfCardStyle.Portrait, ShelfCardStyle.fromCms("Portrait", "top_10"))
        assertEquals(ShelfCardStyle.Landscape, ShelfCardStyle.fromCms("Landscape", "new"))
        assertEquals(ShelfCardStyle.Banner, ShelfCardStyle.fromCms("Landscape", "editorial"))
        assertEquals(ShelfCardStyle.Square, ShelfCardStyle.fromCms("Square", "generic"))
        assertEquals(RailContentType.Top10, RailContentType.fromCms("top_10"))
        assertEquals(RailContentType.Editorial, RailContentType.fromCms("editorial"))
    }
}
