package cn.edu.szu.cs.adapter;

import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.DefaultRelevantObject;
import cn.edu.szu.cs.infrastructure.dataloader.IRelevantObjectDataLoader;
import cn.edu.szu.cs.infrastructure.dataloader.RelevantObjectDataLoaderImpl;
import cn.edu.szu.cs.infrastructure.ds.WordOrderingIndex;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ServiceLoaderUtil;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * 用于接受并处理请求的管理器
 * <p> Implement the manager to accept and process requests
 * @author Whitence
 * @date 2024/4/6 19:30
 * @version 1.0
 */
@SuppressWarnings("all")
public class KstcDataFetchManager {

    private static DataFetchManager dataFetchManager = null;

    private static IRelevantObjectDataLoader<DefaultRelevantObject> dbscanDataLoader = null;

    private static WordOrderingIndex wordOrderingIndex = null;

    static {



        try {
            File objFile = new ClassPathResource("objs.zip").getFile();
            ZipFile objZipFile = new ZipFile(objFile);
            InputStream stream = objZipFile.getInputStream(objZipFile.entries().nextElement());
            dbscanDataLoader = new RelevantObjectDataLoaderImpl<>(stream, DefaultRelevantObject.class);
            Assert.notNull(dbscanDataLoader, "dbscanDataLoader is null");


            File file = new ClassPathResource("wordOrderingIndex_10_100.zip").getFile();
            ZipFile zipFile = new ZipFile(file);
            InputStream inputStream = zipFile.getInputStream(zipFile.entries().nextElement());
            wordOrderingIndex = new WordOrderingIndex(inputStream);
            Assert.notNull(wordOrderingIndex, "wordOrderingIndex is null");

            dataFetchManager = ServiceLoaderUtil.loadFirst(DataFetchManager.class);

            if(dataFetchManager == null){
                throw new RuntimeException("No implementation of DataFetchManager found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static IRelevantObjectDataLoader<DefaultRelevantObject> getDataLoader() {
        return dbscanDataLoader;
    }

    public static WordOrderingIndex getWordOrderingIndex() {
        return wordOrderingIndex;
    }


    public static void setDataFetchManager(DataFetchManager dataFetchManager) {
        KstcDataFetchManager.dataFetchManager = dataFetchManager;
    }


    public static void generateTask(String commandType, String command, String paramsStr){
        dataFetchManager.generateTask(commandType, command, paramsStr);
    }

    @NonNull
    public static List<DataFetchResult> listTask(){
        return dataFetchManager.listTask();
    }


    @NonNull
    public static DataFetchResult generateTaskAndGet(String commandType, String command, String paramsStr){
        return dataFetchManager.generateTaskAndGet(commandType, command, paramsStr);
    }

}
