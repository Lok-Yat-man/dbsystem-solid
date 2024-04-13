package cn.edu.szu.cs.kstc;

import java.util.List;

/**
 * RelatedObject
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/11/1 21:53
 */
public interface RelevantObject {

    /**
     * get object id
     *
     * @return
     */
    String getObjectId();

    /**
     * set object id
     * @param objectId
     */
    void setObjectId(String objectId);

    /**
     * get coordinate
     *
     * @return
     */
    double[] getCoordinate();

    /**
     * set coordinate
     * @param coordinate
     */
    void setCoordinate(double[] coordinate);

    /**
     * get name
     *
     * @return
     */
    String getName();

    /**
     * set name
     * @param name
     */
    void setName(String name);

    /**
     * get labels
     *
     * @return
     */
    List<String> getLabels();

    /**
     * set labels
     * @param labels
     */
    void setLabels(List<String> labels);

    /**
     * equals
     *
     * @return
     */
    @Override
    int hashCode();

}
