package cn.itcast.core.service.StaticPage;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.staticPage.StaticPageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceIMpl implements StaticPageService, ServletContextAware {
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemDao itemDao;
    @Resource
    private ItemCatDao itemCatDao;
    // 注入servletContext
    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    // 注入configuration
    // FreeMarkerConfigurer：注入FreeMarkerConfigurer，获取Configuration对象以外，可以指定模板的位置
    private Configuration configuration;
    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.configuration = freeMarkerConfigurer.getConfiguration();
    }

    @Override
    public void getHtml(Long id) {
        // 1.创建configuration并指定模版位置，，在配置文件中指定模版路径
        // 2.获取该位置下的模版
        try {
            Template template = configuration.getTemplate("item.ftl");
            // 3.准备数据
            Map<String, Object> modelDate = getModelDate(id);
            // 4.模版 + 数据 = 带有数据的静态页
            String pathName = "/"+id+".html";
            String path = servletContext.getRealPath(pathName);
            File file = new File(path);
            template.process(modelDate, new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // 获取模版需要的数据
    private Map<String, Object> getModelDate(Long id) {
        Map<String, Object> map = new HashMap<>();
        // 获取商品数据
        Goods goods = goodsDao.selectByPrimaryKey(id);
        map.put("goods", goods);
        // 获取商品描述信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        map.put("goodsDesc", goodsDesc);
        // 获取商品分类信息
        // 三级类目
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        map.put("itemCat1",itemCat1);
        map.put("itemCat2",itemCat2);
        map.put("itemCat3",itemCat3);
        // 获取商品库存信息
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);
        map.put("itemList", itemList);
        return map;
    }
}
