package cn.itcast.core.service.search;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ItemDao itemDao;

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        // 高亮显示关键字
        Map<String, Object> search = searchForHighLightPage(searchMap);
        // 商品分类查询 加载分类列表
        List<String> category = searchForGroupPage(searchMap);
        if (category != null) {
            resultMap.put("categoryList", category);
            // 商品品牌列表，规格列表
            Map<String, Object> specAndBrand = selectBrandsAndSpecsByItemCatNameWith0(category.get(0));
            resultMap.putAll(specAndBrand);
        }
        resultMap.putAll(search);
        return resultMap;
    }

    /**
     * 商品上架
     * @param id
     */
    @Override
    public void saveItemToSolr(Long id) {
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(query);
        // 将数据导入索引库中
        if (items != null && items.size() > 0){
            for (Item item : items) {
                String spec = item.getSpec();
                // 手动封装SpecMap
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                item.setSpecMap(map);
            }
            // 将数据导入索引库中
            solrTemplate.saveBeans(items, 1000);
        }
    }

    /**
     * 商品下架
     * 从solr删除
     * @param id
     */
    @Override
    public void deleteItemFromSolr(Long id) {
        // 商品下架
        SimpleQuery query = new SimpleQuery("item_goodsid:" + id);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    private Map<String, Object> selectBrandsAndSpecsByItemCatNameWith0(String itemCatName) {
        // 通过分类名称获取模版id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(itemCatName);
        // 通过模版id 获取品牌结果集，规格结果集
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        Map<String, Object> map = new HashMap<>();
        map.put("brandList", brandList);
        map.put("specList", specList);
        return map;
    }

    // 加载分类列表
    private List<String> searchForGroupPage(Map<String, String> searchMap) {
        // 根据哪个字段检索
        Criteria criteria = new Criteria("item_keywords");
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 设置分组条件
        GroupOptions options = new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);
        // 分组查询
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        // 处理分组结果集 封装数据
        List<String> list = new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> entries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : entries) {
            String groupValue = groupEntry.getGroupValue();
            list.add(groupValue);
        }
        return list;
    }

    // 高亮显示关键字
    private Map<String, Object> searchForHighLightPage(Map<String, String> searchMap) {
        Criteria criteria = new Criteria("item_keywords");
        // 封装检索条件
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleHighlightQuery highLightQuery = new SimpleHighlightQuery(criteria);
        // 封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer start = (pageNo - 1) * pageSize;
        highLightQuery.setOffset(start);
        highLightQuery.setRows(pageSize);
        // 封装高亮条件
        HighlightOptions options = new HighlightOptions();
        options.addField("item_title"); // 如果该字段包含关键字 那么该关键字高亮显示
        options.setSimplePrefix("<font color='red'>"); // 开始标签 样式红色
        options.setSimplePostfix("</font>"); //结束标签
        highLightQuery.setHighlightOptions(options);
        // 添加过滤的条件：商品的分类、品牌、规格、价格
        // 商品分类过滤
        String category = searchMap.get("category");
        if (category != null && !"".equals(category)) {
            Criteria cri = new Criteria("item_category");
            cri.is(category);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            highLightQuery.addFilterQuery(filterQuery);
        }
        // 品牌过滤
        String brand = searchMap.get("brand");
        if (brand != null && !"".equals(brand)) {
            Criteria cri = new Criteria("item_brand");
            cri.is(brand);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            highLightQuery.addFilterQuery(filterQuery);
        }
        // 规格过滤
        String spec = searchMap.get("spec");
        if (spec != null && !"{}".equals(spec)) {
            Map<String, String> specList = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = specList.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria cri = new Criteria("item_spec_" + entry.getKey());
                cri.is(entry.getKey());
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
                highLightQuery.addFilterQuery(filterQuery);
            }
        }
        // 价格过滤
        String price = searchMap.get("price");
        if (price != null && !"".equals(price)) {
            String[] prices = price.split("-");
            Criteria cri = new Criteria("item_price");
            if (price.contains("*")) {
                cri.greaterThanEqual(prices[0]);
            } else {
                cri.between(prices[0], prices[1], true, true);
            }
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            highLightQuery.addFilterQuery(filterQuery);
        }
        // 添加排序条件
        String sort = searchMap.get("sort");
        if (sort != null && !"".equals(sort)) {
            String sortField = "item_" + searchMap.get("sortField");
            if ("ASC".equals(sort)) { // 升序
                Sort s = new Sort(Sort.Direction.ASC, sortField);
                highLightQuery.addSort(s);
            } else { // 降序
                Sort s = new Sort(Sort.Direction.DESC, sortField);
                highLightQuery.addSort(s);
            }
        }

        // 根据条件查询
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(highLightQuery, Item.class);
        // 处理高亮显示结果
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<Item> highlightEntry : highlighted) {
                Item item = highlightEntry.getEntity(); // 普通标题
                List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
                if (highlights != null && highlights.size() > 0) {
                    String title = highlights.get(0).getSnipplets().get(0); // 高亮标题
                    item.setTitle(title);
                }
            }
        }
        // 将结果封装到map中
        Map<String, Object> map = new HashMap<>();
        map.put("total", highlightPage.getTotalElements()); // 总条数
        map.put("totalPages", highlightPage.getTotalPages()); // 总页数
        map.put("rows", highlightPage.getContent()); // 结果集
        return map;
    }
}
