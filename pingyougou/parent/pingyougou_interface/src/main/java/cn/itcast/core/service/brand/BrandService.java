package cn.itcast.core.service.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询所有品牌
     */
    List<Brand> findAll();

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult findPage(Integer pageNum,Integer pageSize);

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    PageResult search(Integer pageNum,Integer pageSize,Brand brand);

    /**
     * 添加
     * @param brand
     */
    void add(Brand brand);

    /**
     * 修改之显示数据
     * @param id
     * @return
     */
    Brand findOne(Long id);

    /**
     * 保存修改的数据
     * @param brand
     */
    void update(Brand brand);

    /**
     * 根据id删除
     * @param ids
     */
    void delete(Long[] ids);

    List<Map<String, String>> selectOptionList();
}
