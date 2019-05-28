package cn.itcast.core.controller.upload;

import cn.itcast.core.entity.Result;
import cn.itcast.core.util.fdfs.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName UploadController
 * @Description 文件上传
 * @Author 传智播客
 * @Date 10:10 2019/5/8
 * @Version 2.1
 **/
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    /**
     * @author 栗子
     * @Description 附件的上传
     * @Date 10:11 2019/5/8
     * @param file
     * @return cn.itcast.core.entity.Result
     **/
    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file){
        try{
            // 将附件上传到FastDFS中（工具类）
            String conf = "classpath:fastDFS/fdfs_client.conf";
            FastDFSClient fastDFSClient = new FastDFSClient(conf);
            // 文件的扩展名
            String filename = file.getOriginalFilename();   // 1.jpg
            String extName = FilenameUtils.getExtension(filename);  // 直接获取到扩展名
            // 上传
            String url = fastDFSClient.uploadFile(file.getBytes(), extName, null);
            // url并不完整，需要拼接服务器地址
            // 常量的维护：配置文件、枚举、类（接口：对修改关闭 对扩展开放）
            // 将url的地址：交给配置文件进行维护
            url =  FILE_SERVER_URL + url;
            return new Result(true, url);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Result(false, "上传失败");
    }
}
