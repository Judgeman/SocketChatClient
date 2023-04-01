package de.judgeman.WebSocketChatClient.ViewControllers;

import de.judgeman.WebSocketChatClient.Model.SettingEntry;
import de.judgeman.WebSocketChatClient.Services.SettingService;
import de.judgeman.WebSocketChatClient.Services.ViewService;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Created by Paul Richter on Thu 03/09/2020
 */
@Controller
public class MainViewController extends ViewController {

    @Autowired
    private ViewService viewService;

    @Autowired
    private SettingService settingService;

    @FXML
    private Pane contentPane;
    @FXML
    private Pane glassPane;
    @FXML
    private Pane dialogOverLayer;

    public Pane getGlassPane() {
        return glassPane;
    }

    public void setGlassPane(Pane glassPane) {
        this.glassPane = glassPane;
    }

    public Pane getContentPane() {
        return contentPane;
    }

    public Pane getDialogOverLayer() {
        return dialogOverLayer;
    }

    public void setDialogOverLayer(Pane dialogOverLayer) {
        this.dialogOverLayer = dialogOverLayer;
    }

    public void showInitialView() {
        String currentUsername = settingService.loadSetting(SettingService.CURRENT_LOGIN_NAME);
        if (currentUsername != null && !currentUsername.isEmpty()) {
            showChatView(currentUsername);
            return;
        }

        showUserSetupView();
    }

    private void showChatView(String currentUsername) {
        ChatViewController chatViewController = (ChatViewController) viewService.showView(ViewService.FILE_PATH_CHAT_VIEW);
        chatViewController.setUserName(currentUsername);
    }

    private void showUserSetupView() {
        viewService.showView(ViewService.FILE_PATH_USER_SETUP);
    }

    public void showNewView(Parent root) {
        removeLastVisibleView();
        contentPane.getChildren().add(root);
    }

    private void removeLastVisibleView() {
        contentPane.getChildren().clear();
    }
}