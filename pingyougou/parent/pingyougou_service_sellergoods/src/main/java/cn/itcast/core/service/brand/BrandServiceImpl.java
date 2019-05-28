package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page<Brand> page = (Page<Brand>)brandDao.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */

    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        //设置分页条件
        PageHelper.startPage(pageNum,pageSize);
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (brand != null){
            if (!StringUtils.isEmpty(brand.getName())){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (!StringUtils.isEmpty(brand.getFirstChar())){
                criteria.andNameLike("%"+brand.getFirstChar()+"%");
            }
        }
        //排序
        brandQuery.setOrderByClause("id desc");
//        查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(page.getTotal(),page.getResult());
    }
    /**
     * 添加
     * @param brand
     */
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    /**
     * 修改之显示数据
     * @param id
     * @return
     */
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }
    /**
     * 保存修改的数据
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }
    /**
     * 根据id删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
//        for (Long id : ids) {
//            brandDao.deleteByPrimaryKey(id);
//        }
            brandDao.deleteByPrimaryKeys(ids);

    }

    /**
     * 初始化品牌列表
     * @return
     */

    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
