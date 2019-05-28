package cn.itcast.core.task;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 定时任务
 */
@Component
public class RedisTask {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    // 将商品分类的数据写入缓存
    @Scheduled(cron = "00 36 19 * * ?")
    public void autoUpdateRedisOfItemCat(){
        // 将商品分类写入到redis中
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList != null && itemCatList.size() > 0){
            for (ItemCat itemCat : itemCatList) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }
        System.out.println("1定时器执行了");
    }
    // 将商品模版的数据写入缓存
    @Scheduled(cron = "00 36 19 * * ?")
    public void autoUpdateRedisOfTemplate(){
        List<TypeTemplate> templates = typeTemplateDao.selectByExample(null);
        if (templates != null && templates.size() > 0){
            for (TypeTemplate template : templates) {
                // 品牌结果集
                String brandIds = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
                // 规格结果集
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(), specList);
            }
        }
        System.out.println("2定时器执行了");

    }

    public List<Map> findBySpecList(Long id) {
        //通过模版id获得模版对象
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        //获取规格结果集
        String specIds = typeTemplate.getSpecIds();
        //转换成json对象
        List<Map> specList = JSON.parseArray(specIds, Map.class);
        if (specList != null && specList.size()>0){
            for (Map map : specList) {
                //获取规格id
                String specId = map.get("id").toString();
//                通过规格id获取规格选项
                SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
                optionQuery.createCriteria().andSpecIdEqualTo(Long.parseLong(specId));
                List<SpecificationOption> options = specificationOptionDao.selectByExample(optionQuery);
                //封装到map
                map.put("options",options);
            }
        }

        return specList;
    }
}
