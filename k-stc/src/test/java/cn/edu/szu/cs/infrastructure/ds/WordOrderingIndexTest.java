package cn.edu.szu.cs.infrastructure.ds;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class WordOrderingIndexTest {


    public static void main(String[] args) throws Exception{

        FileInputStream fileInputStream = new FileInputStream("E:\\JAVA_Files\\dbsystem-solid\\k-stc\\src\\main\\resources\\objs.txt");

        FileOutputStream fileOutputStream = new FileOutputStream("E:\\JAVA_Files\\dbsystem-solid\\k-stc\\src\\main\\resources\\wordOrderingIndex.txt");

        WordOrderingIndex.generateWordOrderingIndexFile(fileInputStream, fileOutputStream);

        fileInputStream.close();
        fileOutputStream.close();

    }
}
