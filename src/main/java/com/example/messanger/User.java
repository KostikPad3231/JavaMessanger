package com.example.messanger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class User {
    public String username;
    public ObjectOutputStream out;
    public ObjectInputStream in;

    public User(String username, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.username = username;
        this.out = out;
        this.in = in;
    }
}

