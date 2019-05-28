package cn.itcast.core.service.typeTemplate;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 模版列表查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //设置分页条件
        PageHelper.startPage(page,rows);
        TypeTemplateQuery query = new TypeTemplateQuery();
        if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName())){
            query.createCriteria().andNameLike("%"+typeTemplate.getName()+"%");
        }
        query.setOrderByClause("id desc");
        Page<TypeTemplate> list = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);
        return new PageResult(list.getTotal(),list.getResult());
    }
    /**
     * 添加模版
     * @param typeTemplate
     */
    @Override
    public void add(TypeTemplate typeTemplate) {
        int i = typeTemplateDao.insertSelective(typeTemplate);
        System.out.println(i);
    }
    /**
     * 通过模版id获取品牌以及扩展属性
     * @param id
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }
    /**
     * 根据模版id获取规格以及规格选项结果集
     * @param id
     * @return
     */
    @Override
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

    /**
     * 回显分类
     * @return
     */
    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateDao.selectByExample(null);
    }

}
