package com.bielinskim;

import java.util.ArrayList;
import java.util.List;

public class Book {

    int nr;
    String author;
    String title;
    String date;
    String content;
    List<String> users;
    boolean blocked;

    public Book() {
        this.users = new ArrayList<String>();
        blocked = false;
    }

    public Book(int nr, String author, String title, String date, String content) {
        this.nr = nr;
        this.author = author;
        this.title = title;
        this.date = date;
        this.content = content;
        this.users = new ArrayList<String>();
        blocked = false;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List getUsers() {
        return users;
    }

    public void setUsers(List users) {
        this.users = users;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
