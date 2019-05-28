package cn.itcast.core.vo;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;

import java.io.Serializable;
import java.util.List;
@SuppressWarnings("serial")
public class SpecificationVo implements Serializable {
    private Specification specification;
    private List<SpecificationOption> specificationOptionList;

    public SpecificationVo() {
    }

    public SpecificationVo(Specification specification) {
        this.specification = specification;
    }

    public SpecificationVo(Specification specification, List<SpecificationOption> specificationOptionList) {
        this.specification = specification;
        this.specificationOptionList = specificationOptionList;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public List<SpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<SpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
