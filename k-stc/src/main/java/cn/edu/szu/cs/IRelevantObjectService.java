package cn.edu.szu.cs;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 *  IRelevantObjectService
 * @author Whitence
 * @date 2023/10/17 21:29
 * @version 1.0
 */
public interface IRelevantObjectService {

    RelevantObject getById(String objId);

    Map<String,Double> getWeightsById(String objId);

    List<RelevantObject> getByIds(List<String> objIds);

    List<RelevantObject> getByIds(List<String> objIds, Predicate<RelevantObject> predicate);

    List<RelevantObject> getAll();

}
