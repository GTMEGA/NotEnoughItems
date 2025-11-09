plugins {
    antlr
    id("com.falsepattern.fpgradle-mc") version ("3.1.0")
}

group = "codechicken"

minecraft_fp {
    java {
        compatibility = jvmDowngrader
        version = JavaVersion.VERSION_17
        jvmDowngraderShade = projectIsLgpl21PlusCompatible
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

val genGrammar = tasks.generateGrammarSource
genGrammar {
    maxHeapSize = "128m"
    arguments.addAll(listOf("-package", "codechicken.nei.search", "-visitor", "-no-listener"))
    outputDirectory = project
        .layout
        .buildDirectory
        .dir("generated/sources/search/antlr/codechicken/nei/search")
        .map { it.asFile }
        .get()
}

tasks.compileJava {
    dependsOn(genGrammar)
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
    api("codechicken:codechickencore-mc1.7.10:1.4.7-mega:dev")
    shadowImplementation("org.apache.commons:commons-csv:1.10.0")
    compileOnly(deobfModrinth("forge-essentials:7.5.1"))

    antlr("org.antlr:antlr4:4.13.2n")
    shadowImplementation("org.antlr:antlr4-runtime:4.13.2")


    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.+")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.lwjgl.lwjgl:lwjgl:2.9.3");
}



tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}