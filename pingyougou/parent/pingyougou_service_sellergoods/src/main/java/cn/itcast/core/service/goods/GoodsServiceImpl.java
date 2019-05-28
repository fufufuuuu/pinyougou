package cn.itcast.core.service.goods;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.staticPage.StaticPageService;
import cn.itcast.core.vo.GoodVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination topicPageAndSolrDestination;

    @Resource
    private Destination queueSolrDeleteDestination;

    @Transactional
    @Override
    public void add(GoodVo goodVo) {
        // 1保存商品的基本信息
        Goods goods = goodVo.getGoods();
        goods.setAuditStatus("0"); //商品的审核状态
        goodsDao.insertSelective(goods); //保存 返回自增主键的id
        // 2保存商品的描述信息
        GoodsDesc goodsDesc = goodVo.getGoodsDesc();
        goodsDescDao.insertSelective(goodsDesc);
        // 3保存商品对应的库存信息
        // 判断商品是否启用规格
        if ("1".equals(goods.getIsEnableSpec())) {
            List<Item> itemList = goodVo.getItemList(); //库存信息
            if (itemList != null && itemList.size() > 0) {
                for (Item item : itemList) {
                    // 商品标题= spu名称 + spu副标题+规格名称
                    StringBuilder title = new StringBuilder(goods.getGoodsName()
                            + " " + goods.getCaption());
                    String spec = item.getSpec(); //规格 栗子：
                    // json转对象
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entrySet = map.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        title.append(entry.getValue());
                    }
                    item.setTitle(title.toString());
                    setItemAttribute(goods, goodsDesc, item);
                }
            }

        }

    }

    /**
     * 分页查询商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        if (goods.getSellerId() != null && !goods.getSellerId().equals("")){
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId());
            Page<Goods> goodsList = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
            return new PageResult(goodsList.getTotal(),goodsList.getResult());
        }
        return null;
    }

    /**
     * 回显商品
     * @param id
     * @return
     */
    @Override
    public GoodVo findOne(Long id) {
        //商品基本信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        GoodVo goodVo = new GoodVo();
        goodVo.setGoods(goods);
        // 商品描述信息
        goodVo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(goods.getId()));
        // 库存信息
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        goodVo.setItemList(itemDao.selectByExample(query));
        return goodVo;
    }

    /**
     * 更新商品
     * @param goodVo
     */

    @Override
    public void update(GoodVo goodVo) {
        // 更新商品的基本信息
        Goods goods = goodVo.getGoods();
        // 注意：更新后的商品需要重新审核（status = 0）
        goods.setAuditStatus("0");
        goodsDao.updateByPrimaryKeySelective(goods);
        // 更新商品描述信息
        GoodsDesc goodsDesc = goodVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        // 更新库存信息 先删除再添加
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(query);
        // 商品标题=spu名称+spu副标题+规格名称
        StringBuilder title = new StringBuilder(goods.getGoodsName()+""+goods.getCaption());
        // 判断规格是否启用
        if ("1".equals(goods.getIsEnableSpec())){
            // 封装规格名称
            List<Item> itemList = goodVo.getItemList();
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map<String, String> maps = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = maps.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title.append(entry.getKey());
                }
                item.setTitle(title.toString());
                //设置库存商品公共的属性
                setItemAttribute(goods, goodsDesc, item);
                // 更新库存信息
                itemDao.insertSelective(item);
            }

        } else {
            Item item = new Item();
            item.setTitle(title.toString());
            item.setStatus("1");
            item.setIsDefault("1");
            item.setPrice(goods.getPrice());
            item.setNum(996);
            setItemAttribute(goods, goodsDesc, item);
            itemDao.insertSelective(item);
        }
    }
    /**
     * 查询未审核列表
     * @param page
     * @param rows
     * @param goods
     * @return
     * 待审核且未删除
     */
    @Override
    public PageResult searchForManager(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page, rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if (goods.getAuditStatus()!= null && !"".equals(goods.getAuditStatus())){
            // 未审核条件 status = 0
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        // 未删除
        criteria.andIsDeleteIsNull();
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }
    /**
     * 审核商品
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        Goods goods = new Goods();
        goods.setAuditStatus(status);
        if (ids != null && ids.length>0){
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)){
                    // 商品上架
//                    isShow(id);
//                    dataImportMysqlToSolr();
//                    // 生成静态页
//                    staticPageService.getHtml(id);
                    // 发送商品ID到MQ
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(String.valueOf(id));
                        }
                    });

                }
            }
        }
    }


    // 将数据库数据导入到solr索引库中
    private void dataImportMysqlToSolr() {
        ItemQuery query = new ItemQuery();
        // 上架才导入
        query.createCriteria().andStatusEqualTo("1");
        List<Item> itemList = itemDao.selectByExample(query);
        if (itemList != null && itemList.size() > 0){
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(itemList,3000);
        }
    }

    /**
     * 删除商品
     * @param ids
     * 修改IsDelete
     */
    @Override
    public void delete(Long[] ids) {
        Goods goods = new Goods();
        goods.setIsDelete("1");
        if (ids != null && ids.length > 0){
            for (final Long id : ids) {
                goods.setId(id);
                // 逻辑删除
                goodsDao.updateByPrimaryKeySelective(goods);
                // 发布商品id到MQ
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));
                    }
                });
            }
        }
    }

    //设置库存商品公共的属性
    private void setItemAttribute(Goods goods, GoodsDesc goodsDesc, Item item) {
        String images = goodsDesc.getItemImages();
        List<Map> mapList = JSON.parseArray(images, Map.class);
        if (mapList != null && mapList.size() > 0) {
            String imageUrl = mapList.get(0).get("url").toString();
            item.setImage(imageUrl);
        }
        // 商品分类的id（三级）
        Long category3Id = goods.getCategory3Id();
        String sellerId = goods.getSellerId();
        item.setCategoryid(category3Id);
        // 商品的状态
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        // 商品id
        item.setGoodsId(goods.getId());
        // 商家id
        item.setSellerId(sellerId);
        // 商品分类名称
        item.setCategory(itemCatDao.selectByPrimaryKey(category3Id).getName());
        // 商品品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(sellerId).getName());
        // 商家店铺表
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());
    }
}
