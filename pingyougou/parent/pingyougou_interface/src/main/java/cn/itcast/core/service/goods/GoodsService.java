package cn.itcast.core.service.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.vo.GoodVo;

public interface GoodsService {
    /**
     * 保存商品
     * @param goodVo
     */
    void add(GoodVo goodVo);
    /**
     * 商品列表查询
     *
     */
    PageResult search(Integer page, Integer rows, Goods goods);
    /**
     * 回显商品
     *
     */
    GoodVo findOne(Long id);
    /**
     * 修改商品
     */
    void update(GoodVo goodVo);

    /**
     * 查询未审核列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    PageResult searchForManager(Integer page, Integer rows, Goods goods);

    /**
     * 审核商品
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 删除商品
     * @param ids
     * 修改IsDelete
     */
    void delete(Long[] ids);
}
