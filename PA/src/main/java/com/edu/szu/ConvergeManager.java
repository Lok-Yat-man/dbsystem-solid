package com.edu.szu;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

/**
 * @author 86136 Email:a@wk2.cn
 * @since 2024/03/19 16:30
 */
@NoArgsConstructor
@Log4j2
public class ConvergeManager {
    //public ConvergeManager(){}
    public String function(String dataPath,String outputPath) throws Exception {
        log.info("Begin to run pa");
        Converge manager = new Converge(new String[]{dataPath,outputPath,",","10","1","1","1"});
        return outputPath;
    }

}
