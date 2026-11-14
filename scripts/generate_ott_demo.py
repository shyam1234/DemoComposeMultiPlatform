#!/usr/bin/env python3
"""Generate OttPlatform CMP demo sources (IPTV-org + Android OTT UI + tests)."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def w(rel: str, content: str):
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")
    print("wrote", rel)


KMP_LIB = '''plugins {{
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary){extra_plugins}
}}

kotlin {{
    androidTarget()
    jvm("desktop")
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {{ target ->
        target.binaries.framework {{
            baseName = "{base}"
            isStatic = true
        }}
    }}

    sourceSets {{
        commonMain.dependencies {{
{common_deps}
        }}
        commonTest.dependencies {{
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
{test_deps}
        }}
        androidMain.dependencies {{
{android_deps}
        }}
        desktopMain.dependencies {{
{desktop_deps}
        }}
        iosMain.dependencies {{
{ios_deps}
        }}
    }}
}}

android {{
    namespace = "{ns}"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {{
        minSdk = libs.versions.android.minSdk.get().toInt()
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }}
}}
'''

CMP_LIB = '''plugins {{
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler){extra_plugins}
}}

kotlin {{
    androidTarget()
    jvm("desktop")
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {{ target ->
        target.binaries.framework {{
            baseName = "{base}"
            isStatic = true
        }}
    }}

    sourceSets {{
        commonMain.dependencies {{
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
{common_deps}
        }}
        commonTest.dependencies {{
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
{test_deps}
        }}
        androidMain.dependencies {{
{android_deps}
        }}
        desktopMain.dependencies {{
            implementation(compose.desktop.currentOs)
{desktop_deps}
        }}
        iosMain.dependencies {{
{ios_deps}
        }}
    }}
}}

android {{
    namespace = "{ns}"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {{
        minSdk = libs.versions.android.minSdk.get().toInt()
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }}
}}
'''


def indent(lines: list[str], n: int = 12) -> str:
    pad = " " * n
    return "\n".join(f"{pad}{x}" for x in lines if x)


def kmp(path, ns, base, common, android=None, desktop=None, ios=None, test=None, extra_plugins=""):
    w(f"{path}/build.gradle.kts", KMP_LIB.format(
        ns=ns, base=base, extra_plugins=extra_plugins,
        common_deps=indent(common),
        android_deps=indent(android or []),
        desktop_deps=indent(desktop or []),
        ios_deps=indent(ios or []),
        test_deps=indent(test or []),
    ))


def cmp(path, ns, base, common, android=None, desktop=None, ios=None, test=None, extra_plugins=""):
    w(f"{path}/build.gradle.kts", CMP_LIB.format(
        ns=ns, base=base, extra_plugins=extra_plugins,
        common_deps=indent(common),
        android_deps=indent(android or []),
        desktop_deps=indent(desktop or []),
        ios_deps=indent(ios or []),
        test_deps=indent(test or []),
    ))


# ---------------- domain sources ----------------
w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/model/ContentModels.kt", '''
package com.ott.platform.domain.model

data class ContentId(val value: String)

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
)

data class Channel(
    val id: ContentId,
    val title: String,
    val logoUrl: String?,
    val streamUrl: String,
    val categoryId: String,
    val categoryName: String,
    val groupTitle: String = categoryName,
    val isGeoBlocked: Boolean = false,
    val isNot247: Boolean = false,
    val quality: String? = null,
    val tvgId: String? = null,
)

data class ContentShelf(
    val id: String,
    val title: String,
    val items: List<Channel>,
)

data class HomeFeed(
    val hero: Channel?,
    val shelves: List<ContentShelf>,
)

sealed class OttResult<out T> {
    data class Success<T>(val data: T) : OttResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : OttResult<Nothing>()
    data object Loading : OttResult<Nothing>()
}
''')

w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/repository/CatalogRepository.kt", '''
package com.ott.platform.domain.repository

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult

interface CatalogRepository {
    suspend fun getCategories(): OttResult<List<Category>>
    suspend fun getHomeFeed(limitPerShelf: Int = 24): OttResult<HomeFeed>
    suspend fun getChannelsByCategory(categoryId: String, limit: Int = 40): OttResult<List<Channel>>
    suspend fun search(query: String, limit: Int = 40): OttResult<List<Channel>>
    suspend fun getChannel(id: ContentId): OttResult<Channel>
}
''')

w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/repository/WatchlistRepository.kt", '''
package com.ott.platform.domain.repository

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    val watchlist: Flow<List<Channel>>
    suspend fun add(channel: Channel)
    suspend fun remove(id: ContentId)
    suspend fun isInWatchlist(id: ContentId): Boolean
    suspend fun toggle(channel: Channel): Boolean
}
''')

w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/usecase/GetHomeFeedUseCase.kt", '''
package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class GetHomeFeedUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(limitPerShelf: Int = 24): OttResult<HomeFeed> {
        require(limitPerShelf > 0) { "limitPerShelf must be > 0" }
        return catalogRepository.getHomeFeed(limitPerShelf)
    }
}
''')

w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/usecase/SearchChannelsUseCase.kt", '''
package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class SearchChannelsUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(query: String, limit: Int = 40): OttResult<List<Channel>> {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return OttResult.Success(emptyList())
        }
        return catalogRepository.search(trimmed, limit)
    }
}
''')

w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/usecase/GetChannelDetailsUseCase.kt", '''
package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class GetChannelDetailsUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(id: ContentId): OttResult<Channel> =
        catalogRepository.getChannel(id)
}
''')

w("core/domain/src/commonMain/kotlin/com/ott/platform/domain/usecase/ToggleWatchlistUseCase.kt", '''
package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.repository.WatchlistRepository

class ToggleWatchlistUseCase(
    private val watchlistRepository: WatchlistRepository,
) {
    suspend operator fun invoke(channel: Channel): Boolean =
        watchlistRepository.toggle(channel)
}
''')

w("core/domain/src/commonTest/kotlin/com/ott/platform/domain/usecase/SearchChannelsUseCaseTest.kt", '''
package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SearchChannelsUseCaseTest {

    private val sample = Channel(
        id = ContentId("news-1"),
        title = "World News Live",
        logoUrl = null,
        streamUrl = "https://example.com/live.m3u8",
        categoryId = "news",
        categoryName = "News",
    )

    private val repo = object : CatalogRepository {
        override suspend fun getCategories() = OttResult.Success(emptyList())
        override suspend fun getHomeFeed(limitPerShelf: Int) = OttResult.Success(HomeFeed(null, emptyList()))
        override suspend fun getChannelsByCategory(categoryId: String, limit: Int) = OttResult.Success(emptyList())
        override suspend fun search(query: String, limit: Int): OttResult<List<Channel>> {
            val hits = listOf(sample).filter { it.title.contains(query, ignoreCase = true) }
            return OttResult.Success(hits.take(limit))
        }
        override suspend fun getChannel(id: ContentId) = OttResult.Success(sample)
    }

    private val useCase = SearchChannelsUseCase(repo)

    @Test
    fun `short query returns empty without hitting meaningful search`() = runTest {
        val result = useCase("a")
        assertTrue(result is OttResult.Success)
        assertEquals(emptyList(), (result as OttResult.Success).data)
    }

    @Test
    fun `query finds matching channel title`() = runTest {
        val result = useCase("world")
        assertTrue(result is OttResult.Success)
        assertEquals(1, (result as OttResult.Success).data.size)
        assertEquals("World News Live", result.data.first().title)
    }
}
''')

w("core/domain/src/commonTest/kotlin/com/ott/platform/domain/usecase/GetHomeFeedUseCaseTest.kt", '''
package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetHomeFeedUseCaseTest {

    private val channel = Channel(
        id = ContentId("1"),
        title = "Demo",
        logoUrl = null,
        streamUrl = "https://example.com/a.m3u8",
        categoryId = "news",
        categoryName = "News",
    )

    @Test
    fun `rejects non positive shelf limit`() = runTest {
        val useCase = GetHomeFeedUseCase(object : CatalogRepository {
            override suspend fun getCategories() = error("unused")
            override suspend fun getHomeFeed(limitPerShelf: Int) = error("unused")
            override suspend fun getChannelsByCategory(categoryId: String, limit: Int) = error("unused")
            override suspend fun search(query: String, limit: Int) = error("unused")
            override suspend fun getChannel(id: ContentId) = error("unused")
        })
        assertFailsWith<IllegalArgumentException> { useCase(0) }
    }

    @Test
    fun `returns repository home feed`() = runTest {
        val feed = HomeFeed(channel, listOf(ContentShelf("news", "News", listOf(channel))))
        val useCase = GetHomeFeedUseCase(object : CatalogRepository {
            override suspend fun getCategories() = OttResult.Success(emptyList())
            override suspend fun getHomeFeed(limitPerShelf: Int) = OttResult.Success(feed)
            override suspend fun getChannelsByCategory(categoryId: String, limit: Int) = OttResult.Success(emptyList())
            override suspend fun search(query: String, limit: Int) = OttResult.Success(emptyList())
            override suspend fun getChannel(id: ContentId) = OttResult.Success(channel)
        })
        val result = useCase(12)
        assertTrue(result is OttResult.Success)
        assertEquals("Demo", (result as OttResult.Success).data.hero?.title)
        assertEquals(1, result.data.shelves.size)
    }
}
''')

print("domain sources OK")
print("ROOT", ROOT)
