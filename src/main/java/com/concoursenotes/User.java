package com.concoursenotes;

import java.sql.Date;

public class User {
    public String id;
    public String name;
    public Date created;
    public String username;
    public boolean premium;

    public User(String id, String name, Date created, String username, boolean premium) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.username = username;
        this.premium = premium;
    }
}
