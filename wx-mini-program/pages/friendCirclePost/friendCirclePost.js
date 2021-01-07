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
    categoryId:null
  },
  chooseImage: function(e) {
    if (this.data.files.length >= 9) {
      util.showErrorToast('只能上传9张图片')
      return false;
    }

    var that = this;
    wx.chooseImage({
      count: 9,
      sizeType: ['original', 'compressed'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        that.setData({
          files: that.data.files.concat(res.tempFilePaths)
        });
        that.upload(res);
      }
    })
  },
  upload: function(res) {
    var that = this;
   
    for (let index = 0; index < res.tempFilePaths.length; index++) {
      wx.showLoading({
        title: '上传图片中...',
      })
      const element = res.tempFilePaths[index];
      const uploadTask = wx.uploadFile({
        url: api.StorageUpload,
        filePath: element,
        name: 'file',
        success: function(res) {
          wx.hideLoading({
            complete: (res) => {-1},
          })
          var _res = JSON.parse(res.data);
          if (_res.errno === 0) {
            var url = _res.data.url
            that.data.picUrls.concat(url)
            that.setData({
              hasPicture: true,
              picUrls: that.data.picUrls
            })
          }
        },
        fail: function(e) { 
           wx.hideLoading({
          complete: (res) => {-1},
        })
          wx.showModal({
            title: '错误',
            content: '上传失败',
            showCancel: false
          })
        },
      }) 
      uploadTask.onProgressUpdate((res) => {
        console.log('上传进度', res.progress)
        console.log('已经上传的数据长度', res.totalBytesSent)
        console.log('预期需要上传的数据总长度', res.totalBytesExpectedToSend)
      })
    }
   

   

  },
  previewImage: function(e) {
    wx.previewImage({
      current: e.currentTarget.id, // 当前显示图片的http链接
      urls: this.data.files // 需要预览的图片http链接列表
    })
  },
  
  onLoad: function(options) {
    var that = this;
    that.setData({
      orderId: options.orderId,
      type: options.type,
      valueId: options.valueId,
      categoryId:options.categoryId
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
      util.showErrorToast('请填写评论')
      return false;
    }

    util.request(api.FriendCicleAdd, {
      content: that.data.content,
      phone: that.data.phone,   
      icons: that.data.picUrls,
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
  submitOrder: function() {
    if (this.data.addressId <= 0) {
      util.showErrorToast('请选择收货地址');
      return false;
    }
    util.jhxLoadShow("正在下单，请稍后...");
    let nowTime = + new Date();
    if (nowTime - lastTime > 5000 || !lastTime) { //5秒内避免重复提交订单
      lastTime = nowTime;
    } else {
      return false;
    }

    util.request(api.OrderSubmit, {
      cartId: this.data.cartId,
      addressId: this.data.addressId,
      couponId: this.data.couponId,
      message: this.data.message,
      grouponRulesId: this.data.grouponRulesId,
      grouponLinkId: this.data.grouponLinkId
    }, 'POST').then(res => {
      util.jhxLoadHide();
      if (res.errno === 0) {
        
        // 下单成功，重置couponId
        try {
          wx.setStorageSync('couponId', 0);
        } catch (error) {

        }

        const orderId = res.data.orderId;
        util.request(api.OrderPrepay, {
          orderId: orderId
        }, 'POST').then(function(res) {
          if (res.errno === 0) {
            const payParam = res.data;
            console.log("支付过程开始");
            wx.requestPayment({
              'timeStamp': payParam.timeStamp,
              'nonceStr': payParam.nonceStr,
              'package': payParam.packageValue,
              'signType': payParam.signType,
              'paySign': payParam.paySign,
              'success': function(res) {
                console.log("支付过程成功");
                wx.redirectTo({
                  url: '/pages/payResult/payResult?status=1&orderId=' + orderId
                });
              },
              'fail': function(res) {
                console.log("支付过程失败");
                wx.redirectTo({
                  url: '/pages/payResult/payResult?status=0&orderId=' + orderId
                });
              },
              'complete': function(res) {
                console.log("支付过程结束")
              }
            });
          } else {
            wx.redirectTo({
              url: '/pages/payResult/payResult?status=0&orderId=' + orderId
            });
          }
        });

      } else {
        wx.redirectTo({
          url: '/pages/payResult/payResult?status=0&orderId=' + orderId
        });
      }
    });
  },
  bindInputValue(event) {

    let value = event.detail.value;

    //判断是否超过140个字符
    if (value && value.length > 1000) {
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