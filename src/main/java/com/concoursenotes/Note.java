package com.concoursenotes;

import java.sql.Date;

public class Note {

    public String id;
    public String userid;
    public String title;
    public Date creation;
    public Date edit;
    public String content;
    public String username;

    public Note(String id, String userid, String title, Date creation, Date edit, String content, String username) {
        this.id = id;
        this.userid = userid;
        this.title = title;
        this.creation = creation;
        this.edit = edit;
        this.content = content;
        this.username = username;
    }
}