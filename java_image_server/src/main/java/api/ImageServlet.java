package api;

import dao.Image;
import dao.ImageDao;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author: Dennis
 * @date: 2020/3/14 11:16
 */

public class ImageServlet extends HttpServlet {
    /**
     * 查看图片属性：既能查看所有，也能查看指定
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // req  请求   方法 url，各种 header，body
        // resp 响应   状态码，各种 header，body
        resp.setStatus(200);
        // 把 hello 字符串放到 http 响应的 body 中
        resp.getWriter().write("hello");
        // 修改 webapp/WEB-INF/web.xml 把新创建的 servelet 注册进去
    }

    /**
     *上传图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //resp.setContentType("text/html; charset=utf-8");
        //1. 获取图片属性信息，并且存入数据库
        // a.需要创建一个 factory 对象和 upload 对象,为了获取图片属性
        //       固定逻辑
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        // b.通过 upload 对象进一步解析（解析HTTP请求中的 body 中的内容）
        //    FileItem 代表一个上传的文件对象
        //    理论上来说，HTTP 支持一个请求中同时上传多个文件
        List<FileItem> items = null;
        try {
             items = upload.parseRequest(req);
        } catch (FileUploadException e) {
            // 出现异常说明解析出错
            e.printStackTrace();

            // 告诉客户端出现的具体错误是什么
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{\"ok\": false,\"reason\":\"请求解析失败\"}");
            return;
        }
        // c. 把 FileItem 中的属性提取出来，转换成 Image 对象，才能存到数据库中
        //     当前只考虑一张图片的情况
        FileItem fileItem = items.get(0);
        Image image = new Image();
        image.setImageName(fileItem.getName());
        image.setSize((int)fileItem.getSize());
        // 手动获取当前日期，并且转换成格式化日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        image.setUploadTime(simpleDateFormat.format(new Date()));
        image.setContentType(fileItem.getContentType());
        // 自己构造一个路径来保存
        image.setPath("./image/" + image.getImageName());
        //  MD5 暂时不考虑
        image.setMd5("11223344");
        // 存到数据库中
        ImageDao imageDao = new ImageDao();
        imageDao.insert(image);

        //2. 获取图片内容信息，并且写入磁盘文件
        File file = new File(image.getPath());
        try {
            fileItem.write(file);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{\"ok\": false,\"reason\":\"写如磁盘失败\"}");
            return;
        }

        //3. 给给客户端返回一个结果数据
        resp.setContentType("application/json; charset=utf-8");
        resp.getWriter().write("{\"ok\":ture}");
    }

    /**
     * 删除指定图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}
