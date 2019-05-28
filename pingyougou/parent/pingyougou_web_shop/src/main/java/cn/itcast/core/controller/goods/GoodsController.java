package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import cn.itcast.core.vo.GoodVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;

    /**
     * 保存商品
     *
     * @param goodVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody GoodVo goodVo) {
        try {
            //设置商家的Id
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            goodVo.getGoods().setSellerId(name);
            goodsService.add(goodVo);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "添加失败");
    }

    /**
     * 查询商品了列表
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, Goods goods) {
        //设置商家id
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsService.search(page, rows, goods);
    }

    /**
     * 回显商品
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public GoodVo findOne(Long id) {
        return goodsService.findOne(id);
    }
    /**
     * 更新商品
     * @param goodVo
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody GoodVo goodVo) {
        try {
            goodsService.update(goodVo);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "修改失败");
        }
    }
}
