package cn.edu.szu.cs;

import java.io.Serializable;
import java.util.List;
/**
 *  IFile
 * @author Whitence
 * @date 2023/10/22 21:11
 * @version 1.0
 */
public class IFile implements Serializable {

    private String keyword;

    private List<Pair> pairs;

    IFile(){}

    public IFile(String keyword, List<Pair> pairs) {
        this.keyword = keyword;
        this.pairs = pairs;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    public void setPairs(List<Pair> pairs) {
        this.pairs = pairs;
    }
}
