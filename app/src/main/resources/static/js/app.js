import dcpgs from "./DCPGS.js";
import kdv from "./kdv.js";
import kstc from "./kstc.js";
import test from "./test.js";
import topk from "./topk.js";
import topk_yago from "./topk_yago.js";
import std from "./std.js";
import pa from "./PA.js";
// import { Loading } from './environment/elementUI'

mapboxgl.accessToken = 'pk.eyJ1IjoieGlhb3NoaWhkIiwiYSI6ImNrNngzYnRhdzBqNm0zZnJ4eWZjdndrYzkifQ.qQjf8zANr9PsMpwq2NsRWQ';


new Vue({
  el: "#app",
  data() {
    return {
      baseUrl: "http://localhost: ",
      // mapStyle: "./js/mapstyle/style.json",
      mapStyle: "./js/mapstyle/dark-purple.json",
      // mapStyle: "mapbox://styles/mapbox/basic-v9",
      // mapStyle: "mapbox://styles/mapbox/dark-v11",
      // mapStyle: "mapbox://styles/mapbox/navigation-guidance-night-v2",
      // mapStyle: "mapbox://styles/mapbox/navigation-preview-night-v2",
      // mapStyle: "mapbox://styles/mapbox/traffic-night-v2",
      // mapStyle: "mapbox://styles/mapbox/navigation-night-v1",
      map: "",
      API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
      env: "szu_server",//local(DCPGS算法读取本地文件) or prod(DCPGS算法读取本地开发环境文件) or szu_server（更换baseUrl）
      switchStatus: "SWITCH",
      currentAlgorithm: 'DCPGS',
      sideBarDisabled: false,
      mapLoading: false,
      expandIcon: './img/btn-open.png', // sidebar展开时的图标路径
      collapseIcon: './img/sidebar/btn-fold.png',// sidebar收起时的图标路径
      paramExpandIcon: './img/params/btn-popupOpen.png',
      paramCollapseIcon: './img/params/btn-popupFold.png',
      isLoading: false, // 控制加载动画显示的状态
      DCPGS: {
        loading: false,
        dataset: "gowalla",//gowalla or brightkite
        labelPosition: "right",
        location: "",
        clusters: "",
        clusterNums: 0,
        layerLoaded: 0,
        markers: [],
        maxClusterNums: 150,
        params: {
          epsilon: 0.5,
          maxD: 120,
          omega: 0.5,
          tau: 0.7
        }
      },
      pa: {
        loading: false,
        dataset: "Brightkite_Euro_sorted",
        labelPosition: "right",
        location: "",
        clusters: "",
        clusterNums: 0,
        layerLoaded: 0,
        markers: [],
        maxClusterNums: 10
      },
      sideBar: {
        switchIcon: "el-icon-arrow-right"
      },
      KSTC: {
        labelPosition: "right",
        location: "",
        clusters: [],
        clusterNums: 0,
        layerLoaded: 0,
        markers: [],
        query: {
          "tmpKeywords": "restaurants",
          "keywords": "restaurants",
          "location": {
            "longitude": -75.18782594247841,
            "latitude": 39.939905740202335
          },
          "k": 5,
          "epsilon": 50.0,
          "minPts": 10,
          "maxDist": -1,
          "command": "SIMPLE_DBSCAN_BASED_APPROACH"
        },
        loading: false,
        timeout: false,
        running: false,
        lastKeywords: []
      },
      spatial_skylines: {
        labelPosition: "right",
        location: "",
        layerLoaded: 0,
        markers: [],
        query: {
          longitude: -75.157,
          latitude: 39.95,
          keywords: "Fast Burgers Salad",
          command: "BSTD"
        },
        loading: false,
        timeout: false,
        lastKeywords: []
      },
      // /**加**/
      topk: {
        labelPosition: "right",
        location: "",
        layerLoaded: 0,
        query: {
          longitude_topk: -3.483,
          latitude_topk: 52.983,
          keywords_topk: 'museum painting david',
          k_topk: 3
        }
      },
      topk_yago: {
        labelPosition: "right",
        location: "",
        layerLoaded: 0,
        query: {
          longitude_yago_topk: 20.05,
          latitude_yago_topk: 52.35,
          keywords_yago_topk: 'museum painting david',
          k_yago_topk: 3
        }
      },
      kdv: {
        labelPosition: "right",
        opacity: 0.6,
        dataFileName: "./cases.csv",
        interval:null,
        temporal: 0,
        kdv_type: 1,
        num_threads: 1,
        x_L: 113.8482,
        x_U: 114.4473,
        y_L: 22.2025,
        y_U: 22.4655,
        resolution: 2,
        resolution_levels: [150, 250, 500, 750, 1000],
        row_pixels: 250,
        col_pixels: 250,
        kernel_s_type: 1,//用不上
        kernel_t_type: 1,//用不上
        bandwidth_origin: 5,
        bandwidth_s: 0.0025,//参数和HK COVID-19页面相同
        bandwidth_t: 3,
        t_L: 7,
        t_U: 14,
        t_pixels: 28,//时间维度上的栅格数
        cur_t: 7,
      }
    }
  },

  methods: {
    showLoader() {
      console.log("showLoader");
      this.isLoading = true; // 设置 isLoading 为 true
    },
    hideLoader() {
      console.log("hideLoader");
      this.isLoading = false; // 设置 isLoading 为 false
    },
    async paramsSwitch(state) {
      this.$forceUpdate();
      if (state === '') {
        this.switchStatus = this.currentAlgorithm;
      }
      else if (state === 'DCPGS_UPDATE') {
        this.DCPGS.loading = true;
        this.sideBarDisabled = true;
        await dcpgs.updateParams(this)
          .then(() => {
            console.log("DCPGS params running finished")
            this.switchStatus = "DCPGS";
            this.DCPGS.loading = false;
            this.sideBarDisabled = false;
          });
      } else if (state === 'KSTC_UPDATE') {
        this.switchStatus = "KSTC"
        await kstc.loadKSTC(this);
      }
      else if (state === 'spatial_skylines_UPDATE') {
        this.currentAlgorithm = "spatial_skylines";
        this.switchStatus = "spatial_skylines";
        await std.LoadSTD(this);
      }
      else if (state === 'topK') {
        this.switchStatus = 'topK'
        var lon = this.topk.query.longitude_topk;
        var la = this.topk.query.latitude_topk;
        await topk.LoadtopK(this, lon, la);
      }
      else if (state === 'topK_UPDATE') {
        this.switchStatus = 'topK'
        var lon = this.topk.query.longitude_topk;
        var la = this.topk.query.latitude_topk;
        var key = this.topk.query.keywords_topk;
        var k = this.topk.query.k_topk;
        //await topk.LoadtopK(this, lon, la);
        await topk.PostTopK(this, lon, la, key, k);
        //await topk.LoadtopK(this,lon,la);
      }
      else if (state === 'topK_yago') {
        this.switchStatus = 'topK_yago'
        var lon = this.topk_yago.query.longitude_yago_topk;
        var la = this.topk_yago.query.latitude_yago_topk;
        await topk_yago.LoadtopK_yago(this, lon, la);
      }
      else if (state === 'topK_yago_UPDATE') {
        this.switchStatus = 'topK_yago'
        var lon = this.topk_yago.query.longitude_yago_topk;
        var la = this.topk_yago.query.latitude_yago_topk;
        var key = this.topk_yago.query.keywords_yago_topk;
        var k = this.topk_yago.query.k_yago_topk;
        await topk_yago.PostTopK_yago(this, lon, la, key, k);
      }
      else if (state === 'PA_UPDATE') {
        this.pa.loading = true;
        this.sideBarDisabled = true;
      } else if (state === 'kdv_UPDATE') {
        this.switchStatus = 'kdv'
        await kdv.loadHeatMap(this);
      }
      else {
        this.switchStatus = state;
      }
    },

    paramAdjust(param, plus, maxValue, minValue, gap, fractionDigits) {
      let ans = parseFloat(param);
      if (plus) {
        ans = ans + gap > maxValue ? maxValue : ans + gap;
      } else {
        ans = ans - gap < minValue ? minValue : ans - gap;
      }
      return Number(ans.toFixed(fractionDigits));
    },

    sideBarSwitch(id, switchId, inName, outName, switchInName, switchOutName) {
      let sideBar = document.getElementById(id);
      if (sideBar.classList.contains(outName)) {//展开
        sideBar.classList.add(inName);
        sideBar.classList.remove(outName);
        if (id == 'sideBar') {
          this.$refs.sideBarIcon.src = this.collapseIcon;
        } else if (id == 'params') {
          this.$refs.paramIcon.src = this.paramExpandIcon;
        }
      } else if (sideBar.classList.contains(inName)) {//收起
        sideBar.classList.add(outName);
        sideBar.classList.remove(inName);
        if (id == 'sideBar') {
          this.$refs.sideBarIcon.src = this.expandIcon;
        } else if (id == 'params') {
          this.$refs.paramIcon.src = this.paramCollapseIcon;
        }
      }
      let barSwitch = document.getElementById(switchId);
      if (barSwitch.classList.contains(switchOutName)) {
        barSwitch.classList.add(switchInName);
        barSwitch.classList.remove(switchOutName);
      } else if (barSwitch.classList.contains(switchInName)) {
        barSwitch.classList.add(switchOutName);
        barSwitch.classList.remove(switchInName);
      }
    },

    updateClusterNums() {
      dcpgs.updateClusterNums(this);
    },
    updatePAClusterNums() {
      pa.updateClusterNums(this);
    },

    updateKDVOpacity() {
      this.map.setPaintProperty('matrix-heat', 'fill-opacity', this.kdv.opacity);
    },
    updateKDVResolution() {
      this.kdv.row_pixels = this.kdv.resolution_levels[this.kdv.resolution - 1];
      kdv.updateAtriibution(this);
    },
    updateKDVBandwidth() {
      this.kdv.bandwidth_s = 0.0005 * this.kdv.bandwidth_origin
      kdv.updateAtriibution(this);
    },
    updateKDVBandwidth_t() {
      kdv.updateAtriibution(this);
    },
    updateTemporalSwitch() {
      if (this.kdv.temporal == true) {
        this.kdv.kdv_type = 3;
      } else {
        this.kdv.kdv_type = 1;
      }
      kdv.updateCurrentTime(this);//移除或添加过滤器
      kdv.updateAtriibution(this);
    },
    updateKDVCur_t() {
      kdv.updateCurrentTime(this);
    },
    switchPlay(){
      console.log("123")
      if(this.kdv.interval==null){
        this.kdv.interval = setInterval(()=>{
            this.kdv.cur_t +=(this.kdv.t_U-this.kdv.t_L)/this.kdv.t_pixels;
            if(this.kdv.cur_t>=this.kdv.t_U) this.kdv.cur_t = this.kdv.t_L;
            kdv.updateCurrentTime(this)
        },150);
      }else{
        clearInterval(this.kdv.interval)
        this.kdv.interval = null;
      }
    },

    async loadDSPGS(location, zoom) {
      this.showLoader(); // 显示加载动画
      this.currentAlgorithm = "DCPGS";
      if (location !== '') {
        this.DCPGS.location = location;
      }

      this.paramsSwitch('DCPGS');
      // 获取 paramSwitch 元素
      document.getElementById('paramSwitch').style.bottom = '11%';
      if (location === '')
        location = this.DCPGS.location;
      if (zoom === -1)
        zoom = this.map.getZoom();
      await dcpgs.loadDCPGS(this, location, zoom);
      console.log("location: ", this.DCPGS.location)
      this.hideLoader();
    },

    async loadKDV() {
      this.showLoader(); // 显示加载动画
      document.getElementById('paramSwitch').style.bottom = '16%';
      try {
        this.currentAlgorithm = "kdv";
        this.paramsSwitch('kdv');
        await kdv.loadHeatMap(this);
        console.log("KDV loaded");

        // await new Promise(resolve => setTimeout(resolve, 7500));
      } finally {
        this.hideLoader(); // 隐藏加载动画
      }
    },

    async loadKStc(str) {
      this.showLoader(); // 显示加载动画
      this.currentAlgorithm = "KSTC";
      this.switchStatus = "KSTC"
      this.map = new mapboxgl.Map({
        container: 'map', // container id
        style: this.mapStyle,
        center: [this.KSTC.query.location.longitude, this.KSTC.query.location.latitude],
        zoom: 5
      });
      // 获取 paramSwitch 元素
      document.getElementById('paramSwitch').style.bottom = '16%';
      await kstc.loadKSTC(this);
    },

    KstcChangeToOptics() {
      this.KSTC.query.minPts = 10;
      this.KSTC.query.epsilon = 100;

    },
    handleSelect(item) {
      let arr = this.KSTC.query.keywords.split(" ")
      if (arr.length > 0) {
        arr[arr.length - 1] = item.value;
      }
      this.KSTC.query.keywords = arr.join(" ")
      this.KSTC.query.tmpKeywords = this.KSTC.query.keywords
    },

    async searchKeywords(queryStr, cb) {
      this.showLoader();
      this.KSTC.query.keywords = this.KSTC.query.tmpKeywords;
      kstc.searchKeywords(this).then(res => {

        let values = res.data.map(item => {
          return {
            value: item
          }
        });
        console.log(values);
        cb(values)
      })
      this.hideLoader();
    },

    async loadSTD() {
      this.showLoader();
      this.currentAlgorithm = "spatial_skylines";
      this.switchStatus = "spatial_skylines";
      // 获取 paramSwitch 元素
      document.getElementById('paramSwitch').style.bottom = '15%';
      try {
        await std.LoadSTD(this);
        await new Promise(resolve => setTimeout(resolve, 5000));
      } finally {
        this.hideLoader();
      }
    },

    async loadTopK() {
      try {
        this.showLoader();
        this.currentAlgorithm = "topK";
        this.paramsSwitch('topK');
        this.switchStatus = "topK"
        var lon = this.topk.query.longitude_topk;
        var la = this.topk.query.latitude_topk;
        var key = this.topk.query.keywords_topk;
        var k = this.topk.query.k_topk;
        // topk.LoadtopK(this, lon, la);
        /**页面自动加载首次查询结果**/
        // 获取 paramSwitch 元素
        document.getElementById('paramSwitch').style.bottom = '8%';
        await topk.StarLoadtopK(this, lon, la, key, k);
        await new Promise(resolve => setTimeout(resolve, 4000));
      } finally {
        this.hideLoader();
      }
    },

    loadTopK_yago() {
      this.currentAlgorithm = "topK_yago";
      this.paramsSwitch('topK_yago');
      this.switchStatus = "topK_yago"
      var lon = this.topk.query.longitude_topk;
      var la = this.topk.query.latitude_topk;
      var key = this.topk.query.keywords_topk;
      var k = this.topk.query.k_topk;
      topk_yago.StarLoadtopK_yago(this, lon, la, key, k);
    },

    loadTest() {
      test.testTree(this);
    },

    async loadPA(dataset, zoom) {
      this.showLoader();
      this.currentAlgorithm = "PA";
      this.switchStatus = "PA";
      this.paramsSwitch('PA_UPDATE');
      if (dataset == "") {
        dataset = this.pa.dataset;
      }
      // 获取 paramSwitch 元素
      document.getElementById('paramSwitch').style.bottom = '7%';
      await pa.loadPA(dataset, this, zoom);
      pa.updateClusterNums(this);
    }

  },

  //挂载
  mounted() {
    if (this.env === "szu_server") {
      this.baseUrl = "http://172.31.238.174:8080";
    }
    console.log("mounted, baseUrl: ", this.baseUrl);
    this.loadDSPGS('StockholmSweden', 13);
  },
})
