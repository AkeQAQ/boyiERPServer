package com.boyi.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    public static void writeFile(InputStream fis,String outFilePath,String fileName)throws Exception{
        FileOutputStream fos = new FileOutputStream(outFilePath + File.separator + fileName);
        byte[] bytes=new byte[1024];
        int temp=0;
        while((temp=fis.read(bytes))!=-1){
            fos.write(bytes);
        }
        fos.flush();
        fos.close();

    }
}
