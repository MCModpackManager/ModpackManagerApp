package net.masterzach32.modpacks

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXProgressBar
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import java.util.*

/*
 * MinecraftModpackManager - Created on 6/9/2017
 * Author: Zachary Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zachary Kozar
 * @version 6/9/2017
 */
class ModpackManageView : VBox() {

    private val modpacks = mutableListOf<ModpackView>()
    private val added = mutableListOf<ModpackView>()

    fun refresh() {
        modpacks.forEach {
            if (it.modpack.added && !added.contains(it))
                added.add(it)
            else if (!it.modpack.added && added.contains(it))
                added.remove(it)
        }

        added.forEach { it.update() }

        Collections.sort(added)

        children.clear()
        children.addAll(added)
    }

    fun reload(repos: List<Repo>) {
        val modpacks = mutableListOf<Modpack>()

        repos.forEach { modpacks.addAll(it.modpacks) }

        modpacks.filter { modpack -> !this.modpacks.any { it.modpack == modpack } }
                .forEach { this.modpacks.add(ModpackView(it)) }

        refresh()
    }

    private class ModpackView(val modpack: Modpack) : HBox(), Comparable<ModpackView> {

        val playButton = JFXButton("Install")
        val removeButton = JFXButton("Remove")
        val versionSelector = JFXComboBox<ModpackVersion>()
        val progressBar = JFXProgressBar()
        val progressDesc = Label()

        init {
            prefWidth = 685.0
            prefHeight = 100.0
            spacing = 10.0
            //border = Border(BorderStroke(Paint.valueOf("4d4d4d"), BorderStrokeStyle.SOLID, CornerRadii(0.0), BorderStroke.DEFAULT_WIDTHS))

            val image = ImageView(modpack.getIcon())
            image.fitWidth = 150.0
            image.fitHeight = 150.0
            children.add(image)

            val info = BorderPane()

            val content = VBox()
            val title = VBox()
            title.spacing = -5.0
            val name = Label(modpack.name)
            name.font = Font.font(24.0)
            title.children.add(name)
            val author = Label("By ${modpack.creator} - ${modpack.repo?.name}")
            author.font = Font.font(10.0)
            title.children.add(author)
            content.children.add(title)

            val buttonBox = HBox()
            buttonBox.styleClass.add("large-font")
            buttonBox.spacing = 15.0
            // buttons
            playButton.prefWidth = 90.0
            playButton.styleClass.add("green-button")
            playButton.setOnAction {
                Thread {
                    playButton.isDisable = true
                    removeButton.isDisable = true
                    progressBar.isVisible = true
                    progressDesc.isVisible = true
                    installModpack(versionSelector.value, modpack, progressDesc.textProperty()) {
                        progressBar.progress = it
                    }
                    progressBar.progress = 0.0
                    progressBar.isVisible = false
                    progressDesc.isVisible = false
                    playButton.isDisable = false
                    removeButton.isDisable = false
                    Platform.runLater { (this.parent as ModpackManageView).refresh() }
                }.start()
            }
            removeButton.prefWidth = 90.0
            removeButton.styleClass.add("green-button")
            removeButton.setOnAction {
                modpack.added = false
                (this.parent as ModpackManageView).refresh()
            }
            buttonBox.children.addAll(playButton, removeButton)
            buttonBox.alignment = Pos.BOTTOM_RIGHT
            content.children.add(buttonBox)
            // modpack version selector
            versionSelector.styleClass.add("large-font")
            versionSelector.items.addAll(modpack.versions)
            versionSelector.selectionModel.select(0)
            versionSelector.setOnAction {
                playButton.isDisable = versionSelector.selectionModel.selectedItem.installed
            }
            content.children.add(versionSelector)
            // progress bar
            progressBar.progress = 0.0
            progressBar.isVisible = false
            content.children.add(progressBar)
            progressDesc.isVisible = false
            content.children.addAll(progressDesc)
            children.add(content)

            versionSelector.minWidth = progressBar.width
        }

        fun update() {

        }

        override fun compareTo(other: ModpackView): Int {
            return modpack.compareTo(other.modpack)
        }
    }
}