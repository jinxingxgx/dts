package com.qiguliuxing.dts.wx.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.StringUtils;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsAd;
import com.qiguliuxing.dts.db.domain.DtsBrand;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.domain.DtsSearchHistory;
import com.qiguliuxing.dts.db.service.DtsAdService;
import com.qiguliuxing.dts.db.service.DtsBrandService;
import com.qiguliuxing.dts.db.service.DtsCategoryService;
import com.qiguliuxing.dts.db.service.DtsSearchHistoryService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.util.IpUtil;

/**
 * 品牌供应商
 */
@RestController
@RequestMapping("/wx/brand")
@Validated
public class WxBrandController {
    private static final Logger logger = LoggerFactory.getLogger(WxBrandController.class);

    @Autowired
    private DtsBrandService brandService;
    @Autowired
    private DtsSearchHistoryService searchHistoryService;
    @Autowired
    private DtsAdService adService;

    @Autowired
    private DtsCategoryService categoryService;

    /**
     * 品牌列表
     *
     * @param page 分页页数
     * @param size 分页大小
     * @return 品牌列表
     */
    @GetMapping("list")
    public Object list(String keyword, @LoginUser Integer userId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,@RequestParam Double longitude, @RequestParam Double latitude) {

        // 添加到搜索历史
        if (userId != null && !StringUtils.isNullOrEmpty(keyword)) {
            DtsSearchHistory searchHistoryVo = new DtsSearchHistory();
            searchHistoryVo.setKeyword(keyword);
            searchHistoryVo.setUserId(userId);
            searchHistoryVo.setFrom("wx");
            searchHistoryService.save(searchHistoryVo);
        }

        logger.info("【请求开始】品牌列表,请求参数,page:{},size:{}", page, size);
        List<DtsBrand> brandList = brandService.queryVoWithKeyWord(keyword, page, size);
        int total = brandService.queryTotalCount(keyword);
        int totalPages = (int) Math.ceil((double) total / size);
        for (int i = 0; i < brandList.size(); i++) {
            try {
                double distance = IpUtil.getDistance(brandList.get(i).getLatitude().doubleValue(), brandList.get(i).getLongitude().doubleValue(), longitude, latitude);
                brandList.get(i).setDistance(distance + "米");
            } catch (Exception e) {
                brandList.get(i).setDistance("暂时无法找到商家地址");
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("brandList", brandList);
        data.put("totalPages", totalPages);

        logger.info("【请求结束】品牌列表,响应结果：{}", JSONObject.toJSONString(data));
        return ResponseUtil.ok(data);
    }

    @GetMapping("listWithType")
    public Object listWithType(String keyword ,@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, @RequestParam Integer categoryId, @RequestParam Integer type, @RequestParam Double longitude, @RequestParam Double latitude) {
        logger.info("【请求开始】品牌列表,请求参数,page:{},size:{}");
        //查询当前目录是不是最下层
        List<DtsCategory> currentSubCategory = categoryService.queryByPid(categoryId);
        if (currentSubCategory.size() == 0) {
            List<DtsBrand> brandList = brandService.queryVoWithType(keyword,page, size, categoryId, type);
            for (int i = 0; i < brandList.size(); i++) {
                try {
                    double distance = IpUtil.getDistance(brandList.get(i).getLatitude().doubleValue(), brandList.get(i).getLongitude().doubleValue(), longitude, latitude);
                    brandList.get(i).setDistance(distance + "米");
                } catch (Exception e) {
                    brandList.get(i).setDistance("暂时无法找到商家地址");
                }
            }
            int total = brandService.queryTotalCountWithType(keyword,categoryId, type);
            int totalPages = (int) Math.ceil((double) total / size);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("brandList", brandList);
            data.put("totalPages", totalPages);
            data.put("banner", adService.queryWithPostion(categoryId));
            logger.info("【请求结束】品牌列表,响应结果：{}", JSONObject.toJSONString(data));
            return ResponseUtil.ok(data);
        } else {
            //有子目录 则查询子目录的置顶
            List<DtsBrand> brandList=new ArrayList<>();
            for (int i = 0; i <currentSubCategory.size() ; i++) {

                brandList.addAll(brandService.queryVoTop(currentSubCategory.get(i).getId()));
            }
            for (int i = 0; i < brandList.size(); i++) {
                try {
                    double distance = IpUtil.getDistance(brandList.get(i).getLatitude().doubleValue(), brandList.get(i).getLongitude().doubleValue(), longitude, latitude);
                    brandList.get(i).setDistance(distance + "米");
                } catch (Exception e) {
                    brandList.get(i).setDistance("暂时无法找到商家地址");
                }
            }
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("brandList", brandList);
            data.put("banner", adService.queryWithPostion(categoryId));
            return ResponseUtil.ok(data);

        }

    }

    /**
     * 品牌详情
     *
     * @param id 品牌ID
     * @return 品牌详情
     */
    @GetMapping("detail")
    public Object detail(@NotNull Integer id) {
        logger.info("【请求开始】品牌详情,请求参数,id:{}", id);
        DtsBrand entity = brandService.findById(id);
        if (entity == null) {
            logger.error("品牌商获取失败,id:{}", id);
            return ResponseUtil.badArgumentValue();
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("brand", entity);

        logger.info("【请求结束】品牌详情,响应结果：{}", JSONObject.toJSONString(data));
        return ResponseUtil.ok(data);
    }

    /**
     * 品牌详情
     *
     * @param id 品牌ID
     * @return 品牌详情
     */
    @GetMapping("adDetail")
    public Object adDetail(@NotNull Integer id) {
        logger.info("【请求开始】广告详情,请求参数,id:{}", id);
        DtsAd entity = adService.findById(id);
        if (entity == null) {
            logger.error("广告获取失败,id:{}", id);
            return ResponseUtil.badArgumentValue();
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ad", entity);

        logger.info("【请求结束】广告详情,响应结果：{}", JSONObject.toJSONString(data));
        return ResponseUtil.ok(data);
    }
}