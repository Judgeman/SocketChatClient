package de.judgeman.WebSocketChatClient.ViewControllers.Other;

import de.judgeman.WebSocketChatClient.Model.ChatUser;
import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Services.MessageService;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import de.judgeman.WebSocketChatClient.ViewControllers.ChatViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul Richter on Fri 24/03/2023
 */
@Controller
@Scope("prototype")
public class FriendSelectionEntryViewController extends ViewController {

    @Autowired
    private ChatViewController chatViewController;

    @Autowired
    private MessageService messageService;

    private ChatUser chatUser;

    private Message lastMessage;

    private List<Message> newMessages;

    public FriendSelectionEntryViewController() {
        newMessages = new ArrayList<>();
    }

    @FXML
    private Label nameLabel;
    @FXML
    private Label newMessagesLabel;
    @FXML
    private Label lastMessageText;
    @FXML
    private Label lastMessageDate;

    public void setChatUser(ChatUser chatUser) {
        this.chatUser = chatUser;

        nameLabel.setText(chatUser.getName());
    }

    public ChatUser getChatUser() {
        return chatUser;
    }

    @FXML
    private void entryClicked() {
        chatViewController.showFriendChat(getChatUser());
        setNewMessagesAsSeen();
    }

    public void addNewMessage(Message message) {
        newMessages.add(message);
        setLastMessage(message);

        updateNewMessagesBadge();
    }

    public void setLastMessage(Message message) {
        if (message == null) {
            return;
        }

        this.lastMessage = message;

        lastMessageText.setText(getPreviewText(message));
        lastMessageDate.setText(messageService.getFormattedDateString(message.getDate()));
    }

    private String getPreviewText(Message message) {
        if (message == null) {
            return "";
        }

        String displayText = message.getText();
        if (displayText.length() > 20) {
            displayText = message.getText().substring(0, 20);
            displayText = displayText + "...";
        }

        return displayText;
    }

    private void updateNewMessagesBadge() {
        if (newMessages == null || newMessages.size() == 0) {
            newMessagesLabel.setText("");
            return;
        }

        newMessagesLabel.setText("" + newMessages.size());
    }

    public void setNewMessagesAsSeen() {
        for (Message message : newMessages) {
            message.setSeen(true);
            messageService.updateMessage(message);
        }

        updateNewMessagesBadge();
        newMessages.clear();
    }

    public void setNewMessages(List<Message> newMessages) {
        this.newMessages = newMessages;
        updateNewMessagesBadge();
    }
}
