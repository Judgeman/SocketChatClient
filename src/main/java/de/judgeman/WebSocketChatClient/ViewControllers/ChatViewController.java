package de.judgeman.WebSocketChatClient.ViewControllers;

import de.judgeman.WebSocketChatClient.HelperClasses.ViewRootAndControllerPair;
import de.judgeman.WebSocketChatClient.Interfaces.WebSocketResponseHandler;
import de.judgeman.WebSocketChatClient.Model.ChatUser;
import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Services.*;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import de.judgeman.WebSocketChatClient.ViewControllers.Other.FriendSelectionEntryViewController;
import de.judgeman.WebSocketChatClient.ViewControllers.Other.MessageLayoutViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.HashMap;
import java.util.List;

@Controller
public class ChatViewController extends ViewController implements WebSocketResponseHandler {

    private final Logger logger = LogService.getLogger(this.getClass());

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatUserService chatUserService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ViewService viewService;

    private WebSocketStompClient stompClient;
    private StompSession session;

    private String ownName;
    private String selectedNameOfFriend;

    HashMap<String, FriendSelectionEntryViewController> friendsEntryViewControllers;

    @FXML
    private TextField nameOfFriendTextField;
    @FXML
    private TextField messageTextField;
    @FXML
    private VBox messageVBox;
    @FXML
    private Label chatViewConnectionStateConnectedLabel;
    @FXML
    private Label chatViewConnectionStateDisconnectedLabel;
    @FXML
    private Label friendNameLabel;
    @FXML
    private VBox friendListVBox;
    @FXML
    private Button messageSendButton;
    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        friendsEntryViewControllers = new HashMap<>();
    }

    public void setUserName(String name) {
        ownName = name;

        logoutButton.setText(String.format(languageService.getLocalizationText("chatViewLogoutButton"), ownName));
        loadAndShowFriends();

        connect();
    }

    private void loadAndShowFriends() {
        for (ChatUser chatUser : chatUserService.loadChatUsers()) {
            addNewFriend(chatUser.getName());
        }
    }

    @FXML
    private void plusButtonClicked() {
        String nameOfFriend = nameOfFriendTextField.getText();
        if (nameOfFriend.isEmpty()) {
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),
                                              languageService.getLocalizationText("chatViewFriendNameEmpty"));

            return;
        }

        if (nameOfFriend.equals(ownName)) {
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),
                                              languageService.getLocalizationText("chatViewFriendNameIsOwnName"));

            return;
        }

        if (friendIsInFriendList(nameOfFriend)) {
            logger.info("Friend already exists");
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),
                                              languageService.getLocalizationText("chatViewFriendAlreadyExists"));
            return;
        }

        if(chatUserService.existUser(nameOfFriend)) {
            logger.info("Friend already in the database");
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),
                                              languageService.getLocalizationText("chatViewFriendAlreadyExists"));
            return;
        }

        chatUserService.saveNewChatFriend(nameOfFriend);
        addNewFriend(nameOfFriend);
        nameOfFriendTextField.clear();
    }

    @FXML
    private void logout() {
        disconnect();
        clearUserData();

        viewService.showView(ViewService.FILE_PATH_USER_SETUP);
    }

    private void clearUserData() {
        settingService.deleteSetting(SettingService.CURRENT_LOGIN_NAME);
        messageService.deleteAllMessages();
        chatUserService.deleteAllChatUsers();
    }

    private void addNewFriend(String nameOfNewFriend) {
        ViewRootAndControllerPair pair = viewService.getRootAndViewControllerFromFXML(ViewService.FILE_PATH_CHAT_VIEW_FRIEND_SELECTION_ENTRY);
        FriendSelectionEntryViewController friendSelectionEntryViewController = (FriendSelectionEntryViewController) pair.getViewController();
        friendsEntryViewControllers.put(nameOfNewFriend, friendSelectionEntryViewController);

        Platform.runLater(() -> {
            friendSelectionEntryViewController.setName(nameOfNewFriend);
            friendListVBox.getChildren().add(pair.getRoot());
        });
    }

    private void connect() {
        if (session != null) {
            logger.info("Already connected");
            return;
        }

        logger.info("Try to connect");
        if (stompClient == null) {
            stompClient = webSocketService.createNewStompClient();
        }
        session = webSocketService.connect(stompClient, ownName, this);
    }

    private void changeConnectionStateToConnected() {
        hideAllConnectionStates();
        chatViewConnectionStateConnectedLabel.setVisible(true);
    }

    private void changeConnectionStateToDisconnected() {
        hideAllConnectionStates();
        chatViewConnectionStateDisconnectedLabel.setVisible(true);
    }

    private void hideAllConnectionStates() {
        chatViewConnectionStateConnectedLabel.setVisible(false);
        chatViewConnectionStateDisconnectedLabel.setVisible(false);
    }

    private void clearChat() {
        messageVBox.getChildren().clear();
    }

    private void disconnect() {
        webSocketService.disconnect(session);
        session = null;
        changeConnectionStateToDisconnected();
    }

    @Override
    public void handleNewMessage(Message message) {
        String nameOfFriend = getFriendsNameFromMessage(message);
        ChatUser chatUser = chatUserService.loadChatUser(nameOfFriend);

        if (chatUser == null) {
            chatUser = chatUserService.saveNewChatFriend(nameOfFriend);
            addNewFriend(nameOfFriend);
        }

        messageService.saveNewMessage(chatUser, message);

        if (message.getSender() != null &&
            !nameOfFriend.equals(selectedNameOfFriend)) {
            boolean messageFromMyself = message.getSender().equals(ownName);

            if (!messageFromMyself) {
                Platform.runLater(() -> friendsEntryViewControllers.get(nameOfFriend).addMessageBadge());
            }

            return;
        }

        showMessage(message);
    }

    private String getFriendsNameFromMessage(Message message) {
        if (message.getSender().equals(ownName)) {
            return message.getReceiver();
        }

        return message.getSender();
    }

    private void showMessage(Message message) {
        String pathOfFXML = getRightMessageFXMLPath(message);

        ViewRootAndControllerPair pair = createMessageViewAndController(pathOfFXML);
        Platform.runLater(() -> {
            ((MessageLayoutViewController)pair.getViewController()).setMessage(message);
            messageVBox.getChildren().add(pair.getRoot());
        });
    }

    private boolean friendIsInFriendList(String nameOfFriend) {
        return friendsEntryViewControllers.containsKey(nameOfFriend);
    }

    @Override
    public void afterConnectionEstablished() {
        changeConnectionStateToConnected();
    }

    private String getRightMessageFXMLPath(Message message) {
        if (message.getSender() != null && message.getSender().equals(ownName)) {
            return ViewService.FILE_PATH_MESSAGE_LAYOUT_OWN;
        }

        return ViewService.FILE_PATH_MESSAGE_LAYOUT_OTHER;
    }

    @Override
    public void handleError(Throwable throwable) {
        disconnect();
        tryToReconnect();
    }

    private void tryToReconnect() {
        int secondsToReconnect = webSocketService.getReconnectionTryInSeconds();
        if (secondsToReconnect < 0) {
            logger.info("Auto reconnection is off");
            return;
        }

        logger.info("Reconnect in seconds: " + secondsToReconnect);
        Thread reconnectThread = new Thread(() -> {
            try {
                Thread.sleep(1000L * secondsToReconnect);
                connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        reconnectThread.start();
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
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),
                                              languageService.getLocalizationText("noActiveConnection"));
            logger.info("session is null");
            return false;
        }

        if (selectedNameOfFriend == null) {
            logger.info("No Friend selected");
            viewService.showInformationDialog(languageService.getLocalizationText("errorTitle"),
                                              languageService.getLocalizationText("chatViewNoFriendSelected"));
            return false;
        }

        if (messageTextField.getText().isEmpty()) {
            logger.info("TextField is empty - ignore");
            return false;
        }

        try {
            webSocketService.sendMessage(session, ownName, selectedNameOfFriend, messageTextField.getText());
            return true;
        } catch (Exception ex) {
            handleError(ex);
        }

        return false;
    }

    public void showFriendChat(String name) {
        clearChat();
        friendNameLabel.setText(name);
        selectedNameOfFriend = name;

        ChatUser chatUser = chatUserService.loadChatUser(name);
        showMessages(chatUserService.loadChatMessages(chatUser));
        activateMessageEntryAndButton();
    }

    private void activateMessageEntryAndButton() {
        messageTextField.setDisable(false);
        messageSendButton.setDisable(false);
    }

    private void showMessages(List<Message> messages) {
        for (Message message : messages) {
            showMessage(message);
        }
    }
}
