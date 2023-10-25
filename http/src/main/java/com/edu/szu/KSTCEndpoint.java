package com.edu.szu;

import cn.edu.szu.cs.KSTC;
import cn.edu.szu.cs.KstcCluster;
import cn.edu.szu.cs.Query;
import cn.edu.szu.cs.SimpleKSTC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stc")
public class KSTCEndpoint {

    private KSTC kstc = new SimpleKSTC();

    /**
     * search top-k cluster
     * @param query
     * @return
     */
    @PostMapping("/search")
    public List<KstcCluster> kstcSearch(@RequestBody Query query){
        return kstc.kstcSearch(query);
    }


}
