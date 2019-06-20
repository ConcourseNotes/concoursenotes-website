/*
 * Copyright (c) Concourse Notes 2019.
 */

package com.concoursenotes;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

@Controller
public class SubmitController {
    private final DataSource dataSource;

    public SubmitController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/submit")
    public String noteForm(ModelMap map, OAuth2Authentication authentication) {
        map.addAttribute(new Note());
        Main.putHeaderDetails(authentication, map);
        return "submit";
    }

    @PostMapping("/submit")
    public String noteSubmit(ModelMap map, OAuth2Authentication authentication, @ModelAttribute Note note) {
        Main.putHeaderDetails(authentication, map);
        System.out.println(note.content);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO \"public\".\"notes\" (\"userid\", \"username\", \"creation\", \"edit\", \"content\", \"id\", \"title\") VALUES (?, ?, DEFAULT, DEFAULT, ?, DEFAULT, ?) RETURNING id");
            LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getUserAuthentication().getDetails();
            statement.setString(1, details.get("id"));
            statement.setString(2, details.get("name"));
            statement.setString(3, note.content);
            statement.setString(4, note.title);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return "redirect:/note/" + rs.getString("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
