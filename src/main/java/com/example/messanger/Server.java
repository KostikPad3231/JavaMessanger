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
import java.util.ArrayList;

public class Server {
    private static ArrayList<User> clients = new ArrayList<>();
    private static ArrayList<String> users = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(60000)){
            System.out.println("Сервер запущен!");
            while(true) {
                new Thread(() -> {
                    try(Socket client = serverSocket.accept()) {
                        serve(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }
    private static void updateUsersList() {
        users.clear();
        for(User user: clients){
            users.add(user.username);
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
        System.out.println(t_name + " подключился");
        User new_user = new User(t_name, out, in);
        clients.add(new_user);
        users.add(t_name);
        notifyUsersConnectUser(t_name);
        notifyUser(new_user);

        while(true){
            try {
                Document document = (Document) in.readObject();
                NodeList message_elements = document.getElementsByTagName("message").item(0).getChildNodes();
                Message message = new Message(message_elements.item(0).getTextContent(),
                        message_elements.item(1).getTextContent(),
                        message_elements.item(2).getTextContent(),
                        message_elements.item(3).getTextContent());
                if(message.type.equals("disconnect")){
                    System.out.println(message.from + " отключился");
                    clients.removeIf(user -> user.username.equals(message.from));
                    updateUsersList();
                    notifyUsersDelete(message.from);
                    break;
                }
                System.out.println("Получил сообщение от " + message.from + " с текстом: " + message.text);
                System.out.println("Отправляю " + message.to);
                ObjectOutputStream out_to;
                for (User user: clients) {
                    if(user.username.equals(message.to)){
                        out_to = user.out;
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
    private static void notifyUser(User user){
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
        Text text_from = document.createTextNode("Server");
        Text text_to = document.createTextNode("");
        Text text_type = document.createTextNode("notify all");
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

        try {
            user.out.writeObject(document);
            user.out.flush();
            user.out.writeObject(users);
            user.out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void notifyUsersConnectUser(String name){
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
        Text text_from = document.createTextNode("Server");
        Text text_to = document.createTextNode("");
        Text text_type = document.createTextNode("notify connect");
        Text text_data = document.createTextNode(name);
        document.appendChild(message);
        message.appendChild(from);
        from.appendChild(text_from);
        message.appendChild(to);
        to.appendChild(text_to);
        message.appendChild(type);
        type.appendChild(text_type);
        message.appendChild(data);
        data.appendChild(text_data);

        for(User user: clients){
            try {
                if(!user.username.equals(name)){
                    user.out.writeObject(document);
                    user.out.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void notifyUsersDelete(String name){
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
        Text text_from = document.createTextNode("Server");
        Text text_to = document.createTextNode("");
        Text text_type = document.createTextNode("notify delete");
        Text text_data = document.createTextNode(name);
        document.appendChild(message);
        message.appendChild(from);
        from.appendChild(text_from);
        message.appendChild(to);
        to.appendChild(text_to);
        message.appendChild(type);
        type.appendChild(text_type);
        message.appendChild(data);
        data.appendChild(text_data);

        for(User user: clients){
            try {
                if(!user.username.equals(name)){
                    user.out.writeObject(document);
                    user.out.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static boolean checkUsername(String username){
        for(User user: clients){
            if(user.username.equals(username)){
                return false;
            }
        }
        return true;
    }
}
