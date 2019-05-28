package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentQuery;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Service
public class ContentServiceImpl implements ContentService {
	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private ContentDao contentDao;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		contentDao.insertSelective(content);
		clearCacheForRedis(content.getCategoryId());
	}

	@Override
	public void edit(Content content) {
		Long newCategoryId = content.getCategoryId();
		Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
		if (!newCategoryId.equals(oldCategoryId)){
			clearCacheForRedis(newCategoryId);
			clearCacheForRedis(oldCategoryId);
		} else {
			clearCacheForRedis(newCategoryId);
		}
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				contentDao.deleteByPrimaryKey(id);
				clearCacheForRedis(contentDao.selectByPrimaryKey(id).getCategoryId());
			}
		}
	}

	/**
	 * @author 栗子
	 * @Description 首页大广告的轮播图数据
	 * @Date 15:37 2019/5/9
	 * @param categoryId
	 * @return java.util.List<cn.itcast.core.pojo.ad.Content>
	 **/
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
    	// 从缓存中获取
		List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		if (list == null){
			synchronized (this){
				// 封装查询条件
				ContentQuery query = new ContentQuery();
				// 可用的广告：status 0 不  1 可用
				query.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
				// 根据sort_order进行排序
				query.setOrderByClause("sort_order desc");
				// 查询
				list = contentDao.selectByExample(query);
				// 存到缓存中
				redisTemplate.boundHashOps("content").put(categoryId, list);
			}

		}
		return list;
    }


	//清空缓存
	private void clearCacheForRedis(Long categoryId) {
		redisTemplate.boundHashOps("content").delete(categoryId);
	}
}
