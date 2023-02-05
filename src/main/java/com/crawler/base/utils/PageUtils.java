package com.crawler.base.utils;

import com.crawler.base.common.model.PageData;
import com.crawler.base.common.model.PageParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import javax.persistence.Column;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class PageUtils {
    /**
     * 将pageable成拼接sql语句
     * @param page
     * @return
     */
    public static String initPageSql(Pageable page) {
        if(page == null)
        return "";
        StringBuffer sb = new StringBuffer();
        if(page.getSort() != null) {
            Sort sort = page.getSort();
            Iterator < Order > iterator = sort.iterator();
            sb.append(" Order By");
            while(iterator.hasNext()) {
                Order order = iterator.next();
                if(order.getProperty().startsWith("custom:")) {
                	sb.append(" "+order.getProperty().substring(7, order.getProperty().length()));
                }else {
                	sb.append(" `" + order.getProperty()+"`");
                }
                sb.append(" " + order.getDirection() + ",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        if(page.getPageSize()>0)
        sb.append(" limit " + page.getOffset() + "," + page.getPageSize());
        return sb.toString();
    }
	public static String initPageSql(Pageable page,String otherSort) {
		if(page == null)
			return "";
		StringBuffer sb = new StringBuffer();
		if(page.getSort() != null) {
			Sort sort = page.getSort();
			Iterator < Order > iterator = sort.iterator();
			sb.append(" Order By");
			while(iterator.hasNext()) {
				Order order = iterator.next();
				sb.append(" `" + order.getProperty()+"`");
				sb.append(" " + order.getDirection() + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
			if (!StringUtils.isEmpty(otherSort) ) {
				sb.append(otherSort);
			}
		}
		if(page.getPageSize()>0)
			sb.append(" limit " + page.getOffset() + "," + page.getPageSize());
		return sb.toString();
	}
	public static String initOnlySortSql(Pageable page) {
		if(page == null)
			return "";
		StringBuffer sb = new StringBuffer();
		if(page.getSort() != null) {
			Sort sort = page.getSort();
			Iterator < Order > iterator = sort.iterator();
			sb.append(" Order By");
			while(iterator.hasNext()) {
				Order order = iterator.next();
				if(order.getProperty().startsWith("custom:")) {
					sb.append(" "+order.getProperty().substring(7, order.getProperty().length()));
				}else {
					sb.append(" `" + order.getProperty()+"`");
				}
				sb.append(" " + order.getDirection() + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
    public static String initOnlyPageSql(Pageable page) {
        if(page == null)
        return "";
        StringBuffer sb = new StringBuffer();
        if(page.getPageSize()>0)
        sb.append(" limit " + page.getOffset() + "," + page.getPageSize());
        return sb.toString();
    }
    /**
     * 将pageable成拼接sql语句
     * @param page
     * @return
     * @throws Exception
     */
    public static String initNativePageSql(Class clazz,Pageable page) {
        if(page == null)
        return "";
        StringBuffer sb = new StringBuffer();
        try {
        	if(page.getSort() != null) {
                Sort sort = page.getSort();
                Iterator < Order > iterator = sort.iterator();
                sb.append(" Order By");
                while(iterator.hasNext()) {
                    Order order = iterator.next();
                    sb.append(" `" + getNativeColumn(clazz, order.getProperty())+"`");
                    sb.append(" " + order.getDirection() + ",");
                }
                sb.deleteCharAt(sb.length() - 1);
            }
		} catch (Exception e) {
		}
        if(page.getPageSize()>0)
        sb.append(" limit " + page.getOffset() + "," + page.getPageSize());
        return sb.toString();
    }
    public static String getNativeColumn(Class clazz,String fieldName) throws Exception {
    	Field field = clazz.getDeclaredField(fieldName);
    	String name = "";
		boolean fieldHasAnno = field.isAnnotationPresent(Column.class);
		if(fieldHasAnno) {
			Column column = field.getAnnotation(Column.class);
			return column.name();
		}
		fieldName = "get"+fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Method method = clazz.getDeclaredMethod(fieldName);
		Column column = method.getAnnotation(Column.class);
		return column.name();
    }
    public static Query initPageQuery(Query query, Pageable page) {
    	query.setFirstResult((int) page.getOffset());
    	query.setMaxResults(page.getPageSize());
    	return query;
    }
    public static String initSortSql(Pageable page) {
    	if(page == null)
            return "";
        StringBuffer sb = new StringBuffer();
        if(page.getSort() != null) {
            Sort sort = page.getSort();
            Iterator < Order > iterator = sort.iterator();
            sb.append(" Order By");
            while(iterator.hasNext()) {
                Order order = iterator.next();
                sb.append(" " + order.getProperty());
                sb.append(" " + order.getDirection() + ",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    /**
     * 获得总页数
     * @param pageSize 页面大小
     * @param count 总记录数
     * @return
     */
    public static Integer getTotalPage(Integer pageSize, Integer count) {
        if(pageSize==null||pageSize==0)
            return null;
        if(count==null||count==0)
            return 1;
        return ((count-1)/pageSize)+1;
    }
    /**
     * 将page对象转为pageData对象
     * @param page
     * @return
     */
    public static <T> PageData<T> convertPageData(Page<T> page){
    	PageData<T> pageData = new PageData<>();
    	pageData.setRows(page.getContent());
    	pageData.setRecords((int)page.getTotalElements());
    	pageData.setTotal(page.getTotalPages());
    	pageData.setPage(page.getNumber() + 1);
    	return pageData;
    }
    public static <T> PageData<T> convertPageData(List<T> list, Integer count, PageParam pageInfo){
    	PageData<T> pageData = new PageData<>();
    	pageData.setRows(list);
		if (pageInfo != null && count != null && (pageInfo.getPageFlag() == null || 1 != pageInfo.getPageFlag())) {
			pageData.setRecords(count);
			pageData.setPage(pageInfo.getPageNumber() + 1);
			pageData.setTotal(PageUtils.getTotalPage(pageInfo.getPageSize(), count));
		}
		return pageData;
    }

	/**
	 * 生成通用分页信息
	 * @param page
	 * @param size
	 * @param sidx
	 * @param sord
	 * @return
	 */
	public static PageParam constructPageParam(Integer page, Integer size, String sidx, String sord) {
		Sort sort = constructSort(sidx, sord);
		Integer p = 0;
		if (page != null){
			p = page - 1;
			if (p <= 0){
				p = 0;
			}
		}

		PageParam pageInfo = new PageParam(p, size, 1, sort);
		return pageInfo;
	}
    /**
     * 生成通用分页信息
     * @param page
     * @param size
     * @param pageFlag
     * @param sidx
     * @param sord
     * @return
     */
    public static PageParam constructPageParam(Integer page, Integer size, Integer pageFlag, String sidx, String sord) {
    	Sort sort = constructSort(sidx, sord);
		Integer p = 0;
		if (page != null){
			p = page - 1;
			if (p <= 0){
				p = 0;
			}
		}

		PageParam pageInfo = new PageParam(p, size, pageFlag, sort);
		return pageInfo;
    }
    /**
     * 生成通用分页信息
     * @param page
     * @param size
     * @param pageFlag
     * @param sidx
     * @param sord
     * @return
     */
    public static PageParam constructPageParam(Integer page, Integer size, Integer pageFlag, String[] sidx, String[] sord) {
    	Sort sort = constructSort(sidx, sord);
		Integer p = 0;
		if (page != null)
			p = page - 1;
		PageParam pageInfo = new PageParam(p, size, pageFlag, sort);
		return pageInfo;
    }
    public static PageParam constructPageParam(Integer page, Integer size, Sort sort) {
    	Integer p = 0;
		if (page != null)
			p = page - 1;
		PageParam pageInfo = new PageParam(p, size, sort);
		return pageInfo;
    }
    /**
     * 生成通用地排序信息
     * @param sidx
     * @param sord
     * @return
     */
    public static Sort constructSort(String sidx, String sord) {
		Sort sort = null;
		if (!StringUtils.isEmpty(sidx)) {
			String[] sidxArr = StringUtils.split(sidx, ",");
			String[] sordArr = StringUtils.split(sord, ",");
			List<Order> oList = new ArrayList<>();
			for(int i=0;i<sidxArr.length;i++) {
				String si = sidxArr[i];
				String so = sordArr[i];
				if ("desc".equalsIgnoreCase(so))
					oList.add(new Order(Direction.DESC, si));
				else
					oList.add(new Order(Direction.ASC, si));
			}
			sort = Sort.by(oList);
		}
		return sort;
	}
    /**
     * 生成通用地排序信息
     * @param sidxArr
     * @param sordArr
     * @return
     */
    public static Sort constructSort(String[] sidxArr, String[] sordArr) {
		Sort sort = null;
		if (sidxArr != null && sidxArr.length > 0) {
			List<Order> oList = new ArrayList<>();
			for(int i=0;i<sidxArr.length;i++) {
				String si = sidxArr[i];
				String so = sordArr[i];
				if ("desc".equalsIgnoreCase(so))
					oList.add(new Order(Direction.DESC, si));
				else
					oList.add(new Order(Direction.ASC, si));
			}
			sort = Sort.by(oList);
		}
		return sort;
	}


    public static PageParam constructPageParam(Integer page, Integer size) {
		return constructPageParam(page, size, null);
    }
    public enum SortPropertyEnum{
    	SIDX,SORD;
    };
    public static EnumMap<SortPropertyEnum, String> deconstructSort(Sort sort){
    	EnumMap<SortPropertyEnum, String> enumMap = new EnumMap<>(SortPropertyEnum.class);
    	if(sort != null) {
    		String sidx = "";
    		String sord = "";
    		Iterator<Order> iterator = sort.iterator();
    		while(iterator.hasNext()) {
    			Order order = iterator.next();
    			String property = order.getProperty();
    			Direction direction = order.getDirection();
    			sidx += property + ",";
    			if(direction.isAscending()) {
    				sord += "asc" + ",";
    			}else {
    				sord += "desc" + ",";
    			}
    		}
    		if(!StringUtils.isEmpty(sidx)) {
    			sidx = sidx.substring(0, sidx.length() - 1);
    			sord = sord.substring(0, sord.length() - 1);
    		}
    		enumMap.put(SortPropertyEnum.SIDX, sidx);
    		enumMap.put(SortPropertyEnum.SORD, sord);
    	}

    	return enumMap;
    }
    public static Map<String, Object> deconstructPageParam(PageParam pageParam){
    	Map<String, Object> map = new HashMap<>();
    	if(pageParam != null) {
    		map.put("page", pageParam.getPageNumber() + 1);
    		map.put("size", pageParam.getPageSize());
    		map.put("pageFlag", pageParam.getPageFlag());
    		EnumMap<SortPropertyEnum, String> enumMap = deconstructSort(pageParam.getSort());
    		map.put("sidx", enumMap.get(SortPropertyEnum.SIDX));
    		map.put("sord", enumMap.get(SortPropertyEnum.SORD));
    	}
    	return map;
    }

}
