/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@SpringBootApplication
public class Main {

    static ConcourseAuthenticationSuccessHandler handler = new ConcourseAuthenticationSuccessHandler();
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        handler = new ConcourseAuthenticationSuccessHandler();
        SpringApplication.run(Main.class, args).addApplicationListener(handler);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @RequestMapping("/")
    String index(OAuth2Authentication authentication, ModelMap map) {
        if (authentication != null) {
            LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getUserAuthentication().getDetails();
            map.addAttribute("userimage", details.get("picture") + "?sz=20");
            map.addAttribute("userloginaction", "/logout");
            map.addAttribute("username", "Welcome, " + details.get("given_name") + "!");
            map.addAttribute("logintext", "Log Out");
        } else {
            map.addAttribute("userimage", "/img/profile.png");
            map.addAttribute("userloginaction", "/login");
            map.addAttribute("username", "");
            map.addAttribute("logintext", "Log In");
        }
        return "index";
    }

    @RequestMapping("/notes")
    String notes(OAuth2Authentication authentication, ModelMap map) {
        LinkedHashMap<Object, Object> details = (LinkedHashMap<Object, Object>) authentication.getUserAuthentication().getDetails();
        map.addAttribute("userimage", details.get("picture") + "?sz=20");
        map.addAttribute("userloginaction", "/logout");
        map.addAttribute("username", "Welcome, " + details.get("given_name") + "!");
        map.addAttribute("logintext", "Log Out");
        map.addAttribute("notes", getNotesForUser((String) details.get("id")));

        return "my-notes";
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
            String stacktrace = "";
            for (StackTraceElement s : e.getStackTrace()) {
                stacktrace += s;
                stacktrace += "\n";
            }
            model.put("message", stacktrace);
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

    private List<Note> getNotesForUser(String userid) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM public.notes WHERE userid='" + userid + "' ORDER BY edit LIMIT 10");

            ArrayList<Note> output = new ArrayList<>();
            while (rs.next()) {
                output.add(new Note(rs.getString("id"), rs.getString("userid"), rs.getString("title"), rs.getDate("creation"), rs.getDate("edit"), rs.getString("content"), rs.getString("username")));
            }

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostConstruct
    public void init() {
        handler.setDataSource(dataSource);
    }
}
