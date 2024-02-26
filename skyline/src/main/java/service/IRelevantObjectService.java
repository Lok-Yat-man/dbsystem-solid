package service;

import entity.RelevantObject;

import java.util.List;
import java.util.Map;

/**
 * IRelatedObjectService
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/11/1 22:06
 */
public interface IRelevantObjectService {

    RelevantObject getById(String id);

    Map<String,Double> getWeightsById(String objId);

    List<RelevantObject> getByIds(List<String> ids);

    List<String> getLabelsById(String id);

    List<RelevantObject> getAll();


}
