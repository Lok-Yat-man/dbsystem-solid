package cn.edu.szu.cs.infrastructure.ds;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class WordOrderingIndexTest {


    public static void main(String[] args) throws Exception{

        File objFile = new ClassPathResource("objs.zip").getFile();
        ZipFile objZipFile = new ZipFile(objFile);
        InputStream inputStream = objZipFile.getInputStream(objZipFile.entries().nextElement());

        FileOutputStream fileOutputStream = new FileOutputStream("E:\\JAVA_Files\\dbsystem-solid\\k-stc\\src\\main\\resources\\wordOrderingIndex_5_200.txt");
        WordOrderingIndex.generateWordOrderingIndexFile(inputStream, fileOutputStream,5,200);
        fileOutputStream.close();
        ZipUtil.zip("E:\\JAVA_Files\\dbsystem-solid\\k-stc\\src\\main\\resources\\wordOrderingIndex_5_200.txt","E:\\JAVA_Files\\dbsystem-solid\\k-stc\\src\\main\\resources\\wordOrderingIndex_5_200.zip");

        inputStream.close();

    }
}
