package de.judgeman.WebSocketChatClient.ViewControllers.Other;

import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Services.MessageService;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class MessageLayoutViewController extends ViewController {

    @Autowired
    private MessageService messageService;

    private Message message;

    @FXML
    private Label messageTextLabel;
    @FXML
    private Label nameTextLabel;
    @FXML
    private Label messageDateLabel;

    public void setMessage(Message message) {
        this.message = message;

        nameTextLabel.setText(message.getSender());
        messageTextLabel.setText(message.getText());
        messageDateLabel.setText(messageService.getFormattedDateString(message.getDate()));
    }
}