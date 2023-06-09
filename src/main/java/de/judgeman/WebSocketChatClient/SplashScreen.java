package de.judgeman.WebSocketChatClient;

import de.judgeman.WebSocketChatClient.Services.LanguageService;
import de.judgeman.WebSocketChatClient.Services.ViewService;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Paul Richter on Mon 30/03/2020
 */
public class SplashScreen extends Preloader {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        stage.initStyle(StageStyle.UNDECORATED);

        URL viewUrl = getClass().getResource(ViewService.FILE_PATH_SPLASH_SCREEN);
        URL cssUrl = getClass().getResource(ViewService.FILE_PATH_DEFAULT_STYLE_CSS);
        FXMLLoader fxmlLoader = new FXMLLoader(viewUrl);
        fxmlLoader.setResources(ResourceBundle.getBundle(LanguageService.LOCALIZATION_BUNDLE_NAME, LanguageService.DEFAULT_LANGUAGE));

        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, ViewService.DEFAULT_WIDTH_SPLASH_SCREEN, ViewService.DEFAULT_HEIGHT_SPLASH_SCREEN);
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}
