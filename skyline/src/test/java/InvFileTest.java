import ivtidx.DefaultLeafInvertedIndex;
import ivtidx.InvertedIndex;
import org.junit.jupiter.api.Test;
import service.DefaultRelevantObjectServiceImpl;
import service.IRelevantObjectService;

import java.util.LinkedList;
import java.util.List;

public class InvFileTest {
    @Test
    public void testInvFile(){
        IRelevantObjectService relevantObjectService = new DefaultRelevantObjectServiceImpl();
        InvertedIndex defaultLeafInvertedIndex = new DefaultLeafInvertedIndex(relevantObjectService);
        List<String> strings = new LinkedList<>();
        //strings.add("Balloons");
        strings.add("Surf");

        //System.out.println(defaultLeafInvertedIndex.getValues("Balloons"));
        List values = defaultLeafInvertedIndex.getValues(strings);
        System.out.println(values);
        System.out.println(values.size());
        //System.out.println(relevantObjectService.getWeightsById("GldIUU-hF_Oq_zN4BE9EAg"));
        //System.out.println(defaultLeafInvertedIndex.getValues(strings));

    }
}
