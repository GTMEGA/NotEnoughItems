plugins {
    id("com.falsepattern.fpgradle-mc") version ("1.1.4")
}

group = "codechicken"

minecraft_fp {
    java {
        compatibility = jabel
    }

    mod {
        modid = "NotEnoughItems"
        name = "NotEnoughItems"
        rootPkg = "$group.nei"
    }

    api {
        packages = listOf("api")
    }

    core {
        coreModClass = "asm.NEICorePlugin"
        accessTransformerFile = "nei_at.cfg"
        containsMixinsAndOrCoreModOnly = true
    }

    mixin {
        hasMixinDeps = true
    }

    shadow {
        minimize = true
        relocate = true
    }

    tokens {
        tokenClass = "Tags"
    }

    publish {
        maven {
            repoUrl = "https://mvn.falsepattern.com/gtmega_releases"
            repoName = "mega"
            artifact = "notenoughitems-mc1.7.10"
        }
    }
}

repositories {
    exclusive(mavenpattern(), "com.falsepattern")
    exclusive(mega(), "codechicken") {
        includeModule("mega", "blendtronic-mc1.7.10")
    }
    modrinthEX()
}

dependencies {
    devOnlyNonPublishable("mega:blendtronic-mc1.7.10:1.9.0")
    api("codechicken:codechickencore-mc1.7.10:1.4.2-mega:dev")
    shadowImplementation("org.apache.commons:commons-csv:1.10.0")
    compileOnly(deobfModrinth("forge-essentials:7.5.1"))
}
