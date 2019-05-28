package cn.itcast.core.controller.typeTemp;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.typeTemplate.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TypeTemplateService typeTemplateService;

    /**
     * 通过模版id获取品牌以及自定义属性
     * @param id
     * @return
     */
    @RequestMapping("findOne.do")
    public TypeTemplate findOne(Long id){
        return typeTemplateService.findOne(id);
    }
    /**
     * 根据模版id获取规格以及规格选项结果集
     * @param id
     * @return
     */
    @RequestMapping("/findBySpecList.do")
    public List<Map> findBySpecList(Long id){
        return typeTemplateService.findBySpecList(id);
    }
}
