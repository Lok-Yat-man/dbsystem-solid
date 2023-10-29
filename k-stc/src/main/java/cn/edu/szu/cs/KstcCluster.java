package cn.edu.szu.cs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  KstcCluster
 * @author Whitence
 * @date 2023/10/21 16:39
 * @version 1.0
 */
public class KstcCluster implements Serializable {

    private static AtomicInteger nextId = new AtomicInteger(0);

    private int clusterId;

    private List<RelevantObject> members;

    private KstcCluster(int clusterId,Collection<RelevantObject> cluster){
        this.clusterId=clusterId;
        members=new ArrayList<>(cluster);
    }

    public static KstcCluster create(Collection<RelevantObject> clusters){
        return new KstcCluster(nextId.getAndIncrement(),clusters);
    }

    public static AtomicInteger getNextId() {
        return nextId;
    }

    public static void setNextId(AtomicInteger nextId) {
        KstcCluster.nextId = nextId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public List<RelevantObject> getMembers() {
        return members;
    }

    public void setMembers(List<RelevantObject> members) {
        this.members = members;
    }

    public static void resetId(){nextId.set(0);}
}
