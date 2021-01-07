// 上传组件 基于https://github.com/Tencent/weui-wxss/tree/master/src/example/uploader
var app = getApp();
var util = require('../../utils/util.js');
var api = require('../../config/api.js');
Page({
  data: {
    content: '',
    phone: '',
    picUrls: [],
    files: [],
    isShow:false,
    title:'',
    categoryId:null
  },
  

  
  onLoad: function(options) {
    var that = this;
    that.setData({
      orderId: options.orderId,
      type: options.type,
      valueId: options.valueId,
      categoryId:options.categoryId
    });
    util.request(api.FriendCicleDetail, {
      categoryId:that.data.categoryId,
    }, 'GET').then(function(res) {
      if (res.errno === 0) {
        that.setData({
          content: res.data.content,
          isShow: res.data.isShow,
          picUrls:  res.data.icons,
          title:res.data.title,
          phone:res.data.phone
        });
      
      }
    });
  },
  switchChange: function (e){
    this.setData({
      isShow:e.detail.value
    })
    console.log('switch1 发生 change 事件，携带值为', e.detail.value)
  },
  onClose: function() {
    wx.navigateBack();
  },
  onPost: function() {
    let that = this;

    if (!this.data.content) {
      util.showErrorToast('请填写车牌号')
      return false;
    }
   
    util.request(api.FriendCicleAdd, {
      content: that.data.content,
      phone: that.data.phone,   
      title:that.data.title,
      categoryId:that.data.categoryId,
      isShow: that.data.isShow==true?1:0
    }, 'POST').then(function(res) {
      if (res.errno === 0) {
        wx.showToast({
          title: '信息发布成功',
          complete: function() {
           wx.navigateBack({
             complete: (res) => {-1},
           })
          }
        })
      }
    });
  },
  bindInputValue(event) {

    let value = event.detail.value;

    //判断是否超过140个字符
    if (value && value.length > 140) {
      return false;
    }

    this.setData({
      content: event.detail.value,
    })
  },

  phone(event) {
    this.setData({
      phone: event.detail.value,
    })
  },
  title(event) {
    this.setData({
      title: event.detail.value,
    })
  },
  onReady: function() {

  },
  onShow: function() {
    // 页面显示

  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  }
})