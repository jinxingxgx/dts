var util = require('../../utils/util.js');
var api = require('../../config/api.js');
//获取应用实例
const app = getApp();
Page({
  data: {
    current: '0',
    tabs: [{
        key: '0',
        title: '好店推荐',

      },
      {
        key: '1',
        title: '离我最近',

      }

    ],
    brandList: [],
    id: 0,
    banner: [],
    currentCategory: {},
    scrollLeft: 0,
    scrollTop: 0,
    scrollHeight: 0,
    page: 1,
    titile: null,
    size: 10,
    totalPages: 1,
    keyword:''
  },
  onLoad: function (options) {
    var that = this;
    // 页面初始化 options为页面跳转所带来的参数
    wx.setNavigationBarTitle({
      title: options.title
    })
    that.setData({
      title: options.title
    })
    if (options.title == '求职应聘') {
      that.setData({
        tabs: [{
            key: '0',
            title: '人才推荐',
          },
          {
            key: '1',
            title: '最新求职',
          }

        ]
      });
    } else if (options.title == '招聘专区') {
      that.setData({
        tabs: [{
            key: '0',
            title: '岗位推荐',
          },
          {
            key: '1',
            title: '最新招聘',
          }

        ]
      });
    } else if (options.title == '特色饮品' ||
      options.title == '特色小吃' ||
      options.title == '美食餐饮' ||
      options.title == '蛋糕甜点' ||
      options.title == '清真专区') {
      that.setData({
        tabs: [{
            key: '0',
            title: '好店推荐',
          },
          {
            key: '1',
            title: '离我最近',
          }

        ]
      });
    } else if (options.title == '全职招聘') {
      that.setData({
        tabs: [{
            key: '0',
            title: '岗位推荐',
          },
          {
            key: '1',
            title: '最新招聘',
          }

        ]
      });
    } else if (options.title == '兼职招聘') {
      that.setData({
        tabs: [{
            key: '0',
            title: '岗位推荐',
          },
          {
            key: '1',
            title: '最新招聘',
          }

        ]
      });
    }

    if (options.id) {
      that.setData({
        id: parseInt(options.id),

      });
    }

    wx.getSystemInfo({
      success: function (res) {
        that.setData({
          scrollHeight: res.windowHeight
        });
      }
    });
    this.getGoodsList();
  },
  call(e) {
    var phone = e.currentTarget.dataset.phone
    wx.makePhoneCall({
      phoneNumber: phone,
    })
  },
  onReady: function () {
    // 页面渲染完成
  },
  onShow: function () {
    // 页面显示
    console.log(1);
  },
  onHide: function () {
    // 页面隐藏
  },
  onChangeSearch(e) {
    console.log('onChange', e)
    this.setData({
        keyword: e.detail.value,
    })
  },
  onClear(e) {
    console.log('onClear', e)
    this.setData({
      keyword: '',
    })
},
  onConfirm(e) {

    var that = this;
    this.setData({
      page: 1,
      brandList:[]
    });
    
    this.getGoodsList();
  },
  getGoodsList: function () {
    var that = this;
   
    util.request(api.BrandListWithType, {
        categoryId: that.data.id,
        page: that.data.page,
        size: that.data.size,
        type: that.data.current,
        longitude: app.globalData.longitude,
        latitude: app.globalData.latitude,
        keyword:that.data.keyword
      })
      .then(function (res) {
        that.setData({
          brandList: that.data.brandList.concat(res.data.brandList),
          totalPages: res.data.totalPages
        });
      });

    util.request(api.IndexUrl).then(function (res) {
      if (res.errno === 0) {
        that.setData({
          banner: res.data.banner,
        });
      }
    });
  },
  onUnload: function () {
    // 页面关闭
  },
  onChange(e) {
    console.log('onChange', e)
    this.setData({
      current: e.detail.key,
    })
  },
  onTabsChange(e) {
    console.log('onTabsChange', e)
    const {
      key
    } = e.detail
    const index = this.data.tabs.map((n) => n.key).indexOf(key)
    var that = this;
    this.setData({
      page: 1,
      brandList: [],
      current: key,
      key,
      index,
    })

    this.getGoodsList();
  },
  onSwiperChange(e) {
    console.log('onSwiperChange', e)
    const {
      current: index,
      source
    } = e.detail
    const {
      key
    } = this.data.tabs[index]

    if (!!source) {
      this.setData({
        key,
        index,
      })
    }
  },
  goInfo(e) {
    var id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: '/pages/brandDetail/brandDetail?id=' + id,
    })
  },
  onReachBottom: function () {
    if (this.data.totalPages > this.data.page) {
      this.setData({
        page: this.data.page + 1
      });
    } else {
      wx.showToast({
        title: '已经到底了!',
        icon: 'none',
        duration: 2000
      });
      return false;
    }
    this.getGoodsList();
  }
})