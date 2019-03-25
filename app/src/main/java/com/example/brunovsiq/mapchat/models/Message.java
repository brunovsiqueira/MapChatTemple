package com.example.brunovsiq.mapchat.models;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Calendar;
import java.util.Date;

public class Message {

    private String text;
    private String author;
    private Calendar createdDate;
    private Date createdAt = new Date();

    public Message(String text, String author, Calendar createdDate) {
        this.text = text;
        this.author = author;
        this.createdDate = createdDate;
        this.createdAt.setTime(createdDate.getTimeInMillis());
    }


    public String getText() {
        return text;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
