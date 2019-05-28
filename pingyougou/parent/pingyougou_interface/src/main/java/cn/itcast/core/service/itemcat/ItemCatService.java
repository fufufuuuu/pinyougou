package cn.itcast.core.service.itemcat;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    /**
     * 商品分类查询
     * @param parentId
     * @return
     */
    List<ItemCat> findByParentId(Long parentId);
    /**
     * 添加分类
     * @param itemCat
     */
    void add(ItemCat itemCat);

    /**
     * 加载模版id
     * @param id
     * @return
     */
    ItemCat findOne(Long id);

    /**
     * 查询所有分类列表
     * @return
     */
    List<ItemCat> findAll();
}
