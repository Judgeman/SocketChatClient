package de.judgeman.WebSocketChatClient.ViewControllers.Other;

import de.judgeman.WebSocketChatClient.Model.ChatUser;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import de.judgeman.WebSocketChatClient.ViewControllers.ChatViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

/**
 * Created by Paul Richter on Fri 24/03/2023
 */
@Controller
@Scope("prototype")
public class FriendSelectionEntryViewController extends ViewController {

    @Autowired
    private ChatViewController chatViewController;

    private ChatUser chatUser;

    private int newMessages;

    public FriendSelectionEntryViewController() {
        newMessages = 0;
    }

    @FXML
    private Label nameLabel;
    @FXML
    private Label newMessagesLabel;

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public String getName() {
        return nameLabel.getText();
    }

    @FXML
    private void entryClicked() {
        chatViewController.showFriendChat(getName());
        setNewMessagesAsSeen();
    }

    public void addMessageBadge() {
        newMessages++;
        showNewMessagesReceived();
    }

    private void showNewMessagesReceived() {
        newMessagesLabel.setText("" + newMessages);
    }

    public void setNewMessagesAsSeen() {
        newMessages = 0;
        newMessagesLabel.setText("");
    }
}
