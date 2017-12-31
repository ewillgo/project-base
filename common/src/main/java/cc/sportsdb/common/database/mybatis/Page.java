package cc.sportsdb.common.database.mybatis;

import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> dataList;
    private int totalPage = 0;
    private int pageNo = 1;
    private int pageSize = 10;
    private long totalRecord = 0;
    private boolean firstPage;
    private boolean lastPage;

    public boolean isFirstPage() {
        return firstPage;
    }

    void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    public int getTotalPage() {
        return totalPage;
    }

    void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
    }

    @Override
    public String toString() {
        return "Page{" +
                "dataList=" + dataList +
                ", totalPage=" + totalPage +
                ", pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", totalRecord=" + totalRecord +
                ", firstPage=" + firstPage +
                ", lastPage=" + lastPage +
                '}';
    }
}
