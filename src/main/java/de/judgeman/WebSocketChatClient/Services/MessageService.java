package de.judgeman.WebSocketChatClient.Services;

import de.judgeman.WebSocketChatClient.Model.ChatUser;
import de.judgeman.WebSocketChatClient.Model.Message;
import de.judgeman.WebSocketChatClient.Repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Paul Richter on Mon 03/04/2023
 */
@Service
public class MessageService {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");;
    private final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");;

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

    public String getFormattedDateString(Date date) {
        if (dateIsToday(date)) {
            return simpleTimeFormat.format(date);
        }

        return simpleDateFormat.format(date);
    }

    private boolean dateIsToday(Date date) {
        Calendar dateToCheck = Calendar.getInstance();
        dateToCheck.setTime(date);

        Calendar currentDate = Calendar.getInstance();
        return dateToCheck.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                dateToCheck.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                dateToCheck.get(Calendar.DATE) == currentDate.get(Calendar.DATE);

    }

    public Message loadLastMessage(ChatUser chatUser) {
        return messageRepository.findTop1BySenderOrReceiverOrderByDateDesc(chatUser.getName(), chatUser.getName());
    }

    public List<Message> loadAllNewMessages(ChatUser chatUser) {
        return messageRepository.findBySenderAndSeenOrReceiverAndSeen(chatUser.getName(), false, chatUser.getName(), false);
    }

    public void updateMessage(Message message) {
        messageRepository.save(message);
    }
}