package cn.edu.szu.cs.kstc.optics;

import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;

import java.util.List;
import java.util.Set;
/**
 *  OpticsBasedApproach
 * @author Whitence
 * @date 2024/4/5 12:03
 * @version 1.0
 */
public class OpticsBasedApproach implements TopKSpatialTextualClustersRetrieval<OpticsRelevantObject> {


    public interface WordOrderIndex {

    }

    @Override
    public List<Set<OpticsRelevantObject>> kstcSearch(KstcQuery query) {
        return null;
    }




}
