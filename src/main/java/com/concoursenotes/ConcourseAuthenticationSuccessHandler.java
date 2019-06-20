/*
 * Copyright (c) Concourse Notes 2019.
 */

package com.concoursenotes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedHashMap;

@Component
public class ConcourseAuthenticationSuccessHandler implements ApplicationListener<AuthenticationSuccessEvent> {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private DataSource dataSource;

    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
        OAuth2Authentication authentication = (OAuth2Authentication) authenticationSuccessEvent.getAuthentication();
        System.out.println(authentication.getClass().getName());
        if (dataSource == null) {
            System.out.println("NULL NULL NULL NULL");
        }
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO public.users\n" +
                    "    (\"id\", \"username\", \"creation\", \"email\", \"premium\") VALUES (?, ?, DEFAULT, ?, DEFAULT) ON CONFLICT DO NOTHING");
            LinkedHashMap<Object, Object> details = (LinkedHashMap<Object, Object>) authentication.getUserAuthentication().getDetails();
            statement.setString(1, (String) details.get("id"));
            statement.setString(2, (String) details.get("name"));
            statement.setString(3, (String) details.get("email"));
            PreparedStatement exists = connection.prepareStatement("select exists(select * from public.users where id=?)");
            exists.setString(1, (String) details.get("id"));
            ResultSet rs = exists.executeQuery();
            rs.next();
            if (!rs.getBoolean("exists") && (Boolean) details.get("verified_email")) {
                System.out.println("INSERT NEW USER");
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}