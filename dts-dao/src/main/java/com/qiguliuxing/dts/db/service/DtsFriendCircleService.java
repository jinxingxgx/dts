package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsFeedbackMapper;
import com.qiguliuxing.dts.db.dao.DtsFriendsCircleMapper;
import com.qiguliuxing.dts.db.domain.DtsCommentExample;
import com.qiguliuxing.dts.db.domain.DtsFeedback;
import com.qiguliuxing.dts.db.domain.DtsFeedbackExample;
import com.qiguliuxing.dts.db.domain.DtsFriendsCircle;
import com.qiguliuxing.dts.db.domain.DtsFriendsCircleExample;
import com.qiguliuxing.dts.db.domain.DtsFriendsCircleWithBLOBs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author qiguliuxing
 * @date 2017/12/27
 */
@Service
public class DtsFriendCircleService {
    @Autowired
    private DtsFriendsCircleMapper dtsFriendsCircleMapper;

    public Integer add(DtsFriendsCircleWithBLOBs dtsFriendsCircle) {
        dtsFriendsCircle.setCreateTime(LocalDateTime.now());
        return dtsFriendsCircleMapper.insertSelective(dtsFriendsCircle);
    }
    public Integer update(DtsFriendsCircleWithBLOBs dtsFriendsCircle) {
        dtsFriendsCircle.setCreateTime(LocalDateTime.now());
        return dtsFriendsCircleMapper.updateByPrimaryKeySelective(dtsFriendsCircle);
    }
    public int count() {
        DtsFriendsCircleExample example = new DtsFriendsCircleExample();
        return (int) dtsFriendsCircleMapper.countByExample(example);
    }

    public List<DtsFriendsCircleWithBLOBs> querySelective(Integer categoryId, Integer userId, String content, Integer page, Integer limit, String sort, String order,Byte isShow) {
        DtsFriendsCircleExample example = new DtsFriendsCircleExample();
        DtsFriendsCircleExample.Criteria criteria = example.createCriteria();

        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (categoryId != null) {
            criteria.andCategoryIdEqualTo(categoryId);
        }
        if (!StringUtils.isEmpty(content)) {
            criteria.andContentLike("%" + content + "%");
        }
        if (isShow!=null) {
            criteria.andIsShowEqualTo(isShow);
        }
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }



        PageHelper.startPage(page, limit);
        return dtsFriendsCircleMapper.selectByExampleWithBLOBs(example);
    }

    public void delete(Integer id) {
        dtsFriendsCircleMapper.deleteByPrimaryKey(id);
    }
}
