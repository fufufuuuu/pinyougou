package cn.itcast.core.controller.upload;

import cn.itcast.core.entity.Result;
import cn.itcast.core.util.fdfs.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file) {
        try {
            //将附件上传到FastDFS中
            String conf = "classpath:fdfs_client.conf";
            FastDFSClient fastDFSClient = new FastDFSClient(conf);

            //文件扩展名
            String filename = file.getOriginalFilename();
            String extName = FilenameUtils.getExtension(filename); // 直接获取扩展名
            //上传
            String url = fastDFSClient.uploadFile(file.getBytes(),extName,null);
            //拼接服务器地址
            url = FILE_SERVER_URL+url;
            return new Result(true,url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"上传失败");
        }
    }
}
