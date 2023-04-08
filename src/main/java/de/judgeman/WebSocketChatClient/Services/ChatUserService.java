package de.judgeman.WebSocketChatClient.Services;

import de.judgeman.WebSocketChatClient.Model.ChatUser;
import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Repositories.ChatUserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Paul Richter on Sat 01/04/2023
 */
@Service
public class ChatUserService {

    private final Logger logger = LogService.getLogger(this.getClass());

    @Autowired
    private ChatUserRepository chatUserRepository;

    @Autowired
    private MessageService messageService;

    public ChatUser saveNewChatFriend(String newUsername) {
        ChatUser newUser = chatUserRepository.findByName(newUsername);
        if (newUser != null) {
            logger.info("ChatUser " + newUsername + " already exists");
            return newUser;
        }

        return chatUserRepository.save(createNewChatUser(newUsername));
    }

    private ChatUser createNewChatUser(String name) {
        ChatUser user = new ChatUser();
        user.setName(name);

        return user;
    }

    public ChatUser loadChatUser(String name) {
        return chatUserRepository.findByName(name);
    }

    public List<ChatUser> loadChatUsers() {
        return chatUserRepository.findAll();
    }

    public boolean existUser(String name) {
        return chatUserRepository.findByName(name) != null;
    }

    public List<Message> loadChatMessages(ChatUser chatUser) {
        return messageService.loadAllMessages(chatUser);
    }

    public void deleteAllChatUsers() {
        chatUserRepository.deleteAll();
    }
}
