package de.judgeman.WebSocketChatClient.ViewControllers;

import de.judgeman.WebSocketChatClient.Services.LanguageService;
import de.judgeman.WebSocketChatClient.Services.SettingService;
import de.judgeman.WebSocketChatClient.Services.ViewService;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Created by Paul Richter on Fri 24/03/2023
 */
@Controller
public class UserSetupViewController extends ViewController {

    @Autowired
    private ViewService viewService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private SettingService settingService;

    @FXML
    private TextField nameTextField;

    @FXML
    private void onClickSingInBtn() {
        if (!nameIsValid()) {
            viewService.showInformationDialog(languageService.getLocalizationText("userSetupViewNameIsNotValidTitle"),
                                              languageService.getLocalizationText("userSetupViewNameIsNotValidText"));
            return;
        }

        settingService.saveSetting(SettingService.CURRENT_LOGIN_NAME, nameTextField.getText());
        ChatViewController chatViewController = (ChatViewController) viewService.showView(ViewService.FILE_PATH_CHAT_VIEW);
        chatViewController.setUserName(nameTextField.getText());
    }

    private boolean nameIsValid() {
        return !nameTextField.getText().isEmpty();
    }
}
