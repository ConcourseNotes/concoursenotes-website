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