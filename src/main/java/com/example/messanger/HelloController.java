package com.example.messanger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.WindowEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class HelloController {
    Client client;
    String user_to_send;
    String my_username;
    @FXML
    TextField username;
    @FXML
    TextField message;
    @FXML
    Button send;
    @FXML
    VBox messages;
    @FXML
    VBox users;
    @FXML
    private Label welcomeText;
    @FXML
    protected void sendMessage() {
        if(!message.getText().isEmpty()){
            client.sendMessage("message", message.getText(), user_to_send);

            HBox message_box = new HBox();
            message_box.setPrefWidth(Double.MAX_VALUE);
            message_box.setAlignment(Pos.CENTER_RIGHT);
            Text text = new Text(message.getText());
            text.setFill(Color.RED);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setPrefWidth(100);
            textFlow.getStyleClass().add("out_message");
            textFlow.getStyleClass().add("message");
            message_box.getChildren().add(textFlow);
            messages.getChildren().add(message_box);
        }
    }
    @FXML
    public void connect(ActionEvent actionEvent) {
        client = new Client("localhost", 60000, this);
        System.out.println("good");
        if(username.getText().isEmpty() || !client.trySetUsername(username.getText())){
            System.out.println("gavno");
            client.close();
        }
        else {
            my_username = username.getText();
            System.out.println("i am here");
            users.getParent().setVisible(true);
            System.out.println("set visible");
            client.launch();
        }
    }
    public void updateUsersList(ArrayList<String> usersList) {
        for(String user: usersList){
            if(my_username.equals(user)) continue;
            HBox user_box = new HBox();
            user_box.setPrefWidth(Double.MAX_VALUE);
            user_box.setAlignment(Pos.CENTER);
            Text text = new Text(user);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setPrefWidth(50);
            textFlow.getStyleClass().add("user");
            user_box.getChildren().add(textFlow);
            Platform.runLater(() -> {
                users.getChildren().add(user_box);
            });
        }
    }
    private EventHandler<WindowEvent> closeEventHandler = event -> {
        client.close();
    };

    public EventHandler<WindowEvent> getCloseEventHandler(){
        return closeEventHandler;
    }
}