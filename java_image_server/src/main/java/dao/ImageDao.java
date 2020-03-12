package dao;

import common.JavaImageServerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Dennis
 * @date: 2020/3/1 14:14
 */

public class ImageDao {
    /**
     * 把 image 对象插入到数据库中
     * @param image
     */
    public void insert(Image image){
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2.创建并拼接SQL 语句
        String sql = "insert into image_table values(null, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, image.getImageName());
            statement.setInt(2,image.getSize());
            statement.setString(3,image.getUploadTime());
            statement.setString(4,image.getContentType());
            statement.setString(5,image.getPath());
            statement.setString(6,image.getMd5());

            //3. 执行 SQL 语句
            int ret = statement.executeUpdate();
            if (ret != 1){
                // 程序出现问题，抛出一个异常
                throw new JavaImageServerException("插入数据库出错");
            }

        } catch (SQLException | JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            //4. 关闭连接和 statement 对象
            DBUtil.close(connection, statement, null);
        }
    }

    /**
     * 查找数据库中所有图片信息
     * @return
     */
    public List<Image> selectAll(){
        List<Image> images = new ArrayList<>();
        //1.获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2.构造 SQL 语句
        String sql = "select * from image_table";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        //3.执行 SQL 语句
        try {
            statement = connection.prepareStatement(sql);
             resultSet = statement.executeQuery();
            //4.处理结果集
            while(resultSet.next()){
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
            }
            return images;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            //5.关闭连接
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    /**
     * 根据 imageId 查找指定图片信息
     * @param imageId
     * @return
     */

    public Image selectOne(int imageId){
        return null;
    }

    /**
     * 根据 imageId 删除指定图片
     *
     * @param imageId
     */
    public void delect(int imageId) {

    }

    //需要部署到数据库所在的云服务器上才可以运行
    //打一个 jar 包，把 jar 包上传到云服务器上，才可以执行
    public static void main(String[] args) {
        //用于进行简单测试
        //1.测试插入数据
//        Image image = new Image();
//        image.setImageName("1.png");
//        image.setSize(100);
//        image.setUploadTime("20200216");
//        image.setContentType("image/png");
//        image.setPath("./data/1.png");
//        image.setMd5("11223344");
//        ImageDao imageDao = new ImageDao();
//        imageDao.insert(image);

    }
}
