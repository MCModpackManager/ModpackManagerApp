package net.masterzach32.modpacks

import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.*
import javafx.stage.DirectoryChooser

import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.stream.Collectors

class Controller {

    private val downloadedModpacks = mutableListOf<Modpack>()
    private var defaultRepo: Repo? = null

    private val red = Paint.valueOf("FF2300")
    private val green = Paint.valueOf("3fa858")
    private val black = Paint.valueOf("4d4d4d")

    companion object {
        val repos = mutableListOf<Repo>()
        var mcDirectory: File? = null
        var cacheDirectory: File? = null
    }

    fun initialize() {
        defaultRepo = loadRepoFromUrl("http://modpacks.masterzach32.net/repo/")
        if (defaultRepo == null)
            defaultRepo = Repo("Default Repository", "Offline", 1, arrayOf<Modpack>())
        repos.add(defaultRepo!!)
        repoList?.items = FXCollections.observableArrayList(repos)
        browseView!!.reload(repos)
        manageView!!.reload(repos)

        mcDirectory = getMcDirectory()
        cacheDirectory = getCacheDirectory()

        mcDirectoryField!!.text = mcDirectory?.absolutePath
        mcDirectoryField!!.setOnAction { checkDirectoryExists(mcDirectoryField!!) }

        cacheDirectoryField!!.text = cacheDirectory?.absolutePath
        cacheDirectoryField!!.setOnAction { checkDirectoryExists(cacheDirectoryField!!) }
    }

    val checkDirectoryExists = fun(field: JFXTextField) {
        val temp = File(field.text)
        if (temp.exists() && temp.isDirectory) {
            mcDirectory = temp
            field.unFocusColor = black
            field.focusColor = green
        } else {
            field.unFocusColor = red
            field.focusColor = red
        }
    }

    @FXML
    private var pane: BorderPane? = null
    @FXML
    private var repoList: JFXListView<*>? = null
    @FXML
    private var mcDirectoryField: JFXTextField? = null
    @FXML
    private var cacheDirectoryField: JFXTextField? = null
    @FXML
    private var repoInput: JFXTextField? = null
    @FXML
    private var browseSearchBar: JFXTextField? = null
    @FXML
    private var browseView: ModpackBrowseView? = null
    @FXML
    private var manageView: ModpackManageView? = null

    private var isChooserOpen = false
    private var isBrowseOpen = false

    fun chooseMcFolder(e: ActionEvent) {
        val file = chooseFolder()
        if (file != null)
            mcDirectoryField!!.text = file.absolutePath
        mcDirectory = file
    }

    fun chooseCacheFolder(e: ActionEvent) {
        val file = chooseFolder()
        if (file != null)
            cacheDirectoryField!!.text = file.absolutePath
        cacheDirectory = file
    }

    private fun chooseFolder(): File? {
        if (!isChooserOpen) {
            isChooserOpen = true
            val d = DirectoryChooser()
            val file = d.showDialog(null)
            isChooserOpen = false
            return file
        }
        return null
    }

    fun showAboutDialog(e: ActionEvent) {
        println("About")
    }

    fun showCreditDialog(e: ActionEvent) {
        println("Credits")
    }

    fun showWebsite(e: ActionEvent) {
        println("Website")
    }

    fun addRepo(e: ActionEvent) {
        val repoUrl = repoInput!!.text

        val repo = loadRepoFromUrl(repoUrl)

        if (repo != null && !repos.any { it.url == repo.url })
            repos.add(repo)
        repoList!!.items = FXCollections.observableArrayList(repos)

        browseView!!.reload(repos)
        manageView!!.reload(repos)
        browseView!!.search(browseSearchBar!!.text)
    }

    fun removeRepo(e: ActionEvent) {
        val repo = repoList!!.selectionModel.selectedItem as Repo

        if (repo != null && repo.url !== defaultRepo!!.url)
            repos.remove(repo)
        repoList!!.items = FXCollections.observableArrayList(repos)

        browseView!!.reload(repos)
        manageView!!.reload(repos)
        browseView!!.search(browseSearchBar!!.text)
    }

    private fun getMcDirectory(): File {
        val os = System.getProperty("os.name").toLowerCase()
        val home = System.getProperty("user.home")
        if (os.contains("windows"))
            return File("$home\\AppData\\Roaming\\.minecraft")
        else if (os.contains("mac"))
            return File(home + "/Library/Application Support/minecraft")
        else
            return File("")
    }

    private fun getCacheDirectory(): File {
        val os = System.getProperty("os.name").toLowerCase()
        val home = System.getProperty("user.home")
        val cache: File
        if (os.contains("windows"))
            cache = File("$home\\AppData\\Roaming\\modpackmanager")
        else if (os.contains("mac"))
            cache =  File(home + "/Library/Application Support/modpackmanager")
        else
            cache = File("")
        if (!cache.exists())
            cache.mkdir()
        return cache
    }

    fun refreshViewI(e: Event) {
        Platform.runLater { browseView!!.refresh() }
    }

    fun refreshViewD(e: Event) {
        Platform.runLater { manageView!!.refresh() }
    }

    fun updateBrowseList(e: KeyEvent?) {
        val query = browseSearchBar!!.text
        Platform.runLater { browseView!!.search(query) }
    }
}
