package de.judgeman.WebSocketChatClient.HelperClasses;

import de.judgeman.WebSocketChatClient.ViewControllers.Abstract.ViewController;
import javafx.scene.Parent;

/**
 * Created by Paul Richter on Fri 04/09/2020
 */
public class ViewRootAndControllerPair {

    private Parent root;
    private ViewController viewController;

    public ViewRootAndControllerPair(Parent root, ViewController viewController) {
        this.root = root;
        this.viewController = viewController;
    }

    public Parent getRoot() {
        return root;
    }

    public ViewController getViewController() {
        return viewController;
    }
}