package com.sean.usercenter.utils;
 
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.InputStream;
import java.util.UUID;
 
@Component
public class OSSutils {
 
    // 访问域名，参照官方地址：https://help.aliyun.com/zh/oss/user-guide/regions-and-endpoints?spm=a2c4g.11186623.0.i1#concept-zt4-cvy-5db
    @Value("${aliyun-oss.endpoint}")
    private String endpoint;
 
    // 存储空间名称，在OSS的控制台上创建存储空间时会得到。官方地址：https://oss.console.aliyun.com/bucket
    @Value("${aliyun-oss.bucketName}")
    private String bucketName;
 
    public String uploadFileToOSS(MultipartFile file) throws Exception{
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
 
        // 获取上传文件的输入流
        InputStream inputStream = file.getInputStream();
 
        // 使用UUID通用唯一识别码 + 后缀名的形式，方式文件名重复导致覆盖
        String orignalFilename = file.getOriginalFilename();
        // 增加断言null值抛出异常
        assert orignalFilename != null;
        String fileName = UUID.randomUUID().toString() + orignalFilename.substring(orignalFilename.lastIndexOf("."));
 
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = fileName;
 
        // 文件上传到服务器之后的文件访问路径
        String url = "";
        try {
            ossClient.putObject(bucketName, objectName, inputStream);
 
            //拼接文件访问路径并返回  在endpoint名称中加入bucket名称 最后拼接上文件名
            url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error MessageVo:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error MessageVo:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }
}