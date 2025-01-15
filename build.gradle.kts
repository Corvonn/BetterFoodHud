plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}


val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "de.corvonn"
version = providers.environmentVariable("VERSION").getOrElse("1.0.4")

labyMod {
    defaultPackageName = "de.corvonn.betterFoodHud" //change this to your main package name (used by all modules)

    minecraft {
        registerVersion(versions.toTypedArray()) {

            accessWidener.set(file("./game-runner/src/${this.sourceSetName}/resources/betterfoodhud-${this.versionId}.accesswidener"))

            runs {
                getByName("client") {
                    // When the property is set to true, you can log in with a Minecraft account
                    // devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "betterfoodhud"
        displayName = "Better Food HUD"
        author = "Corvonn"
        description = "Provides some helpful features to the HUD related to food."
        minecraftVersion = "1.8.9<1.21.1"
        version = rootProject.version.toString()
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    group = rootProject.group
    version = rootProject.version
}