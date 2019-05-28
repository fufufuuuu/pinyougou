package cn.itcast.core.service.search;

import java.util.Map;

/**
 * 商品检索
 */
public interface ItemSearchService {
    Map<String, Object> search(Map<String, String> searchMap);

    /**
     * 保存商品到solr
     * @param id
     */
    void saveItemToSolr(Long id);

    /**
     * 商品下架
     * 从solr中删除
     * @param id
     */
    void deleteItemFromSolr(Long id);
}
