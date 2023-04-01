package de.judgeman.WebSocketChatClient.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.judgeman.WebSocketChatClient.Interfaces.WebSocketResponseHandler;
import de.judgeman.WebSocketChatClient.Model.Message;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class WebSocketService {

    @Value("${messageServer.address.pattern}")
    private String serverBaseAddressPattern;
    @Value("${messageServer.channelPath}")
    private String channelPath;
    @Value("${messageServer.registerPath}")
    private String registerPath;
    @Value("${messageServer.address}")
    private String serverBaseAddress;
    @Value("${messageServer.port}")
    private int serverPort;
    @Value("${messageServer.connectingTimeout:10}")
    private int serverConnectingTimeout;
    @Value("${messageServer.messageSendingEndPoint}")
    private String messageSendingEndpoint;
    @Value("${messageServer.reconnectionTryInSeconds:10}")
    private int reconnectionTryInSeconds;

    private final Logger logger = LogService.getLogger(this.getClass());

    public int getReconnectionTryInSeconds() {
        return reconnectionTryInSeconds;
    }

    public WebSocketStompClient createNewStompClient() {
        List<Transport> transports = new ArrayList<>();

        StandardWebSocketClient defaultWebSocketClient = new StandardWebSocketClient();
        transports.add(new WebSocketTransport(defaultWebSocketClient));

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        registerMessageConverter(stompClient);

        return stompClient;
    }

    public StompSession connect(WebSocketStompClient stompClient, String name, WebSocketResponseHandler responseHandler) {
        StompSession session = null;

        try {
            session = openSession(stompClient, responseHandler);
            subscribeToChannel(session, String.format(channelPath, name), responseHandler);
            logger.info("Subscribed to channel " + name);
        } catch (Exception ex) {
            ex.printStackTrace();

            if (session != null) {
                session.disconnect();
            }

            session = null;
        }

        return session;
    }

    private void subscribeToChannel(StompSession session, String channel, WebSocketResponseHandler responseHandler) {
        session.subscribe(channel, new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
                logger.info("connection successfully established");
                responseHandler.afterConnectionEstablished();
            }

            @Override
            public void handleException(StompSession stompSession, StompCommand stompCommand, StompHeaders stompHeaders, byte[] bytes, Throwable throwable) {
                throwable.printStackTrace();
                responseHandler.handleError(throwable);
            }

            @Override
            public void handleTransportError(StompSession stompSession, Throwable throwable) {
                throwable.printStackTrace();
                responseHandler.handleError(throwable);
            }

            @Override
            public Type getPayloadType(StompHeaders stompHeaders) {
                return Message.class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object object) {
                Message message = (Message) object;
                logger.info("Incoming text " + message.getText());
                responseHandler.handleNewMessage(message);
            }
        });
    }

    private StompSession openSession(WebSocketStompClient stompClient, WebSocketResponseHandler responseHandler) throws ExecutionException, InterruptedException, TimeoutException {
        String serverRegisterAddress = String.format(serverBaseAddressPattern, serverBaseAddress, serverPort, registerPath);
        logger.info("Use server register address: " + serverRegisterAddress);

        return stompClient.connect(serverRegisterAddress, new StompSessionHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return null;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

            }

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                logger.info("connection successfully established");
                responseHandler.afterConnectionEstablished();
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
                logger.info("connection not successfully established");
                responseHandler.handleError(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                exception.printStackTrace();
                responseHandler.handleError(exception);
            }
        }).get(serverConnectingTimeout, TimeUnit.SECONDS);
    }

    private void registerMessageConverter(WebSocketStompClient stompClient) {
        stompClient.setMessageConverter(new AbstractMessageConverter() {
            private final ObjectMapper mapper = new ObjectMapper();

            @Override
            protected boolean supports(Class<?> aClass) { return aClass == Message.class; }
            @Override
            protected Object convertFromInternal(org.springframework.messaging.Message<?> message, Class<?> targetClass, Object conversionHint) {
                String messageText = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
                Message messageObject = null;
                try {
                    messageObject = mapper.readValue(messageText, Message.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return messageObject;
            }

            @Override
            protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
                try {
                    return mapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                return super.convertToInternal(payload, headers, conversionHint);
            }
        });
    }

    public void sendMessage(StompSession session, String name, String nameOfFriend, String messageText) {
        Message message = new Message();
        message.setSender(name);
        message.setReceiver(nameOfFriend);
        message.setText(messageText);

        session.send(messageSendingEndpoint, message);

        logger.info("Message sent from " + name + " to " + nameOfFriend + ": " + message.getText());
    }

    public void disconnect(StompSession session) {
        disconnectSession(session);
    }

    private void disconnectSession(StompSession session) {
        if (session == null || !session.isConnected()) {
            return;
        }

        session.disconnect();
    }
}
