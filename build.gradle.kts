plugins {
	id("net.fabricmc.fabric-loom-remap").version("1.14-SNAPSHOT")
	id("maven-publish")
    id("org.jetbrains.kotlin.jvm").version("2.2.21")
}

loom {
    runs {
        register("clientMacuguita") {
            client()
            name = "Minecraft Client macuguita"
            programArgs.add("--username=macuguita")
            programArgs.add("--uuid=0e56050b-ee27-478a-a345-d2b384919081")
        }
        configureEach {
            if (name == "client") {
                programArgs.add("--username=Ladybrine")
                programArgs.add("--uuid=5d66606c-949c-47ce-ba4c-a1b9339ba3c8")
            }
        }
    }
    if (project.file("src/main/resources/${BuildConfig.modId}.classtweaker").exists()) {
        accessWidenerPath = project.file("src/main/resources/${BuildConfig.modId}.classtweaker")
    }
}

sourceSets {
    main {
        resources.srcDir("src/main/generated")
        resources.exclude(".cache")
    }
}

version = BuildConfig.modVersion
group = BuildConfig.mavenGroup

base {
    archivesName.set(BuildConfig.modId)
}

repositories {
    val exclusiveRepos = listOf(
        Triple("Shedaniel", "https://maven.shedaniel.me/", listOf("me.shedaniel.cloth")),
        Triple("TerraformersMC", "https://maven.terraformersmc.com/", listOf("com.terraformersmc", "dev.emi")),
        Triple("Modrinth", "https://api.modrinth.com/maven", listOf("maven.modrinth")),
        Triple("ParchmentMC", "https://maven.parchmentmc.org", listOf("org.parchmentmc.data")),
        Triple("Architectury", "https://maven.architectury.dev/", listOf("dev.architectury")),
    )

    exclusiveRepos.forEach { (name, url, groups) ->
        exclusiveContent {
            forRepository {
                maven {
                    this.name = name
                    setUrl(url)
                }
            }
            if (groups.isNotEmpty())
                filter {
                    groups.forEach { includeGroupByRegex(it) }
                }
        }
    }
    mavenCentral()
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.minecraftVersion}")
	mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${BuildConfig.minecraftVersion}:${BuildConfig.parchmentMappings}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.loaderVersion}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${BuildConfig.fabricVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${BuildConfig.fabricKotlinVersion}")

    modImplementation("maven.modrinth:macu-lib:${BuildConfig.maculibVersion}-fabric"){
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation("com.cobblemon:fabric:1.7.1+1.21.1"){
        exclude("net.fabricmc.fabric-api")
    }
}

tasks.register<net.fabricmc.loom.task.FabricModJsonV1Task>("genModJson") {
    outputFile = project.file("src/main/resources/fabric.mod.json")

    json {
        modId = BuildConfig.modId
        version = BuildConfig.modVersion
        name = BuildConfig.modName
        description = BuildConfig.description
        author("macuguita") {
            contactInformation = mapOf(
                "discord" to "macuguita"
            )
        }
        contactInformation.set(mapOf(
            "homepage" to "https://macuguita.com",
        ))
        licenses = listOf(BuildConfig.license)
        if (project.file("src/main/resources/assets/${BuildConfig.modId}/icon.png").exists()) {
            icon("assets/${BuildConfig.modId}/icon.png")
        }
        if (project.file("src/main/resources/${BuildConfig.modId}.mixins.json").exists()) {
            mixin("${BuildConfig.modId}.mixins.json")
        }
        if (project.file("src/main/resources/${BuildConfig.modId}.classtweaker").exists()) {
            accessWidener = "${BuildConfig.modId}.classtweaker"
        }
        environment = "*"

        entrypoint("main", "com.macuguita.daisy_smp.common.DaisySMPTweaks", "kotlin")
        entrypoint("client", "com.macuguita.daisy_smp.client.ClientEntrypoint", "kotlin")
        entrypoint("server", "com.macuguita.daisy_smp.server.ServerEntrypoint", "kotlin")

        depends("fabricloader", ">=${BuildConfig.loaderVersion}")
        depends("minecraft", BuildConfig.minecraftVersionRange)
        depends("java", ">=21")
        depends("fabric-api", "*")
        depends("macu_lib", ">=${BuildConfig.maculibVersion}")
        depends("cobblemon", "*")
        depends("fabric-language-kotlin", "*")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}


tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.processResources {
    dependsOn(tasks.named("genModJson"))
}

tasks.named("sourcesJar") {
    dependsOn(tasks.named("genModJson"))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${BuildConfig.modId}"}
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = BuildConfig.modId
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
