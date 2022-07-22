package com.example.messanger;

public class Message{
    public final String from;
    public final String to;
    public final String type;
    public final String text;
    public Message(String from, String to, String type, String text){
        this.from = from;
        this.to = to;
        this.type = type;
        this.text = text;
    }
}

