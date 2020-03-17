package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        // 考虑查看所有图片属性和查看指定图片属性
        // 通过是否 URL 中带有 imageId 参数来进行区分
        // 存在 imageId 查看指定图片属性，否则就查看所有图片属性
        // 如果URL /image?imageId=100
        // imageId 的值就是 "100"
        // 如果 URL 中不存在 imageId 那么返回 null
        String imageId = req.getParameter("imageId");
        if (imageId == null || imageId.equals("")){
            // 查看所有图片属性
            selectAll(req,resp);
        }else {
            // 查看指定图片
            selectOne(imageId,resp);
        }
    }
    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=utf-8");
        // 1.创建一个 ImageDao 对象，并查找数据库
        ImageDao imageDao = new ImageDao();
        List<Image> images = imageDao.selectAll();
        // 2.把查找到的结果转成 JSON 格式的字符串，并且写回给 resp 对象
        Gson gson = new GsonBuilder().create();
        //    jsonData 就是一个 json 格式的字符串，和之前约定的格式是一样的
        //    重点是下面这行代码，gson 帮我们完成了大量的格式转换
        //    只要把相关的字段都约定成统一的命名，下面的操作就可以一步到位的完成转换
        String jsonData = gson.toJson(images);
        resp.getWriter().write(jsonData);
    }
    private void selectOne(String imageId, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=utf-8");
        //1. 创建 ImageDao 对象
        ImageDao imageDao = new ImageDao();
        Image image = imageDao.selectOne(Integer.parseInt(imageId));
        // 2. 使用 gson 把查到的数据转成 json 格式，并写回给响应对象
        Gson gson = new GsonBuilder().create();
        String jsonData = gson.toJson(image);
        resp.getWriter().write(jsonData);
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
        //  MD5 暂时不考虑
        image.setMd5(DigestUtils.md5Hex(fileItem.get()));
        // 自己构造一个路径来保存
        //           加入时间戳使文件路径唯一
        image.setPath("./image/" + image.getMd5());
        // 存到数据库中
        ImageDao imageDao = new ImageDao();
        // 查看数据库中是否存在相同 MD5 值的图片，不存在 返回 null
        Image existImage = imageDao.selectByMd5(image.getMd5());

        imageDao.insert(image);

        //2. 获取图片内容信息，并且写入磁盘文件
        if (existImage == null) {
            File file = new File(image.getPath());
            try {
                fileItem.write(file);
            } catch (Exception e) {
                e.printStackTrace();
                resp.setContentType("application/json; charset=utf-8");
                resp.getWriter().write("{\"ok\": false,\"reason\":\"写如磁盘失败\"}");
                return;
            }
        }

        //3. 给给客户端返回一个结果数据
//        resp.setContentType("application/json; charset=utf-8");
//        resp.getWriter().write("{\"ok\":ture}");
        resp.sendRedirect("index.html");
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
        // 1. 先获取到请求中的 imageId
        String imageId = req.getParameter("imageId");
        if (imageId == null || imageId.equals("")){
            resp.setStatus(200);
            resp.getWriter().write("{\"ok\":false,\"reason\":\"解析请求失败\"}");
            return;
        }
        // 2. 创建 ImageDao 对象，查看到该图片对象对应的相关属性（这是为了只知道图片的对应文件路径）
        ImageDao imageDao = new ImageDao();
        Image image = imageDao.selectOne(Integer.parseInt(imageId));
        if (image == null){
            // 此时请求中传入的 id 在数据库中不存在
            resp.setStatus(200);
            resp.getWriter().write("{\"ok\":false,\"reason\":\"imageId在数据库中不存在\"}");
        }
        // 3. 删除本地库中的记录
        imageDao.delete(Integer.parseInt(imageId));
        // 4. 删除本地磁盘文件
        Image existImage = imageDao.selectByMd5(image.getMd5());
        if (existImage == null) {
            File file = new File(image.getPath());
            file.delete();
        }
        resp.setStatus(200);
        resp.getWriter().write("{\"ok\":ture}");
    }
}
