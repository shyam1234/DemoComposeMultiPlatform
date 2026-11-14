#!/usr/bin/env python3
"""Scaffold CMP OTT module sources and Gradle files."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

DOMAIN_PKG = "com.ott.platform"

ANDROID_BLOCK = '''
android {
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
'''.strip()


def write(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")
    print(f"wrote {path.relative_to(ROOT)}")


def kmp_library(
    path: str,
    ns: str,
    *,
    compose: bool = False,
    serialization: bool = False,
    android: bool = True,
    ios: bool = True,
    desktop: bool = True,
    deps_common: list[str] | None = None,
    deps_android: list[str] | None = None,
    deps_ios: list[str] | None = None,
    deps_desktop: list[str] | None = None,
    deps_common_test: list[str] | None = None,
):
    plugins = [
        'alias(libs.plugins.kotlinMultiplatform)',
    ]
    if android:
        plugins.append('alias(libs.plugins.androidLibrary)')
    if compose:
        plugins.append('alias(libs.plugins.composeMultiplatform)')
        plugins.append('alias(libs.plugins.composeCompiler)')
    if serialization:
        plugins.append('alias(libs.plugins.kotlinSerialization)')

    targets = []
    if android:
        targets.append("    androidTarget()")
    if desktop:
        targets.append("    jvm(\"desktop\")")
    if ios:
        targets.append("""    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "{base}"
            isStatic = true
        }
    }""".format(base=path.replace(":", "_").replace("/", "_")))

    common = deps_common or []
    android_deps = deps_android or []
    ios_deps = deps_ios or []
    desktop_deps = deps_desktop or []
    test_deps = deps_common_test or ['implementation(libs.kotlin.test)', 'implementation(libs.kotlinx.coroutines.test)']

    def dep_lines(items: list[str], indent=12) -> str:
        if not items:
            return ""
        pad = " " * indent
        return "\n".join(f"{pad}{d}" for d in items)

    source_sets = f"""
    sourceSets {{
        commonMain.dependencies {{
{dep_lines(common)}
        }}
        commonTest.dependencies {{
{dep_lines(test_deps)}
        }}
"""
    if android:
        source_sets += f"""
        androidMain.dependencies {{
{dep_lines(android_deps)}
        }}
"""
    if ios:
        source_sets += f"""
        iosMain.dependencies {{
{dep_lines(ios_deps)}
        }}
"""
    if desktop:
        source_sets += f"""
        desktopMain.dependencies {{
{dep_lines(desktop_deps)}
        }}
"""
    source_sets += "    }"

    android_cfg = ""
    if android:
        android_cfg = f"""
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
"""

    content = f"""
plugins {{
    {chr(10).join('    ' + p if i else p for i, p in enumerate(plugins))}
}}

kotlin {{
{chr(10).join(targets)}
{source_sets}
}}
{android_cfg}
"""
    # Fix plugins formatting
    content = f"""plugins {{
{chr(10).join(f'    {p}' for p in plugins)}
}}

kotlin {{
{chr(10).join(targets)}
{source_sets}
}}
{android_cfg}
"""
    write(ROOT / path / "build.gradle.kts", content)


# ---------- core:domain ----------
kmp_library(
    "core/domain",
    f"{DOMAIN_PKG}.domain",
    deps_common=[
        "implementation(libs.kotlinx.coroutines.core)",
        "implementation(libs.kotlinx.datetime)",
    ],
)

# ---------- core:logging ----------
kmp_library(
    "core/logging",
    f"{DOMAIN_PKG}.logging",
    deps_common=[],
)

# ---------- core:player-api ----------
kmp_library(
    "core/player-api",
    f"{DOMAIN_PKG}.player",
    deps_common=[
        "implementation(projects.core.domain)",
        "implementation(libs.kotlinx.coroutines.core)",
    ],
)

# ---------- core:network ----------
kmp_library(
    "core/network",
    f"{DOMAIN_PKG}.network",
    serialization=True,
    deps_common=[
        "implementation(projects.core.logging)",
        "implementation(libs.ktor.client.core)",
        "implementation(libs.ktor.client.content.negotiation)",
        "implementation(libs.ktor.client.logging)",
        "implementation(libs.ktor.serialization.kotlinx.json)",
        "implementation(libs.kotlinx.serialization.json)",
        "implementation(libs.kotlinx.coroutines.core)",
    ],
    deps_android=["implementation(libs.ktor.client.okhttp)"],
    deps_ios=["implementation(libs.ktor.client.darwin)"],
    deps_desktop=["implementation(libs.ktor.client.cio)"],
)

# ---------- core:data ----------
kmp_library(
    "core/data",
    f"{DOMAIN_PKG}.data",
    serialization=True,
    deps_common=[
        "implementation(projects.core.domain)",
        "implementation(projects.core.network)",
        "implementation(projects.core.logging)",
        "implementation(libs.kotlinx.coroutines.core)",
        "implementation(libs.kotlinx.serialization.json)",
        "implementation(libs.koin.core)",
    ],
)

# ---------- core:testing ----------
kmp_library(
    "core/testing",
    f"{DOMAIN_PKG}.testing",
    deps_common=[
        "implementation(projects.core.domain)",
        "implementation(projects.core.player.api)",
        "implementation(libs.kotlinx.coroutines.core)",
        "implementation(libs.kotlinx.coroutines.test)",
        "implementation(libs.kotlin.test)",
    ],
)

# ---------- core:designsystem (CMP) ----------
kmp_library(
    "core/designsystem",
    f"{DOMAIN_PKG}.designsystem",
    compose=True,
    deps_common=[
        "implementation(compose.runtime)",
        "implementation(compose.foundation)",
        "implementation(compose.material3)",
        "implementation(compose.ui)",
        "implementation(compose.components.resources)",
        "implementation(compose.components.uiToolingPreview)",
        "implementation(libs.coil.compose)",
    ],
)

# ---------- core:ui ----------
kmp_library(
    "core/ui",
    f"{DOMAIN_PKG}.ui",
    compose=True,
    deps_common=[
        "implementation(projects.core.domain)",
        "implementation(projects.core.designsystem)",
        "implementation(compose.runtime)",
        "implementation(compose.foundation)",
        "implementation(compose.material3)",
        "implementation(compose.ui)",
        "implementation(libs.coil.compose)",
        "implementation(libs.jetbrains.lifecycle.runtime.compose)",
    ],
)

# ---------- core:navigation ----------
kmp_library(
    "core/navigation",
    f"{DOMAIN_PKG}.navigation",
    compose=True,
    serialization=True,
    deps_common=[
        "implementation(projects.core.domain)",
        "implementation(compose.runtime)",
        "implementation(libs.jetbrains.navigation.compose)",
        "implementation(libs.kotlinx.serialization.json)",
    ],
)

# ---------- core:di ----------
kmp_library(
    "core/di",
    f"{DOMAIN_PKG}.di",
    deps_common=[
        "implementation(projects.core.domain)",
        "implementation(projects.core.data)",
        "implementation(projects.core.network)",
        "implementation(projects.core.logging)",
        "implementation(projects.core.player.api)",
        "implementation(libs.koin.core)",
        "implementation(libs.kotlinx.coroutines.core)",
    ],
)

FEATURES = [
    "onboarding",
    "authentication",
    "home",
    "search",
    "details",
    "player",
    "watchlist",
    "continuewatching",
    "profile",
    "settings",
]

for feature in FEATURES:
    # API module - pure Kotlin, no Compose required for most; player api may reference routes only
    kmp_library(
        f"features/{feature}/api",
        f"{DOMAIN_PKG}.feature.{feature}.api",
        compose=False,
        deps_common=[
            "implementation(projects.core.domain)",
            "implementation(projects.core.navigation)",
            "implementation(libs.kotlinx.coroutines.core)",
        ] if feature != "player" else [
            "implementation(projects.core.domain)",
            "implementation(projects.core.navigation)",
            "implementation(projects.core.player.api)",
            "implementation(libs.kotlinx.coroutines.core)",
        ],
    )

print("scaffold gradle modules done (apis)")
