package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import cn.itcast.core.service.itemcat.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;
    /**
     * 查询未审核列表
     * @param page
     * @param rows
     * @param goods
     * @return
     * 待审核且未删除
     */
    @RequestMapping("/search.do")
    public PageResult searchForManager(Integer page, Integer rows, @RequestBody Goods goods){
        return goodsService.searchForManager(page, rows, goods);
    }
    /**
     * 审核商品
     * @param ids
     * @param status
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long [] ids, String status){
        try {
            goodsService.updateStatus(ids, status);
            return new Result(true, "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true, "操作失败");
    }
    /**
     * 删除商品
     * @param ids
     * 修改IsDelete
     */
    @RequestMapping("/delete.do")
    public Result delete(Long [] ids){
        try {
            goodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true, "删除失败");
    }
}
