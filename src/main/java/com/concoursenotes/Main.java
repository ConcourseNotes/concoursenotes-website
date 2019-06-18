/*
 * Copyright (c) Concourse Notes 2019.
 */

package com.concoursenotes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "LoopStatementThatDoesntLoop"})
@Controller
@SpringBootApplication
public class Main {

    private static ConcourseAuthenticationSuccessHandler handler = new ConcourseAuthenticationSuccessHandler();
    @Value("${spring.datasource.url}")
    private String dbUrl;

    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        handler = new ConcourseAuthenticationSuccessHandler();
        SpringApplication.run(Main.class, args).addApplicationListener(handler);
    }

    @RequestMapping("/")
    String index(OAuth2Authentication authentication, ModelMap map) {
        if (authentication != null) {
            putHeaderDetails(authentication, map);
        } else {
            map.addAttribute("userimage", "/img/profile.png");
            map.addAttribute("userloginaction", "/login");
            map.addAttribute("username", "");
            map.addAttribute("logintext", "Log In/Sign Up");
        }
        return "index";
    }

    @RequestMapping("/note/**")
    String note(OAuth2Authentication authentication, ModelMap map, HttpServletRequest request) {
        String id = request.getServletPath().substring(request.getServletPath().lastIndexOf("/") + 1);
        putHeaderDetails(authentication, map);
        Note note = getNoteById(id);
        if (note == null) {
            return "error";
        } else {
            map.addAttribute("note", note);
        }
        return "note";
    }

    private void putHeaderDetails(OAuth2Authentication authentication, ModelMap map) {
        LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getUserAuthentication().getDetails();
        map.addAttribute("userimage", details.get("picture") + "?sz=20");
        map.addAttribute("userloginaction", "/logout");
        map.addAttribute("username", "Welcome, " + details.get("given_name") + "!");
        map.addAttribute("logintext", "Log Out");
    }

    @RequestMapping("/notes")
    String notes(OAuth2Authentication authentication, ModelMap map) {
        LinkedHashMap<Object, Object> details = (LinkedHashMap<Object, Object>) authentication.getUserAuthentication().getDetails();
        map.addAttribute("userimage", details.get("picture") + "?sz=20");
        map.addAttribute("userloginaction", "/logout");
        map.addAttribute("username", "Welcome, " + details.get("given_name") + "!");
        map.addAttribute("logintext", "Log Out");
        map.addAttribute("notes", getNotesForUser((String) details.get("id")));

        return "notes";
    }

    @RequestMapping("/db")
    String db(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT t.*, CTID FROM public.notes t\n" +
                    "     LIMIT 501");

            ArrayList<String> output = new ArrayList<>();
            while (rs.next()) {
                output.add("Read from DB: " + rs.getInt("id") + " &UN " + rs.getString("title"));
            }

            model.put("records", output);
            return "db";
        } catch (Exception e) {
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                stacktrace.append(s);
                stacktrace.append("\n");
            }
            model.put("message", stacktrace.toString());
            return "error";
        }
    }

    @RequestMapping("/study")
    String study(Map<String, Object> model) {

        return "study";
    }

    @RequestMapping("/hello")
    String hello(Map<String, Object> model, OAuth2Authentication authentication) {
        LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getUserAuthentication().getDetails();
        model.put("username", details.get("name"));
        model.put("userimage", details.get("picture"));
        return "hello";
    }

    @RequestMapping("/login")
    String login(OAuth2Authentication authentication) {
        System.out.println("GOT");
        return "login";
    }

    @Bean
    public DataSource dataSource() {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        } else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setDriverClassName("org.postgresql.Driver");
            return new HikariDataSource(config);
        }
    }

    private List<Note> getNotesForUser(String userID) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM public.notes WHERE userid=? ORDER BY edit LIMIT 10");
            stmt.setString(1, userID);
            ResultSet rs = stmt.executeQuery();

            ArrayList<Note> output = new ArrayList<>();
            while (rs.next()) {
                output.add(new Note(rs.getString("id"), rs.getString("userID"), rs.getString("title"), rs.getDate("creation"), rs.getDate("edit"), rs.getString("content"), rs.getString("username")));
            }

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Note getNoteById(String id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM public.notes WHERE id=? LIMIT 1");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("CALLED" + rs.getString("title"));
                return new Note(rs.getString("id"), rs.getString("userID"), rs.getString("title"), rs.getDate("creation"), rs.getDate("edit"), rs.getString("content"), rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User getUserById(String id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM public.users WHERE id=? LIMIT 1");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return new User(rs.getString("id"), rs.getString("username"), rs.getDate("creation"), rs.getString("email"), rs.getBoolean("premium"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostConstruct
    public void init() {
        handler.setDataSource(dataSource);
    }
}
