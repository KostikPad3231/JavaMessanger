package com.example.messanger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final HelloController controller;
    private String username;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    public Client(String ip, int port, HelloController controller){
        try {
            socket = new Socket(ip, port);
            this.controller = controller;
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void launch(){
        Thread gettingMessages = new Thread(this::getMessage);
        gettingMessages.start();
        System.out.println("launched");
    }
    private void getMessage(){
        try{
            while(true){
                System.out.println(1);
                Document document = (Document) in.readObject();
                System.out.println(2);
                NodeList message_elements = document.getElementsByTagName("message").item(0).getChildNodes();
                Message message = new Message(message_elements.item(0).getTextContent(),
                        message_elements.item(1).getTextContent(),
                        message_elements.item(2).getTextContent(),
                        message_elements.item(3).getTextContent());
                System.out.println(message.getType());
                if(message.getType().equals("notify")){
                    notifyMe();
                }
                System.out.println(message.getFrom() + ": " + message.getText());
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private void notifyMe() {
        try {
            ArrayList<String> usersList = (ArrayList<String>) in.readObject();
            System.out.println("notified with: " + usersList.size());
            for(String user: usersList){
                System.out.println("User: " + user);
            }
            controller.updateUsersList(usersList);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendMessage(String message_type, String text, String user_to_send){
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document = documentBuilder.newDocument();
        Element message = document.createElement("message");
        Element from = document.createElement("from");
        Element to = document.createElement("to");
        Element type = document.createElement("type");
        Element data = document.createElement("data");
        Text text_from = document.createTextNode(username);
        Text text_to = document.createTextNode(user_to_send);
        Text text_type = document.createTextNode(message_type);
        Text text_data = document.createTextNode(text);
        document.appendChild(message);
        message.appendChild(from);
        from.appendChild(text_from);
        message.appendChild(to);
        to.appendChild(text_to);
        message.appendChild(type);
        type.appendChild(text_type);
        message.appendChild(data);
        data.appendChild(text_data);

        try {
            out.writeObject(document);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean trySetUsername(String username) {
        System.out.println(username);
        String response;
        try {
            out.writeObject(username);
            out.flush();
            response = (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("got response");
        if(response.equals("ok")){
            this.username = username;
            return true;
        }
        else{
            return false;
        }
    }

    public void close() {
        try {
            sendMessage("disconnect", "", "");
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
