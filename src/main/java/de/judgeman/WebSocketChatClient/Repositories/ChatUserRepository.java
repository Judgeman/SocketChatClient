package de.judgeman.WebSocketChatClient.Repositories;

import de.judgeman.WebSocketChatClient.Model.ChatUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Paul Richter on Sat 01/04/2023
 */
public interface ChatUserRepository extends CrudRepository<ChatUser, String> {

    List<ChatUser> findAll();

    ChatUser findByName(String name);

}
