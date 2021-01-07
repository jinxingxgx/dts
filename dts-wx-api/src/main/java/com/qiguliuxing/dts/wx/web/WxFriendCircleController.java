package com.qiguliuxing.dts.wx.web;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.core.util.RegexUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsAddress;
import com.qiguliuxing.dts.db.domain.DtsComment;
import com.qiguliuxing.dts.db.domain.DtsFriendsCircle;
import com.qiguliuxing.dts.db.domain.DtsFriendsCircleWithBLOBs;
import com.qiguliuxing.dts.db.domain.DtsRegion;
import com.qiguliuxing.dts.db.service.DtsAddressService;
import com.qiguliuxing.dts.db.service.DtsFriendCircleService;
import com.qiguliuxing.dts.db.service.DtsRegionService;
import com.qiguliuxing.dts.db.service.DtsUserService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.dao.UserInfo;
import com.qiguliuxing.dts.wx.service.GetRegionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

/**
 * 用户朋友圈
 */
@RestController
@RequestMapping("/wx/friendCircle")
@Validated
public class WxFriendCircleController extends GetRegionService {
    private static final Logger logger = LoggerFactory.getLogger(WxFriendCircleController.class);

    @Autowired
    private DtsFriendCircleService friendsCircleService;
    @Autowired
    private DtsUserService dtsUserService;

    private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(6);

    private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(3, 6, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);

    /**
     * 评论数量
     *
     * @return 评论数量
     */
    @GetMapping("count")
    public Object count() {
        logger.info("【请求开始】获取评论数量,请求参数,type:{},valueId:{}");

        int allCount = friendsCircleService.count();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("allCount", allCount);

        logger.info("【请求结束】获取评论数量,响应内容:{}", JSONObject.toJSONString(data));
        return ResponseUtil.ok(data);
    }

    /**
     * 评论列表
     *
     * @param page 分页页数
     * @param size 分页大小
     * @return 评论列表
     */
    @GetMapping("list")
    public Object list(@LoginUser Integer userId, Integer categoryId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        logger.info("【请求开始】获取评论列表,请求参数,type:{},showType:{}");
        Byte isShow = null;
        if (categoryId.equals(100101319)) {
            isShow = 1;
        }
        List<DtsFriendsCircleWithBLOBs> list = friendsCircleService.querySelective(categoryId, null, "", page, size, "create_time", "desc", isShow);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setUserInfo(dtsUserService.findUserVoById(list.get(i).getUserId()));
            if (categoryId.equals(100101319) && list.get(i).getIsShow() == 0) {
                list.remove(i);
            }

        }
        long count = PageInfo.of(list).getTotal();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("data", list);
        data.put("count", count);
        data.put("userId", userId);
        data.put("currentPage", page);
        if (categoryId.equals(100101319)) {
            if (dtsUserService.findById(userId).getUserLevel() == 2) {
                data.put("canPush", true);
            } else {
                data.put("canPush", false);
            }
        } else {
            data.put("canPush", true);

        }
        logger.info("【请求结束】获取评论列表,响应内容:{}", JSONObject.toJSONString(data));
        return ResponseUtil.ok(data);
    }

    /**
     * 获取详情
     */
    @GetMapping("detail")
    public Object detail(Integer categoryId, @LoginUser Integer userId) {
        logger.info("【请求开始】获取评论列表,请求参数,type:{},showType:{}");
        if (userId == null) {
            logger.error("用户收藏列表查询失败:用户未登录！！！");
            return ResponseUtil.unlogin();
        }
        if (dtsUserService.findById(userId).getUserLevel() == 2) {
            List<DtsFriendsCircleWithBLOBs> list = friendsCircleService.querySelective(categoryId, userId, "", 1, 1, "create_time", "desc", null);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setUserInfo(dtsUserService.findUserVoById(list.get(i).getUserId()));
            }
            if (list.size() > 0) {
                return ResponseUtil.ok(list.get(0));
            }
            return ResponseUtil.ok();

        } else {
            return ResponseUtil.fail(400, "未注册成为司机，没有该权限！");

        }

    }

    /**
     * 获取详情
     */
    @GetMapping("delete")
    public Object delete(Integer id, @LoginUser Integer userId) {
        logger.info("【请求开始】获取评论列表,请求参数,type:{},showType:{}");
        if (userId == null) {
            logger.error("用户收藏列表查询失败:用户未登录！！！");
            return ResponseUtil.unlogin();
        }
        friendsCircleService.delete(id);
        return ResponseUtil.ok();
    }

    /**
     * 添加朋友圈
     * <p>
     * 用户ID
     *
     * @return 收货地址列表
     */
    @PostMapping("add")
    public Object add(@LoginUser Integer userId, @RequestBody DtsFriendsCircleWithBLOBs circleWithBLOBs) {
        logger.info("【请求开始】用户收藏列表查询,请求参数,userId:{},comment:{}", userId, JSONObject.toJSONString(circleWithBLOBs));

        if (userId == null) {
            logger.error("用户收藏列表查询失败:用户未登录！！！");
            return ResponseUtil.unlogin();
        }

        if (dtsUserService.findById(userId).getUserLevel() == 2 && circleWithBLOBs.getCategoryId().equals(100101319)) {//表示出租车
            //先查询是否有该出租车，以及是否是出租车司机，如果是出租车司机则显示
            List<DtsFriendsCircleWithBLOBs> list = friendsCircleService.querySelective(100101319, userId, "", 1, 1, "create_time", "desc", null);
            if (list.size() > 0) {
                DtsFriendsCircleWithBLOBs circle = list.get(0);
                circle.setIsShow(circleWithBLOBs.getIsShow());
                friendsCircleService.update(circle);
            } else {
                circleWithBLOBs.setUserId(userId);
                try {
                    friendsCircleService.add(circleWithBLOBs);
                } catch (Exception e) {
                    logger.error("用户收藏列表查询失败:存储评论数据到库出错！");
                    e.printStackTrace();
                }
            }
        } else {
            circleWithBLOBs.setUserId(userId);
            try {
                friendsCircleService.add(circleWithBLOBs);
            } catch (Exception e) {
                logger.error("用户收藏列表查询失败:存储评论数据到库出错！");
                e.printStackTrace();
            }

            logger.info("【请求结束】用户收藏列表查询,响应内容:{}", JSONObject.toJSONString(circleWithBLOBs));
        }
        return ResponseUtil.ok(circleWithBLOBs);
    }

}