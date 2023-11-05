package cn.edu.szu.cs;

import java.util.List;

/**
 *  IRelatedObjectService
 * @author Whitence
 * @date 2023/11/1 22:06
 * @version 1.0
 */
public interface IRelatedObjectService {

    RelatedObject getById(String id);


    List<RelatedObject> getByIds(List<String> ids);

    List<String> getLabelsById(String id);


}
