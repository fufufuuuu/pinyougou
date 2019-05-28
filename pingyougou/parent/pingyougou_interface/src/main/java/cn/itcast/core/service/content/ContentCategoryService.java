package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentCategory;

public interface ContentCategoryService {

public List<ContentCategory> findAll();
	
	 PageResult findPage(ContentCategory contentCategory, Integer pageNum, Integer pageSize);
	
	 void add(ContentCategory contentCategory);
	
	 void edit(ContentCategory contentCategory);
	
	 ContentCategory findOne(Long id);
	
	 void delAll(Long[] ids);


}
