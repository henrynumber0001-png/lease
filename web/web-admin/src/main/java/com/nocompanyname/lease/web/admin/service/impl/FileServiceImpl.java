package com.nocompanyname.lease.web.admin.service.impl;

import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.minio.MinioProperties;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    MinioClient minioClient; //minio的依赖包含了 MinioClient 类，所以可以直接注入使用

    @Autowired
    MinioProperties minioProperties; //注入 MinioProperties 获取配置信息

    @Override
    public String upload(MultipartFile file) {
        if(file == null || file.isEmpty()){
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        try {
            boolean bucketExsists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!bucketExsists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build());
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .config(createBucketPolicyConfig(minioProperties.getBucketName()))
                                .build());
            }
                String filename = new SimpleDateFormat("yyyyMMdd").format(new Date())
                        + "/"
                        + UUID.randomUUID()
                        + "-"
                        + file.getOriginalFilename();

                //无论 bucket 原来存不存在，都要上传文件
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .stream(file.getInputStream(), file.getSize(), -1)// objectSize = -1 时，partSize 不能也是 -1，例如：.stream(inputStream, -1, 10 * 1024 * 1024)，表示对象总大小未知，但你必须指定 partSize。
                                .object(filename)
                                .contentType(file.getContentType())
                                .build()
                );

                //无论 bucket 原来存不存在，都要返回 URL
                return String.join(
                        "/",
                        minioProperties.getEndpoint(),
                        minioProperties.getBucketName(),
                        filename);
                /*
                第一次上传：bucket 不存在 -> 创建 bucket -> 上传文件 -> 返回 URL
                第二次上传：bucket 已存在 -> 直接上传文件 -> 返回 URL
                 */

        } catch (Exception e) { //接收任意Exception及其子类的异常，随后重新抛出 LeaseException异常，统一返回“服务异常”
            /*
            因为这个 catch 捕获的是 try 里面 MinIO / IO 代码抛出来的原始异常
            可能抛出的异常有很多种，比如：
            IOException
            ServerException
            ErrorResponseException
            InvalidKeyException
            NoSuchAlgorithmException
            InternalException
            这些异常都不是 LeaseException，因此要用Exception 接
             */
            throw new LeaseException(ResultCodeEnum.SERVICE_ERROR,e);
            /*
            但是捕获之后，统一包装成 LeaseException（业务异常），然后再次抛出
            目的就是告诉前端，服务异常了，但不暴露具体异常信息给前端，方便后端排查问题
            这里的 e 是原始异常，也叫 cause，是给后端排查用的
            但是给前端统一返回“服务异常”
             */
        }
    }
    private String createBucketPolicyConfig(String bucketName){
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": [
                        "s3:GetObject"
                      ],
                      "Resource": [
                        "arn:aws:s3:::%s/*"
                      ]
                    }
                  ]
                }
                """.formatted(bucketName, bucketName);
    }
}
