package de.judgeman.WebSocketChatClient.ViewControllers;

import de.judgeman.WebSocketChatClient.HelperClasses.ViewRootAndControllerPair;
import de.judgeman.WebSocketChatClient.Interfaces.WebSocketResponseHandler;
import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Services.LanguageService;
import de.judgeman.WebSocketChatClient.Services.LogService;
import de.judgeman.WebSocketChatClient.Services.ViewService;
import de.judgeman.WebSocketChatClient.Services.WebSocketService;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import de.judgeman.WebSocketChatClient.ViewControllers.Other.MessageLayoutViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Controller
public class ChatViewController extends ViewController implements WebSocketResponseHandler {

    private final Logger logger = LogService.getLogger(this.getClass());

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ViewService viewService;
    @Autowired
    private LanguageService languageService;

    private WebSocketStompClient stompClient;
    private StompSession session;

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField nameOfFriendTextField;
    @FXML
    private TextField messageTextField;
    @FXML
    private Button connectionButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private VBox messageVBox;

    @FXML
    private void connect() {
        if (session != null) {
            logger.info("Already connected");
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),languageService.getLocalizationText("connectionAlreadyExists"));
            return;
        }

        String name = nameTextField.getText();
        String nameOfFriend = nameOfFriendTextField.getText();

        if (name.isEmpty() || nameOfFriend.isEmpty()) {
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),languageService.getLocalizationText("textFieldsMustBeFilledOut"));
            return;
        }

        logger.info("Try to connect");
        if (stompClient == null) {
            stompClient = webSocketService.createNewStompClient();
        }
        session = webSocketService.connect(stompClient, name, this);

        if (session != null && session.isConnected()) {
            changeConnectionControls(false);
            clearChat();
        }
    }

    private void clearChat() {
        messageVBox.getChildren().clear();
    }

    @FXML
    private void disconnectButtonClicked() {
        disconnect();
        showSystemMessage(languageService.getLocalizationText("connectionDisconnected"));
    }

    private void disconnect() {
        webSocketService.disconnect(session);
        session = null;

        changeConnectionControls(true);
    }

    private void changeConnectionControls(boolean enable) {
        nameTextField.setDisable(!enable);
        nameOfFriendTextField.setDisable(!enable);
        connectionButton.setDisable(!enable);
        disconnectButton.setDisable(enable);
    }

    @Override
    public void handleNewMessage(Message message) {
        String pathOfFXML = getRightMessageFXMLPath(message);

        ViewRootAndControllerPair pair = createMessageViewAndController(pathOfFXML);
        Platform.runLater(() -> {
            ((MessageLayoutViewController)pair.getViewController()).setMessage(message);
            messageVBox.getChildren().add(pair.getRoot());
        });
    }

    @Override
    public void afterConnectionEstablished() {
        showSystemMessage(languageService.getLocalizationText("connectionEstablished"));
    }

    private String getRightMessageFXMLPath(Message message) {
        if (message.getSender() != null && message.getSender().equals(nameTextField.getText())) {
            return ViewService.FILE_PATH_MESSAGE_LAYOUT_OWN;
        }

        return ViewService.FILE_PATH_MESSAGE_LAYOUT_OTHER;
    }

    @Override
    public void handleError(Throwable throwable) {
        showSystemMessage(String.format(languageService.getLocalizationText("errorWithDetails"), throwable.getMessage()));
        disconnect();
    }

    private void showSystemMessage(String messageText) {
        ViewRootAndControllerPair pair = createMessageViewAndController(ViewService.FILE_PATH_MESSAGE_LAYOUT_SYSTEM);

        Platform.runLater(() -> {
            Message message = new Message();
            message.setSender(languageService.getLocalizationText("system"));
            message.setReceiver(nameTextField.getText());
            message.setText(messageText);

            ((MessageLayoutViewController) pair.getViewController()).setMessage(message);
            messageVBox.getChildren().add(pair.getRoot());
        });
    }

    private ViewRootAndControllerPair createMessageViewAndController(String fxmlPath) {
        return viewService.getRootAndViewControllerFromFXML(fxmlPath);
    }

    @FXML
    private void sendMessage() {
        boolean messageSent = sendMessage(session);

        if (messageSent) {
            messageTextField.clear();
        }
    }

    private boolean sendMessage(StompSession session) {
        if (session == null) {
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),languageService.getLocalizationText("noActiveConnection"));
            logger.info("session is null");
            return false;
        }

        if (messageTextField.getText().isEmpty()) {
            logger.info("TextField is empty - ignore");
            return false;
        }

        try {
            webSocketService.sendMessage(session, nameTextField.getText(), nameOfFriendTextField.getText(), messageTextField.getText());
            return true;
        } catch (Exception ex) {
            handleError(ex);
        }

        return false;
    }
}
