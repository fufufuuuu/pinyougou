package cn.itcast.core.service.specification;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.vo.SpecificationVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService{
    @Resource
    private SpecificationDao specificationDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;
    /**
     * 查询规格管理表
     * @param pageNum
     * @param pageSize
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Specification specification) {
        PageHelper.startPage(pageNum,pageSize);
//        设置查询条件
        SpecificationQuery specificationQuery = new SpecificationQuery();
        if (specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())){
            specificationQuery.createCriteria().andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        specificationQuery.setOrderByClause("id desc");
        Page<Specification> page = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        return  new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 添加规格
     * @param specificationVo
     */
    @Transactional
    @Override
    public void add(SpecificationVo specificationVo) {
        //保存规格
        Specification specification = specificationVo.getSpecification();
        specificationDao.insertSelective(specification); //返回自增主键id

        //保存规格选项
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if (specificationOptionList != null && specificationOptionList.size()>0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId()); //设置外键
            }
            specificationOptionDao.insertSelectives(specificationOptionList); //批量插入
        }

    }

    /**
     * 回显规格
     * @param id
     * @return
     */

    @Override
    public SpecificationVo findOne(Long id) {
        //查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);

        //查询规格选项
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(query);
        //封装到vo
        SpecificationVo specificationVo = new SpecificationVo();
        specificationVo.setSpecification(specification);
        specificationVo.setSpecificationOptionList(specificationOptionList);
        return specificationVo;
    }

    /**
     * 更新规格
     * @param specificationVo
     */
    @Transactional
    @Override
    public void update(SpecificationVo specificationVo) {
        //更新规格
        Specification specification = specificationVo.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);
        //更新规格选项
        //先删除规格选项
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(query);
        //再插入
        List<SpecificationOption> optionList = specificationVo.getSpecificationOptionList();
        if (optionList != null && optionList.size() > 0) {
            for (SpecificationOption option : optionList) {
                //设置外键
                option.setSpecId(specification.getId());
            }
            //批量插入
            specificationOptionDao.insertSelectives(optionList);
        }
    }

    /**
     * 删除
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length>0){
            for (Long id : ids) {
                //删除规格
                specificationDao.deleteByPrimaryKey(id);
                //删除规格选项
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(query);
            }
        }
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
