package de.judgeman.WebSocketChatClient.Repositories;

import de.judgeman.WebSocketChatClient.Model.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Paul Richter on Mon 03/04/2023
 */
public interface MessageRepository extends CrudRepository<Message, Long> {

    List<Message> findAllBySenderOrReceiver(String sender, String receiver);

    Message findTop1BySenderOrReceiverOrderByDateDesc(String sender, String receiver);

    List<Message> findBySenderAndSeenOrReceiverAndSeen(String sender, boolean seenSender, String receiver, boolean seenReceiver);

}
