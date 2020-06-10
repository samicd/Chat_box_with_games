package Login;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Mel Thong
 *
 */

public class DatabaseConnection {

    public static Connection getConnection(){
        String databaseUrl;
        String databaseUser;
        String databasePassword;

        Connection connection = null;

        try {
            FileInputStream input = new FileInputStream(new File("/Users/Kafka/Desktop/CSMSc/SWJavaFiles/workingProject/src/database.properties"));
            Properties properties = new Properties();

            properties.load(input);

            databaseUser = properties.getProperty("user");
            databasePassword = properties.getProperty("password");
            databaseUrl = properties.getProperty("URL");

            connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
            return connection;

        } catch (IOException | SQLException exception){
            System.out.println("connection not established");
            return null;
        }
    }

    public static void main(String[] args){
        getConnection();
    }
}
