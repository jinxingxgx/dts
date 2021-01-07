var app = getApp();
var util = require('../../utils/util.js');
var api = require('../../config/api.js');

Page({
  data: {
    comments: [],
    allCommentList: [],
    picCommentList: [],
    type: 0,
    valueId: 0,
    userId:null,
    showType: 0,
    allCount: 0,
    hasPicCount: 0,
    allPage: 1,
    picPage: 1,
    size: 10,
    title:'',
    userInfo:null,
    canPush:false,
    categoryId:'',
    buttons: [{
      openType: 'getUserInfo',
      label: '发布',
      icon: '/static/images/friend.png',
    }]
  },
  getCommentCount: function () {
    let that = this;
    util.request(api.friendCircleCount, {
    
    }).then(function (res) {
      if (res.errno === 0) {
        that.setData({
          allCount: res.data.allCount,

        });
      }
    });
  },
  previewImage: function(e) {
    wx.previewImage({
      current: e.currentTarget.dataset.item, // 当前显示图片的http链接
      urls: e.currentTarget.dataset.list // 需要预览的图片http链接列表
    })
  },
  call(e){
    var phone = e.currentTarget.dataset.phone
    wx.makePhoneCall({
      phoneNumber: phone,
    })
  },

  del(e){
    var id = e.currentTarget.dataset.id
    var that=this
    util.request(api.FriendCicleDel, {
      id:id
     
    }).then(function (res) {
      if (res.errno === 0) {
        that.setData({
          allPage:1
        })
        that.getCommentList()

      }
    });
  },
  onClick(e) {
    console.log('onClick', e.detail)
    if(this.data.title=='出租车'||this.data.title=='车辆预约'){
      wx.navigateTo({
        url: '/pages/taixPost/taixPost?categoryId='+this.data.categoryId,
      })
    }else{
      wx.navigateTo({
        url: '/pages/friendCirclePost/friendCirclePost?categoryId='+this.data.categoryId,
      })
    }
   
  },
  getCommentList: function () {
    let that = this;
    util.request(api.FriendCicleList, {
      categoryId:that.data.categoryId,
      size: that.data.size,
      page: (that.data.showType == 0 ? that.data.allPage : that.data.picPage)
     
    }).then(function (res) {
      if (res.errno === 0) {
        if( that.data.allPage==1){
          that.setData({
            allCommentList: res.data.data,
            allPage: res.data.currentPage,
            comments: res.data.data,
            canPush:res.data.canPush,
            userId:res.data.userId
          });
        }else{
          that.setData({
            allCommentList: that.data.allCommentList.concat(res.data.data),
            allPage: res.data.currentPage,
            comments: that.data.allCommentList.concat(res.data.data),
            canPush:res.data.canPush,
            userId:res.data.userId
          });
        }
      

      }
    });
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    
    this.setData({
      categoryId: options.id,
      valueId: options.valueId,
      title:options.title
     
    });
   
    wx.setNavigationBarTitle({
      title: options.title,
    })
    if(options.title =='出租车'){
      this.setData({
        categoryId: 100101319,
      
      });
    }
    this.getCommentCount();
    this.getCommentList();
  },
  onReady: function () {
    // 页面渲染完成

  },
  onShow: function () {
    // 页面显示
  
  },
  onHide: function () {
    // 页面隐藏

  },
  onUnload: function () {
    // 页面关闭

  },

  onReachBottom: function () {
    console.log('onPullDownRefresh');
    if (this.data.showType == 0) {

      if (this.data.allCount / this.data.size < this.data.allPage) {
        return false;
      }

      this.setData({
        'allPage': this.data.allPage + 1
      });
    } else {
      if (this.data.hasPicCount / this.data.size < this.data.picPage) {
        return false;
      }

      this.setData({
        'picPage': this.data.picPage + 1
      });
    }

    this.getCommentList();
  }
})