/*
 * Copyright (c) Concourse Notes 2019.
 */

package com.concoursenotes;

import java.sql.Date;

public class Note {

    public String id;
    public String userID;
    public String title;
    public Date creation;
    public Date edit;
    public String content;
    public String username;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getEdit() {
        return edit;
    }

    public void setEdit(Date edit) {
        this.edit = edit;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Note() {

    }

    public Note(String id, String userID, String title, Date creation, Date edit, String content, String username) {
        this.id = id;
        this.userID = userID;
        this.title = title;
        this.creation = creation;
        this.edit = edit;
        this.content = content;
        this.username = username;
    }
}