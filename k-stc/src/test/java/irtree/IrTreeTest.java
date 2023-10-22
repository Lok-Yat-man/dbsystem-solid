package irtree;

import cn.edu.szu.cs.DefaultRelevantObjectServiceImpl;
import cn.edu.szu.cs.IRelevantObjectService;
import cn.edu.szu.cs.SimpleIRTree;
import org.junit.Test;

public class IrTreeTest {

    @Test
    public void testSimpleIrTree(){

        IRelevantObjectService relevantObjectService = new DefaultRelevantObjectServiceImpl();

        SimpleIRTree simpleIRTree = new SimpleIRTree(relevantObjectService);



    }


}
