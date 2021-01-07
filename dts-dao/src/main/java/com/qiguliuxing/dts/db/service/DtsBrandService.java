package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsBrandMapper;
import com.qiguliuxing.dts.db.domain.DtsBrand;
import com.qiguliuxing.dts.db.domain.DtsBrandExample;
import com.qiguliuxing.dts.db.domain.DtsBrand.Column;
import com.qiguliuxing.dts.db.domain.DtsGoodsExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsBrandService {
    @Resource
    private DtsBrandMapper brandMapper;
    private Column[] columns = new Column[]{Column.id, Column.name, Column.desc, Column.picUrl, Column.floorPrice, Column.address, Column.phone, Column.longitude, Column.latitude};

    public List<DtsBrand> queryVO(int offset, int limit) {
        DtsBrandExample example = new DtsBrandExample();
        example.or().andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);
        return brandMapper.selectByExampleSelective(example, columns);
    }

    public List<DtsBrand> queryVoTop(Integer categoryId) {
        DtsBrandExample example = new DtsBrandExample();
        example.createCriteria().andDefaultCategoryIdEqualTo(categoryId).andSortOrderEqualTo((byte) 0).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        return brandMapper.selectByExampleSelective(example, columns);
    }

    public int queryTotalCount(String keyword) {
        DtsBrandExample example = new DtsBrandExample();
        example.createCriteria().andNameLike("%" + keyword + "%").andDeletedEqualTo(false);
 
        return (int) brandMapper.countByExample(example);
    }

    public int queryTotalCountWithType(String keyword, Integer categoryId, Integer type) {
        DtsBrandExample example = new DtsBrandExample();
        example.createCriteria().andNameLike("%" + keyword + "%").andDefaultCategoryIdEqualTo(categoryId).andDeletedEqualTo(false);
        if (type == 0) {
            example.setOrderByClause("sort_order desc");

        } else {
            example.setOrderByClause("add_time desc");
        }
        return (int) brandMapper.countByExample(example);
    }

    public DtsBrand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    public List<DtsBrand> querySelective(String id, String name, Integer page, Integer size, String sort, String order) {
        DtsBrandExample example = new DtsBrandExample();
        DtsBrandExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return brandMapper.selectByExample(example);
    }

    public int updateById(DtsBrand brand) {
        brand.setUpdateTime(LocalDateTime.now());
        return brandMapper.updateByPrimaryKeySelective(brand);
    }

    public void deleteById(Integer id) {
        brandMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(DtsBrand brand) {
        brand.setAddTime(LocalDateTime.now());
        brand.setUpdateTime(LocalDateTime.now());
        brandMapper.insertSelective(brand);
    }

    public List<DtsBrand> all() {
        DtsBrandExample example = new DtsBrandExample();
        example.or().andDeletedEqualTo(false);
        return brandMapper.selectByExample(example);
    }

    public List<DtsBrand> queryVoWithType(String keyword, int offset, int limit, Integer categoryId, Integer type) {
        DtsBrandExample example = new DtsBrandExample();
        example.createCriteria().andNameLike("%" + keyword + "%").andDefaultCategoryIdEqualTo(categoryId).andDeletedEqualTo(false);

        if (type == 0) {

            example.setOrderByClause("sort_order desc");

        } else {
            example.setOrderByClause("add_time desc");
        }
        PageHelper.startPage(offset, limit);
        return brandMapper.selectByExampleSelective(example, columns);
    }

    public List<DtsBrand> queryVoWithKeyWord(String keyword, Integer page, Integer size) {
        DtsBrandExample example = new DtsBrandExample();
        example.createCriteria().andNameLike("%" + keyword + "%").andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(page, size);
        return brandMapper.selectByExampleSelective(example, columns);
    }
}
