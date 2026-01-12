pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    // --- PHẦN MỚI THÊM VÀO ĐỂ SỬA LỖI ---
    resolutionStrategy {
        eachPlugin {
            // Ép buộc dùng đúng phiên bản 2.8.5 để tránh lỗi fileCollection
            if (requested.id.id == "androidx.navigation.safeargs.kotlin") {
                useVersion("2.8.5")
            }
        }
    }
    // ------------------------------------
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SmartSpend2"
include(":app")