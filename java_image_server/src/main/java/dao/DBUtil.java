package dao;
//dao 数据访问层

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: Dennis
 * @date: 2020/2/22 17:21
 */

public class DBUtil {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/java_image_server?charterEncoding=utf8&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD ="19981017";

    private static volatile DataSource dataSourse =null;
    // dataSourse   单例模式是否线程安全
    //加锁   双重判定   volatile(内存可见)
    public static DataSource getDataSourse(){
        if (dataSourse == null) {
            synchronized (DBUtil.class) {
                if (dataSourse == null){
                    dataSourse = new MysqlDataSource();
                    MysqlDataSource tmpDataSourse = (MysqlDataSource) dataSourse;
                    tmpDataSourse.setURL(URL);
                    tmpDataSourse.setUser(USERNAME);
                    tmpDataSourse.setPassword(PASSWORD);
                }
            }
        }
        return dataSourse;
    }

    public static Connection getConnection()  {
        try {
            return getDataSourse().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet)  {
        try {
            if (resultSet != null){
                resultSet.close();
            }
            if (statement != null){
                statement.close();
            }
            if (connection != null){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
