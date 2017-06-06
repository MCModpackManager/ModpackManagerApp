package net.masterzach32.modpacks

import com.google.gson.GsonBuilder
import com.mashape.unirest.http.Unirest

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

data class Repo(val name: String, val url: String,  val format: Int, val modpacks: Array<Modpack>) {

    override fun toString() = "$name ($url)"
}

data class Modpack(val name: String, val id: String, val creator: String, val versions: Array<ModpackVersion>) : Comparable<Modpack> {

    fun getLatestVersion() = versions.first()

    override fun toString() = name

    override fun compareTo(other: Modpack) = name.compareTo(other.name)
}

data class ModpackVersion(val version: String, val numOfMods: Int, val mcVersion: String, val forgeVersion: String) : Comparable<ModpackVersion> {

    fun getModpackUrl(repo: String, modpack: String) = "${repo.removeTrailingSlash()}/downloads/$modpack/$modpack-$version.zip"

    fun getForgeUrl(): String {
        if (arrayOf("1.8.9", "1.8.8", "1.8.0", "1.7.10").contains(mcVersion))
            return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" +
                    "$mcVersion-$forgeVersion-$mcVersion/forge-$mcVersion-$forgeVersion-$mcVersion-installer.jar"
        return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" +
                "$mcVersion-$forgeVersion/forge-$mcVersion-$forgeVersion-installer.jar"
    }

    override fun toString() = "$version - $mcVersion"

    override fun compareTo(other: ModpackVersion): Int {
        return 0
    }
}

fun loadRepoFromUrl(url: String): Repo? {
    val response = Unirest.get("${url.removeTrailingSlash()}/modpacks.json").asString()

    if (response.status != 200)
        return null

    val gson = GsonBuilder().create()

    try {
        val repo = gson.fromJson(response.body, Repo::class.java)
        if (repo.format > 0 && repo.format <= config.getInt("format"))
            return repo
        return null
    } catch (t: Throwable) {
        println("Error parsing json: ${t.message}")
        return null
    }
}

fun String.removeTrailingSlash(): String {
    return if (this.isNotEmpty() && last() == '/') this.dropLast(1) else this
}

fun main(args: Array<String>) {
    println(loadRepoFromUrl("http://modpacks.masterzach32.net/repo/"))
}