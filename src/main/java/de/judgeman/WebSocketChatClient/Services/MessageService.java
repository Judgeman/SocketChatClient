package de.judgeman.WebSocketChatClient.Services;

import de.judgeman.WebSocketChatClient.Model.ChatUser;
import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul Richter on Mon 03/04/2023
 */
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<Message> loadAllMessages(ChatUser chatUser) {
        List<Message> messages = messageRepository.findAllBySenderOrReceiver(chatUser.getName(), chatUser.getName());
        chatUser.setMessages(messages);

        return messages;
    }

    public void saveNewMessage(ChatUser chatUser, Message message) {
        List<Message> messages = chatUser.getMessages();
        if (messages == null) {
            messages = new ArrayList<>();
        }

        messages.add(message);
        messageRepository.save(message);
    }

    public void deleteAllMessages() {
        messageRepository.deleteAll();
    }
}