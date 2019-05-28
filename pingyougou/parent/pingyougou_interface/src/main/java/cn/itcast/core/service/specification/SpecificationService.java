package cn.itcast.core.service.specification;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    /**
     * 查询规格表
     * @param pageNum
     * @param pageSize
     * @param specification
     * @return
     */
    PageResult search(Integer pageNum, Integer pageSize, Specification specification);

    /**
     * 添加规格
     * @param specificationVo
     */
    void add(SpecificationVo specificationVo);

    /**
     * 规格回显
     * @param id
     * @return
     */
    SpecificationVo findOne(Long id);

    /**
     * 更新规格
     * @param specificationVo
     */
    void update(SpecificationVo specificationVo);

    /**
     * 删除
     * @param ids
     */
    void delete(Long[] ids);
    List<Map<String, String>> selectOptionList();
}
