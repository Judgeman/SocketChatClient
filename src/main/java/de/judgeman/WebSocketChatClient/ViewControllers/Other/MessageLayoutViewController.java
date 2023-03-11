package de.judgeman.WebSocketChatClient.ViewControllers.Other;

import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class MessageLayoutViewController extends ViewController {

    private Message message;

    @FXML
    private Label messageTextLabel;
    @FXML
    private Label nameTextLabel;

    public void setMessage(Message message) {
        this.message = message;

        nameTextLabel.setText(message.getSender());
        messageTextLabel.setText(message.getText());
    }
}