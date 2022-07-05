package com.example.messanger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Server {
    private static ArrayList<User> clients = new ArrayList<>();
    private static ArrayList<String> users = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(60000)){
            Semaphore semaphore = new Semaphore(3);
            while(true) {
                new Thread(() -> {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try(Socket client = serverSocket.accept()) {
                        serve(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        System.out.println("клиент отключился");
                        semaphore.release();
                    }
                }).start();
            }
        }
    }

    private static void updateUsersList() {
        users.clear();
        for(User user: clients){
            users.add(user.getUsername());
        }
    }

    private static void serve(Socket client) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(client.getInputStream());
        String t_name;
        while(true){
            try {
                t_name = (String) in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if(checkUsername(t_name)){
                out.writeObject("ok");
                out.flush();
                break;
            }
            else{
                out.writeObject("bad");
                out.flush();
            }
        }
        String username = t_name;
        System.out.println(username + " подключился");
        System.out.println(1);
        User new_user = new User(username, out);
        clients.add(new_user);
        users.add(username);
        System.out.println("Before notifying " + users.size());
        notifyUsers();
        System.out.println(3);

        while(true){
            try {
                Document document = (Document) in.readObject();
                NodeList message_elements = document.getElementsByTagName("message").item(0).getChildNodes();
                Message message = new Message(message_elements.item(0).getTextContent(),
                        message_elements.item(1).getTextContent(),
                        message_elements.item(2).getTextContent(),
                        message_elements.item(3).getTextContent());
                if(message.getType().equals("disconnect")){
                    clients.removeIf(user -> user.getUsername().equals(message.getFrom()));
                    updateUsersList();
                    notifyUsers();
                    client.close();
                    break;
                }
                System.out.println("Получил сообщение от " + message.getFrom() + " с текстом: " + message.getText());
                System.out.println("Отправляю " + message.getTo());
                ObjectOutputStream out_to;
                for (User user: clients) {
                    System.out.println(user.getUsername());
                    if(user.getUsername().equals(message.getTo())){
                        out_to = user.getOut();
                        out_to.writeObject(document);
                        out_to.flush();
                        break;
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void notifyUsers(){
        System.out.println(4);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        System.out.println(5);
        Document document = documentBuilder.newDocument();
        Element message = document.createElement("message");
        Element from = document.createElement("from");
        Element to = document.createElement("to");
        Element type = document.createElement("type");
        Element data = document.createElement("data");
        Text text_from = document.createTextNode("Server");
        Text text_to = document.createTextNode("");
        Text text_type = document.createTextNode("notify");
        Text text_data = document.createTextNode("");
        document.appendChild(message);
        message.appendChild(from);
        from.appendChild(text_from);
        message.appendChild(to);
        to.appendChild(text_to);
        message.appendChild(type);
        type.appendChild(text_type);
        message.appendChild(data);
        data.appendChild(text_data);
        System.out.println(6);

        for(String name: users){
            System.out.println(name);
        }

        ObjectOutputStream user_out;
        for(User user: clients){
            try {
                System.out.println(7);
                user_out = user.getOut();
                user_out.writeObject(document);
                user_out.flush();
                System.out.println(8);
                System.out.println("Notify " + user.getUsername() + " with " + users.size());
                user_out.writeObject(users);
                user_out.flush();
                System.out.println(9);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static boolean checkUsername(String username){
        for(User user: clients){
            if(user.getUsername().equals(username)){
                return false;
            }
        }
        return true;
    }
}
