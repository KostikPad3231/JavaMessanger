package com.example.messanger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class User {
    private String username;
    private ObjectOutputStream out;

    public User(String username, ObjectOutputStream out) throws IOException {
        this.username = username;
        System.out.println("user 1");
        this.out = out;
        System.out.println("user 2");
    }

    public String getUsername() {
        return username;
    }

    public ObjectOutputStream getOut() {
        return out;
    }
}

