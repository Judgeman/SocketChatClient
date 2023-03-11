package de.judgeman.WebSocketChatClient.Interfaces;

import de.judgeman.WebSocketChatClient.Model.Message;

public interface WebSocketResponseHandler {

    void handleError(Throwable throwable);
    void handleNewMessage(Message message);
    void afterConnectionEstablished();
}
