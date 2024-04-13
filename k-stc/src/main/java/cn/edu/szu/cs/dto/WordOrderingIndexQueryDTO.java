package cn.edu.szu.cs.dto;

import cn.edu.szu.cs.entity.BaseDataFetchActionParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WordOrderingIndexQueryDTO extends BaseDataFetchActionParams {

    private String keyword;
}
