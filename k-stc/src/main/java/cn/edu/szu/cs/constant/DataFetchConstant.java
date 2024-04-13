package cn.edu.szu.cs.constant;

/**
 *  记录数据获取命令常量类
 *  <p> It is a constant class that records the data acquisition command.
 * @author Whitence
 * @date 2024/4/7 0:35
 * @version 1.0
 */

public class DataFetchConstant {

    // Command Type
    /**
     * 运算层
     * <p> Operation Layer
     */
    public static final String OPERATIONAL_LAYER = "OPERATIONAL_LAYER";

    /**
     * 基础设施层
     * <p> Infrastructure Layer
     */
    public static final String INFRASTRUCTURE_LAYER = "INFRASTRUCTURE_LAYER";

    //Command Name

    /**
     * 基于DBSCAN的简单方法
     * <p> Simple method based on DBSCAN
     */
    public static final String SIMPLE_DBSCAN_BASED_APPROACH = "SIMPLE_DBSCAN_BASED_APPROACH";
    public static final String DBSCAN_BASED_APPROACH = "DBSCAN_BASED_APPROACH";

    /**
     * OPTICS-OG
     * <p> OPTICS-OG
     */
    public static final String OPTICS_BASED_APPROACH_OG = "OPTICS_BASED_APPROACH_OG";

    /**
     * OPTICS-OM
     * <p> OPTICS-OM
     */
    public static final String OPTICS_BASED_APPROACH_OM = "OPTICS_BASED_APPROACH_OM";

    /**
     * 通过关键词加载DBSCAN数据
     * <p> Load DBSCAN data by keywords
     */
    public static final String LOAD_DBSCAN_DATA_BY_KEYWORDS = "LOAD_DBSCAN_DATA_BY_KEYWORDS";

    /**
     * 通过关键词加载OPTICS数据
     * <p> Load OPTICS data by keywords
     */
    public static final String LOAD_OPTICS_DATA_BY_KEYWORDS = "LOAD_OPTICS_DATA_BY_KEYWORDS";

    /**
     * 通过关键词加载DBSCAN-RTree数据
     * <p> Load DBSCAN-RTree data by keywords
     */
    public static final String LOAD_DBSCAN_RTREE_DATA_BY_KEYWORDS = "LOAD_DBSCAN_RTREE_DATA_BY_KEYWORDS";

    /**
     * 通过关键词加载OPTICS-RTree数据
     * <p> Load OPTICS-RTree data by keywords
     */
    public static final String LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS = "LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS";


    public static final String DBSCAN_RTREE_RANGE_QUERY = "DBSCAN_RTREE_RANGE_QUERY";


    public static final String OPTICS_RTREE_RANGE_QUERY = "OPTICS_RTREE_RANGE_QUERY";
    public static final String WORD_ORDERING_INDEX = "WORD_ORDERING_INDEX";
    public static final String PREFIX_MATCH_KEYWORDS = "PREFIX_MATCH_KEYWORDS";
}
