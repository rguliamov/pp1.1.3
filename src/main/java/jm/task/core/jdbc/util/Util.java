package jm.task.core.jdbc.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * вместо того, чтобы отдавать клиентскому коду Connection or Statement, мы просим клиентов
 * сказать нам, что они хотят с ними делать, т.е. клиенты передают лямбды.
 * Теперь мы сами управляем высвобождением ресурсов, закрывая Connection и Statement
 */
public class Util {
    /********************** jdbc **********************************/
    // реализуйте настройку соеденения с БД
    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T object) throws SQLException;
    }

    @FunctionalInterface
    public interface  SQLConsumer<T> {
        void accept(T object) throws SQLException;
    }

    private static HikariConfig config = new HikariConfig("/jdbc.properties");
    private static DataSource connectionPool = new HikariDataSource(config);

    public void connection(SQLConsumer<? super Connection> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        try (Connection connection = connectionPool.getConnection()) {
            consumer.accept(connection);
        }
    }

    public <R> R connection(SQLFunction<? super Connection, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        try (Connection connection = connectionPool.getConnection()) {
            return function.apply(connection);
        }
    }

    public <R> R statement(SQLFunction<? super Statement, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        return connection(conn -> {
            try (Statement statement = conn.createStatement()) {
                return function.apply(statement);
            }
        });
    }

    public void statement(SQLConsumer<? super Statement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (Statement statement = conn.createStatement()) {
                consumer.accept(statement);
            }
        });
    }

    public void preparedStatement(String sql, SQLConsumer<? super PreparedStatement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                consumer.accept(preparedStatement);
            }
        });
    }

    /********************** hibernate **********************************/

    private final SessionFactory sessionFactory;

    public Util() {
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(getResourceMap()).build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        Reflections reflections = new Reflections("jm.task.core.jdbc.model");
        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Entity.class);

        classSet.forEach(metadataSources::addAnnotatedClass);

        SessionFactoryBuilder builder =metadataSources.getMetadataBuilder().build()
                .getSessionFactoryBuilder();

        sessionFactory = builder.build();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private Map<Object, Object> getResourceMap() {
        Properties properties = new Properties();
        try (InputStream inputStream = Util.class.getResourceAsStream("/hibernate.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {

        }

         return properties;
    }
}
