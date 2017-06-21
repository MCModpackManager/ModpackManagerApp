package net.masterzach32.modpacks

import com.jfoenix.controls.JFXButton
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import java.util.*

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
class ModpackBrowseView : VBox() {

    private val modpacks = mutableListOf<ModpackView>()
    private val removed = mutableListOf<ModpackView>()

    fun refresh() {
        modpacks.forEach { it.update() }
    }

    fun reload(repos: List<Repo>) {
        val new = mutableListOf<Modpack>()

        repos.forEach { new.addAll(it.modpacks) }

        modpacks.removeAll(modpacks.filter { !new.contains(it.modpack) })

        new.filter { !modpacks.any { view -> view.modpack == it } }.forEach { modpacks.add(ModpackView(it)) }

        Collections.sort(modpacks)

        children.clear()
        children.addAll(modpacks)
    }

    fun search(query: String) {
        val split = query.split()

        if (split.isEmpty()) {
            children.clear()
            children.addAll(modpacks)
        } else {
            removed.clear()
            split.forEach {
                removed.addAll(modpacks.filter { view ->
                    !(view.modpack.name.toLowerCase().contains(it.toLowerCase()) ||
                            view.modpack.desc.toLowerCase().contains(it.toLowerCase())) &&
                            !removed.contains(view)
                })

            }
            children.removeAll(removed)

            children.addAll(modpacks.filter { !removed.contains(it) && !children.contains(it) })
            Collections.sort(children as ObservableList<ModpackView>)
        }
    }

    private class ModpackView(val modpack: Modpack) : HBox(), Comparable<ModpackView> {

        val addButton = JFXButton("Add")

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

            val title = VBox()
            title.spacing = -5.0
            val name = Label(modpack.name)
            name.font = Font.font(24.0)
            title.children.add(name)
            val author = Label("By ${modpack.creator} - ${modpack.repo?.name}")
            author.font = Font.font(10.0)
            title.children.add(author)

            info.top = title

            val desc = Label("\t${modpack.desc}")
            info.center = desc

            addButton.stylesheets.add("/css/main.css")
            addButton.styleClass.addAll("green-button", "large-font")
            addButton.alignment = Pos.CENTER_RIGHT
            addButton.setOnAction {
                modpack.added = true
                addButton.isDisable = true
            }

            info.bottom = addButton

            val version = Label("Latest Version: ${modpack.getLatestVersion().version} - Minecraft ${modpack.getLatestVersion().gameVersion}")
            info.left = version

            children.add(info)

            stylesheets.add("/css/main.css")
            styleClass.add("bg-white")
        }

        fun update() {
            addButton.isDisable = modpack.added
        }

        override fun compareTo(other: ModpackView): Int {
            return modpack.compareTo(other.modpack)
        }
    }
}