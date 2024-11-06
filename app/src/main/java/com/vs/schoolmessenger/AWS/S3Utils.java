package com.vs.schoolmessenger.AWS;

import android.content.Context;
import android.util.Log;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class S3Utils {

    public static String generates3ShareUrl(Context applicationContext, String collegeid, String path, String fileNameDateTime) {
        File f = new File(path);
        AmazonS3 s3client = AmazonUtil.getS3Client(applicationContext);
        ResponseHeaderOverrides overrideHeader = new ResponseHeaderOverrides();
        String mediaUrl = f.getName();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        File Datefolder = new File(currentDate);
        Log.d("datefilenew", String.valueOf(Datefolder));
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(AWSKeys.BUCKET_NAME, Datefolder + "/" + fileNameDateTime + "_" + mediaUrl);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setResponseHeaders(overrideHeader);
        URL url = s3client.generatePresignedUrl(generatePresignedUrlRequest);
        String fileUrl = url.toString().substring(0, url.toString().indexOf('?'));
        Log.e("s", fileUrl);
        return fileUrl;
    }
}



