package de.judgeman.WebSocketChatClient.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Paul Richter on Sat 01/04/2023
 */
@Entity
public class ChatUser {

    @Id
    private String name;

    @Transient
    private List<Message> messages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
