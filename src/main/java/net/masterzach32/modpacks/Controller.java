package net.masterzach32.modpacks;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Controller {

    private ArrayList<Repo> repos = new ArrayList<>();
    private ArrayList<Modpack> downloadedModpacks = new ArrayList<>();
    private Repo defaultRepo;

    private File mcDirectory = getMcDirectory();
    private File cacheDirectory = getCacheDirectory();

    private Paint red = Paint.valueOf("FF2300");
    private Paint green = Paint.valueOf("3fa858");
    private Paint black = Paint.valueOf("4d4d4d");

    public void initialize() {
        defaultRepo = RepoModelKt.loadRepoFromUrl("http://modpacks.masterzach32.net/repo/");
        if (defaultRepo != null)
            repos.add(defaultRepo);
        repoList.setItems(FXCollections.observableArrayList(repos));

        mcDirectoryField.setText(mcDirectory.getAbsolutePath());
        mcDirectoryField.setOnAction((event) -> {
            File temp = new File(mcDirectoryField.getText());
            if (temp.exists() && temp.isDirectory()) {
                mcDirectory = temp;
                mcDirectoryField.setUnFocusColor(black);
                mcDirectoryField.setFocusColor(green);
            } else {
                mcDirectoryField.setUnFocusColor(red);
                mcDirectoryField.setFocusColor(red);
            }
        });

        cacheDirectoryField.setText(cacheDirectory.getAbsolutePath());
        cacheDirectoryField.setOnAction((event) -> {
            File temp = new File(cacheDirectoryField.getText());
            if (temp.exists() && temp.isDirectory()) {
                mcDirectory = temp;
                cacheDirectoryField.setUnFocusColor(black);
                cacheDirectoryField.setFocusColor(green);
            } else {
                cacheDirectoryField.setUnFocusColor(red);
                cacheDirectoryField.setFocusColor(red);
            }
        });
    }

    @FXML
    private BorderPane pane;
    @FXML
    private JFXListView repoList;
    @FXML
    private JFXTextField mcDirectoryField;
    @FXML
    private JFXTextField cacheDirectoryField;
    @FXML
    private JFXTextField repoInput;
    @FXML
    private VBox browse;

    private boolean isChooserOpen = false;

    public void chooseMcFolder(ActionEvent actionEvent) {
        File file = chooseFolder();
        if (file != null)
            mcDirectoryField.setText(file.getAbsolutePath());
    }

    public void chooseCacheFolder(ActionEvent actionEvent) {
        File file = chooseFolder();
        if (file != null)
            cacheDirectoryField.setText(file.getAbsolutePath());
    }

    private File chooseFolder() {
        if (!isChooserOpen) {
            isChooserOpen = true;
            DirectoryChooser d = new DirectoryChooser();
            File file = d.showDialog(null);
            isChooserOpen = false;
            return file;
        }
        return null;
    }

    public void showAboutDialog(ActionEvent actionEvent) {
        System.out.println("About");
    }

    public void showCreditDialog(ActionEvent actionEvent) {
        System.out.println("Credits");
    }

    public void showWebsite(ActionEvent actionEvent) {
        System.out.println("Website");
    }

    public void addRepo(ActionEvent actionEvent) {
        String repoUrl = repoInput.getText();

        Repo repo = RepoModelKt.loadRepoFromUrl(repoUrl);
        addRepo(repo);
    }

    public void removeRepo(ActionEvent actionEvent) {
        Repo repo = (Repo) repoList.getSelectionModel().getSelectedItem();
        removeRepo(repo);
    }

    private void addRepo(Repo repo) {
        if (repo != null && !repos.stream().anyMatch((it) -> it.getUrl().equals(repo.getUrl())))
            repos.add(repo);
        repoList.setItems(FXCollections.observableArrayList(repos));
    }

    private void removeRepo(Repo repo) {
        if (repo != null && repo.getUrl() != defaultRepo.getUrl())
            repos.remove(repo);
        repoList.setItems(FXCollections.observableArrayList(repos));
    }

    private File getMcDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("windows"))
            return new File(home + "\\AppData\\Roaming\\.minecraft");
        else if (os.contains("mac"))
            return new File(home + "/Library/Application Support/minecraft");
        else
            return new File("");
    }

    private File getCacheDirectory() {
        File cache = new File(System.getProperty("user.home") + "/modpackmanager");
        if (!cache.exists())
            cache.mkdir();
        return cache;
    }

    public void debug(Event event) {
        browse.getChildren().clear();
        List<Modpack> modpacks = new ArrayList<>();

        for (Repo repo : repos) {
            modpacks.addAll(Arrays.asList(repo.getModpacks()));
        }

        Collections.sort(modpacks);

        for (Modpack modpack : modpacks) {
            browse.getChildren().add(new Label(modpack.toString()));
        }
    }
}
