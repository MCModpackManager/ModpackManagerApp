package net.masterzach32.modpacks

import javafx.application.Platform
import javafx.beans.property.StringProperty
import net.lingala.zip4j.core.ZipFile
import java.net.URL
import java.io.FileOutputStream
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URISyntaxException
import java.awt.Desktop
import java.net.URI


/*
 * MinecraftModpackManager - Created on 6/8/17
 * Author: Zachary Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zachary Kozar
 * @version 6/8/17
 */

fun String.split(): Array<String> {
    val temp = split(' ')
    val split = mutableListOf<String>()

    var i = 0
    while (i < temp.size) {
        if (temp[i].contains('"') && temp[i][0] == '"') {
            var str = temp[i].substring(1)
            while (i + 1 < temp.size && !temp[i+1].contains('"')) {
                str += " ${temp[i+1]}"
                i++
            }
            if (i + 1 < temp.size && temp[i+1].contains('"') && temp[i+1].last() == '"')
                str += " ${temp[i+1].substring(0, temp[i+1].lastIndex)}"
            split.add(str)
            i++
        } else
            split.add(temp[i])
        i++
    }

    if (split.size == 1 && split[0].isEmpty())
        split.clear()

    return split.toTypedArray()
}

fun downloadModpack(modpackVersion: ModpackVersion, modpack: Modpack, temp: File, progressUpdate: (Double) -> Unit) {
    if (!temp.exists())
        downloadFile(modpackVersion.getDownloadUrl(modpack.repo!!.url, modpack.id), temp.absolutePath, progressUpdate)
    modpackVersion.downloaded = true
}

fun installModpack(modpackVersion: ModpackVersion, modpack: Modpack, label: StringProperty, progressUpdate: (Double) -> Unit): Boolean {
    val temp = File("${Controller.cacheDirectory}/${modpack.name}-${modpackVersion.version}.zip")
    if (!modpackVersion.downloaded) {
        Platform.runLater { label.set("Downloading modpack files") }
        downloadModpack(modpackVersion, modpack, temp, progressUpdate)
    }
    if (!modpackVersion.installed) {
        Platform.runLater {
            label.set("Cleaning minecraft files")
            progressUpdate.invoke(-1.0)
        }
        forEachModpackVersion { it.installed = false }

        File(Controller.mcDirectory!!.absolutePath + "/mods").deleteRecursively()

        Platform.runLater {
            label.set("Extracting modpack files")
        }
        try {
            ZipFile(temp).extractAll(Controller.mcDirectory!!.absolutePath)
        } catch (t: Throwable) {
            t.printStackTrace()
            return false
        }

        Platform.runLater { label.set("Checking for correct Forge installation") }
        val forgeInstaller = Controller.cacheDirectory!!.absolutePath + "/${modpackVersion.getForgeString()}.jar"

        if (!isForgeInstalled(modpackVersion.forgeVersion)) {
            Platform.runLater { label.set("Downloading Forge version ${modpackVersion.forgeVersion}") }
            downloadFile(modpackVersion.getForgeUrl(), forgeInstaller, progressUpdate)

            try {
                Platform.runLater {
                    label.set("Installing Forge")
                    progressUpdate.invoke(-1.0)
                }
                val pb = ProcessBuilder("java", "-jar", forgeInstaller)
                pb.directory(File(Controller.cacheDirectory!!.absolutePath))
                val errorCode = pb.start().waitFor()
                if (errorCode != 0) {
                    Platform.runLater {
                        label.set("Could not install Forge. (error code: $errorCode)")
                        progressUpdate.invoke(1.0)
                    }
                    Thread.sleep(5000)
                    return false
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                return false
            }
        }

        Platform.runLater {
            label.set("Finished! Restart your minecraft launcher and click play!")
            progressUpdate.invoke(1.0)
        }
        Thread.sleep(5000)
        modpackVersion.installed = true
        return true
    }
    return false
}

fun downloadFile(urlStr: String, file: String, progressUpdate: (Double) -> Unit) {
    val url = URL(urlStr)
    val httpConnection = url.openConnection() as HttpURLConnection
    val bis = BufferedInputStream(httpConnection.inputStream)
    val fis = FileOutputStream(file)
    val buffer = ByteArray(1024)
    val fileSize: Long = httpConnection.contentLength.toLong()
    var downloaded: Long = 0
    var count = 0
    while (true) {
        count = bis.read(buffer, 0, 1024)
        if (count == -1)
            break
        fis.write(buffer, 0, count)
        downloaded += count
        Platform.runLater { progressUpdate.invoke(downloaded/fileSize.toDouble()) }
    }

    fis.close()
    bis.close()
}

fun isForgeInstalled(version: String): Boolean {
    val profiles = File(Controller.mcDirectory!!.absolutePath + "/versions").listFiles()
    return profiles.filter { it.name.contains(version) }.isNotEmpty()
}

fun openWebpage(uri: URI) {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

fun openWebpage(url: String) {
    try {
        openWebpage(URL(url).toURI())
    } catch (e: URISyntaxException) {
        e.printStackTrace()
    }

}