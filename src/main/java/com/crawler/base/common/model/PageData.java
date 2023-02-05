package com.crawler.base.common.model;

import java.io.Serializable;
import java.util.List;
/**
 * 返回的分页信息
 * @author Administrator
 *
 */
public class PageData<T> implements Serializable {
	
	
    /**
	  * @Fields serialVersionUID : 
	  */
	
	private static final long serialVersionUID = -2251186547890983454L;
	
	/**
     * 总页数
     */
    private Integer total;
    /**
     * 当前页
     */
    private Integer page;
    /**
     * 总记录数
     */
    private Integer records;
    /**
     * 查询消耗时间
     */
    private long costTime;
    /**
     * 返回数据
     */
    private List<T> rows;
    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public Integer getPage() {
        return page;
    }
    public void setPage(Integer page) {
        this.page = page;
    }
    public Integer getRecords() {
        return records;
    }
    public void setRecords(Integer records) {
        this.records = records;
    }
    public long getCostTime() {
        return costTime;
    }
    public void setCostTime(long costtime) {
        this.costTime = costtime;
    }
    public List<T> getRows() {
        return rows;
    }
    public void setRows(List<T> rows) {
        this.rows = rows;
    } 
    
}
