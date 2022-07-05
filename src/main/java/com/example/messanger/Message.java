package com.example.messanger;

public class Message{
    private final String from;
    private final String to;
    private final String type;
    private final String text;
    public Message(String from, String to, String type, String text){
        this.from = from;
        this.to = to;
        this.type = type;
        this.text = text;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}

