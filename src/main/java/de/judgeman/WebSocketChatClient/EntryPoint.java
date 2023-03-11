package de.judgeman.WebSocketChatClient;

import com.sun.javafx.application.LauncherImpl;
import de.judgeman.WebSocketChatClient.Services.LogService;

/**
 * Created by Paul Richter on Mon 30/03/2020
 */
public class EntryPoint {

    public static void main(String[] args) {

        // tie the console out print to a file
        LogService.tieSystemOutAndErrToFileLogging();

        // Start as Spring Boot application with JavaFx
        LauncherImpl.launchApplication(WebSocketChatClientApplication.class, SplashScreen.class, args);
    }
}
