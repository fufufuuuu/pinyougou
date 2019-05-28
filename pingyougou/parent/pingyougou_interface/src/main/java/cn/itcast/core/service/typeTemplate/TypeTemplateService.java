package cn.itcast.core.service.typeTemplate;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

/**
 * 模版的接口
 */
public interface TypeTemplateService {
    /**
     * 查询模版列表
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    /**
     * 添加模版
     * @param typeTemplate
     */
    void add(TypeTemplate typeTemplate);

    /**
     * 通过模版id获取品牌以及扩展属性
     * @param id
     * @return
     */
    TypeTemplate findOne(Long id);

    /**
     * 根据模版id获取规格以及规格选项结果集
     * @param id
     * @return
     */
    List<Map> findBySpecList(Long id);

    List<TypeTemplate> findAll();
}
