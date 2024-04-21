package cn.edu.szu.cs;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.infrastructure.dataloader.RelevantObjectDataLoaderImpl;
import cn.hutool.core.io.resource.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipFile;


public class TestMain {
    public static void main(String[] args)throws Exception {

        File file = new ClassPathResource("objs.zip").getFile();
        ZipFile zipFile = new ZipFile(file);
        InputStream stream = zipFile.getInputStream(zipFile.entries().nextElement());
        List<DbScanRelevantObject> all = new RelevantObjectDataLoaderImpl<>(stream, DbScanRelevantObject.class).getAll();
        System.out.println(all.size());
    }
}
