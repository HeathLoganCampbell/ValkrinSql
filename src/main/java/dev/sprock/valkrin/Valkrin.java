package dev.sprock.valkrin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import dev.sprock.valkrin.commons.SQLConsumer;
import dev.sprock.valkrin.gson.ExposeExlusion;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Valkrin
{
    public static Gson GSON;
    private HikariDataSource hikari;

    public Valkrin(HikariDataSource hikari)
    {
        this.hikari = hikari;

        GSON = createPrettyGson();
    }

    public Valkrin(String hostAddress, String port, String database, String username, String password)
    {

        hikari = new HikariDataSource();
        hikari.setDriverClassName("com.mysql.jdbc.Driver");
        hikari.setJdbcUrl("jdbc:mysql://" + hostAddress + ":" + port + "/" + database);
        if(username != null) hikari.setUsername(username);
        if(password != null) hikari.setPassword(password);
        hikari.setConnectionTestQuery("show tables");
        hikari.setMaximumPoolSize(15);
        hikari.setConnectionTimeout(2000);

        GSON = createPrettyGson();
    }

    public Valkrin(String hostAddress, String port, String database)
    {
        this(hostAddress, port, database, null, null);
    }

    protected  Gson createPrettyGson()
    {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(new ExposeExlusion())
                .addDeserializationExclusionStrategy(new ExposeExlusion())
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    private Connection getConnection() throws SQLException
    {
        return hikari.getConnection();
    }

    public void execute(String statement, SQLConsumer<PreparedStatement> prepared) {
        getCon(con -> {
            PreparedStatement prep = con.prepareStatement(statement);
            prepared.accept(prep);
            prep.execute();
        });
    }


    public void getResult(String query, SQLConsumer<PreparedStatement> params, SQLConsumer<ResultSet> rsConsumer)
    {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            PreparedStatement prestmt =con.prepareStatement(query);
            params.accept(prestmt);
            rs = prestmt.executeQuery();
            rsConsumer.accept(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeAndLastKey(String query, SQLConsumer<PreparedStatement> params, SQLConsumer<ResultSet> rsConsumer) {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            PreparedStatement prestmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            params.accept(prestmt);
            prestmt.execute();
            rs = prestmt.getGeneratedKeys();
            rsConsumer.accept(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCon(SQLConsumer<Connection> conConsumer) {
        Connection con = null;
        try {
            con = getConnection();
            conConsumer.accept(con);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
