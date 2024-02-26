import irtree.IRTree;
import org.junit.jupiter.api.Test;
import service.DefaultRelevantObjectServiceImpl;
import service.IRelevantObjectService;

public class IRTreeTest {
    @Test
    public void testIRTree(){

        IRelevantObjectService relevantObjectService = new DefaultRelevantObjectServiceImpl();

        IRTree IRTree = new IRTree(relevantObjectService);

        System.out.println(IRTree.getRTree().asString());
    }
}
