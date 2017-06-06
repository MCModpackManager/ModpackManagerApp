package net.masterzach32.modpacks

import com.typesafe.config.ConfigFactory
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class Main : Application() {

    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/fxml/main.fxml"))
        stage.title = "Modpack Manager"
        stage.scene = Scene(root, 700.0, 550.0)
        stage.isResizable = false
        stage.icons.add(Image("/images/icon.png"))
        stage.show()
    }
}

val config = ConfigFactory.load()

fun main(args: Array<String>) {
    Application.launch(*args)
}
