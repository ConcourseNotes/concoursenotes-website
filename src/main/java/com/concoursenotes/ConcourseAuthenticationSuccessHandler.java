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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            Statement statement = connection.createStatement();
            LinkedHashMap<Object, Object> details = (LinkedHashMap<Object, Object>) authentication.getUserAuthentication().getDetails();
            ResultSet rs = statement.executeQuery("select exists(select * from public.users where id='" + details.get("id") + "')");
            rs.next();
            if (!rs.getBoolean("exists") && (Boolean) details.get("verified_email")) {
                System.out.println("INSERT NEW USER");
                connection.createStatement().executeUpdate("INSERT INTO public.users\n" +
                        "    (\"id\", \"username\", \"creation\", \"email\", \"premium\") VALUES ('" + details.get("id") + "', '" + details.get("name") + "', DEFAULT, '" + details.get("email") + "', DEFAULT) ON CONFLICT DO NOTHING");
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