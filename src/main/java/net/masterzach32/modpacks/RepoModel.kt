package net.masterzach32.modpacks

import com.google.gson.GsonBuilder
import com.mashape.unirest.http.Unirest
import javafx.scene.image.Image

/*
 * MinecraftModpackInstaller - Created on 6/2/2017
 * Author: Zachary Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zachary Kozar
 * @version 6/2/2017
 */

data class Repo(val name: String, val url: String, val format: Int, val modpacks: Array<Modpack>) {

    override fun toString() = "$name ($url)"

}

data class Modpack(val name: String, val id: String, val creator: String, val desc: String, val versions: Array<ModpackVersion>) : Comparable<Modpack> {

    @Transient var repo: Repo? = null
    @Transient var added: Boolean = false

    fun getLatestVersion() = versions.first()

    fun getIcon() = "${repo?.url?.removeTrailingSlash()}/downloads/$id/icon.png"

    override fun toString() = name

    override fun compareTo(other: Modpack) = name.compareTo(other.name)
}

data class ModpackVersion(val version: String, val numOfMods: Int, val gameVersion: String, val forgeVersion: String) : Comparable<ModpackVersion> {

    var installed: Boolean = false
    var downloaded: Boolean = false

    fun getForgeString(): String {
        if (arrayOf("1.8.9", "1.8.8", "1.8.0", "1.7.10").contains(gameVersion))
            return "$gameVersion-$forgeVersion-$gameVersion"
        else
            return "$gameVersion-$forgeVersion"
    }

    fun getDownloadUrl(repo: String, modpackId: String) = "${repo.removeTrailingSlash()}/downloads/$modpackId/$modpackId-$version.zip"

    fun getForgeUrl(): String {
        return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/${getForgeString()}/forge-${getForgeString()}-installer.jar"
    }

    override fun toString() = "$version - $gameVersion"

    override fun compareTo(other: ModpackVersion): Int {
        return 0
    }
}

enum class ModpackCategory(val type: String, val desc: String) {
    TECHNIC("Technical", "Includes mods like Buildcraft and Thermal Expansion " +
            "that allow players to automate tasks and gameplay."),
    MAGIC("Magic", ""),
    ADVENTURE("Adventure", "")
}

fun forEachModpackVersion(func: (ModpackVersion) -> Unit) {
    Controller.repos.forEach { it.modpacks.forEach { it.versions.forEach(func) } }
}

fun loadRepoFromUrl(url: String): Repo? {
    try {
        val response = Unirest.get("${url.removeTrailingSlash()}/modpacks.json").asString()

        if (response.status != 200)
            return null

        val gson = GsonBuilder().create()

        val repo = gson.fromJson(response.body, Repo::class.java)
        if (repo.format > 0 && repo.format <= config.getInt("format")) {
            repo.modpacks.forEach { it.repo = repo }
            return repo
        }
        return null
    } catch (t: Throwable) {
        println("Error parsing json: ${t.message}")
        return null
    }
}

fun String.removeTrailingSlash(): String {
    return if (this.isNotEmpty() && last() == '/') this.dropLast(1) else this
}