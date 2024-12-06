plugins {
    id("fpgradle-minecraft") version ("0.8.3")
}

group = "codechicken"

minecraft_fp {
    mod {
        modid = "NotEnoughItems"
        name = "Not Enough Items"
        rootPkg = "$group.nei"
    }

    shadow {
        relocate = true
    }

    core {
        coreModClass = "asm.NEICorePlugin"
        accessTransformerFile = "nei_at.cfg"
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
    exclusive(mega(), "codechicken")
}

dependencies {
    implementation("codechicken:codechickencore-mc1.7.10:1.4.0-mega:dev")
    shadowImplementation("org.apache.commons:commons-csv:1.9.0")
}