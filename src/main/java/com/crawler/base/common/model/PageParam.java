package com.crawler.base.common.model;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class PageParam implements Serializable,Pageable{
	/**
	  * @Fields serialVersionUID :
	  */

	private static final long serialVersionUID = -7011663419520502492L;
	private final Sort sort;
	private Integer page = 0;
	private Integer size = 30;
	/**
	 * 当pageFlag=1,不进行总数count的查询
	 */
	private Integer pageFlag = 0;

	private String sidx;
	private String sord;

	public String getSord() {
		return sord;
	}

	public void setSord(String sord) {
		this.sord = sord;
	}

	public String getSidx() {
		return sidx;
	}

	public void setSidx(String sidx) {
		this.sidx = sidx;
	}

	/**
	 * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0
	 * for {@code page} will return the first page.
	 *
	 * @param page
	 *            zero-based page index.
	 * @param size
	 *            the size of the page to be returned.
	 */
	public PageParam(Integer page, Integer size) {
		this(page, size, null, null);
	}

	public PageParam(Integer page, Integer size, Integer pageFlag) {
		this(page, size, pageFlag, null);
	}

	/**
	 * Creates a new {@link PageRequest} with sort parameters applied.
	 *
	 * @param page
	 *            zero-based page index.
	 * @param size
	 *            the size of the page to be returned.
	 * @param direction
	 *            the direction of the {@link Sort} to be specified, can be
	 *            {@literal null}.
	 * @param properties
	 *            the properties to sort by, must not be {@literal null} or empty.
	 */
	public PageParam(int page, int size, Direction direction, String... properties) {
		this(page, size, Sort.by(direction, properties));
	}

	/**
	 * Creates a new {@link PageRequest} with sort parameters applied.
	 *
	 * @param page
	 *            zero-based page index.
	 * @param size
	 *            the size of the page to be returned.
	 * @param sort
	 *            can be {@literal null}.
	 */
	public PageParam(Integer page, Integer size, Sort sort) {
		if (page != null)
			this.page = page;
		if (size != null)
			this.size = size;
		this.sort = sort;
		if (this.size == null || size == 0)
			this.pageFlag = 1;
	}

	public PageParam(Integer page, Integer size, Integer pageFlag, Sort sort) {
		if (page != null)
			this.page = page;
		if (size != null)
			this.size = size;
		this.pageFlag = pageFlag;
		if (this.size == null || this.size == 0)
			this.pageFlag = 1;
		this.sort = sort;
	}

	/**
	 * 抽取service PageData      page 已在controller减一
	 * @param pageInfo
	 * @param list
	 * @param pageData
	 */
	public static void extractedServicePageData(PageParam pageInfo, List list, PageData pageData) {
		if(list != null && pageInfo != null && pageData !=null) {
			int rows = pageInfo.getPageSize();
			int page = pageInfo.getPageNumber();
			int total =  list.size()%rows>0?(list.size()/rows)+1: list.size()/rows;
			int start = (page)*rows;
			int end = (page +1)*rows> list.size()? list.size():(page +1)*rows;
			pageData.setRows(list.subList(start, end));
			pageData.setPage(page+1);
			pageData.setTotal(total);
			pageData.setRecords(list.size());
		}
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.domain.Pageable#getSort()
	 */
	public Sort getSort() {
		return sort;
	}

	@Override
	public Sort getSortOr(Sort sort) {
		return Pageable.super.getSortOr(sort);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.domain.Pageable#next()
	 */
	public Pageable next() {
		return new PageParam(getPageNumber() + 1, getPageSize(), getSort());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.domain.AbstractPageRequest#previous()
	 */
	public PageParam previous() {
		return getPageNumber() == 0 ? this : new PageParam(getPageNumber() - 1, getPageSize(), getSort());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.domain.Pageable#first()
	 */
	public Pageable first() {
		return new PageParam(0, getPageSize(), getSort());
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PageParam)) {
			return false;
		}

		PageParam that = (PageParam) obj;

		boolean sortEqual = this.sort == null ? that.sort == null : this.sort.equals(that.sort);

		return super.equals(that) && sortEqual;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * super.hashCode() + (null == sort ? 0 : sort.hashCode());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Page param [number: %d, size %d, sort: %s]", getPageNumber(), getPageSize(),
				sort == null ? null : sort.toString());
	}

	@Override
	public boolean isPaged() {
		return Pageable.super.isPaged();
	}

	@Override
	public boolean isUnpaged() {
		return Pageable.super.isUnpaged();
	}

	@Override
	public int getPageNumber() {
		return page;
	}

	@Override
	public int getPageSize() {
		return size;
	}

	@Override
	public long getOffset() {
		return page * size;
	}

	@Override
	public Pageable previousOrFirst() {
		return hasPrevious() ? previous() : first();
	}

	@Override
	public boolean hasPrevious() {
		return page > 0;
	}

	@Override
	public Optional<Pageable> toOptional() {
		return Pageable.super.toOptional();
	}

	public Integer getPageFlag() {
		if (pageFlag == null)
			pageFlag = 0;
		return pageFlag;
	}

	public void setPageFlag(Integer pageFlag) {
		this.pageFlag = pageFlag;
	}

}
