import dcpgs from "./DCPGS.js";
import kdv from "./kdv.js";
import kstc from "./kstc.js";
import test from "./test.js";
import topk from "./topk.js";
import bstd from "./bstd.js";
// import { Loading } from './environment/elementUI'

mapboxgl.accessToken = 'pk.eyJ1IjoieGlhb3NoaWhkIiwiYSI6ImNrNngzYnRhdzBqNm0zZnJ4eWZjdndrYzkifQ.qQjf8zANr9PsMpwq2NsRWQ';


new Vue({
    el: "#app",
    data(){
        return {
            baseUrl: "http://localhost:8080",
            mapStyle: "./js/mapstyle/style.json",
            // mapStyle: "mapbox://styles/mapbox/basic-v9",
            // mapStyle: "mapbox://styles/mapbox/dark-v9",
            // mapStyle: "mapbox://styles/mapbox/navigation-guidance-night-v2",
            // mapStyle: "mapbox://styles/mapbox/navigation-preview-night-v2",
            // mapStyle: "mapbox://styles/mapbox/traffic-night-v2",
            // mapStyle: "mapbox://styles/mapbox/navigation-night-v1",
            map: "",
            API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
            env: "local",//local(DCPGS算法读取本地文件) or prod(DCPGS算法读取本地开发环境文件) or szu_server（更换baseUrl）
            switchStatus: "SWITCH",
            currentAlgorithm: 'DCPGS',
            sideBarDisabled: false,
            mapLoading: false,
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
            sideBar: {
                switchIcon: "el-icon-arrow-right"
            },
            KSTC: {
                labelPosition:"right",
                location:"",
                clusters: [],
                clusterNums: 0,
                layerLoaded: 0,
                markers: [],
                query:{
                    "keywords": "Food,Coffee",
                    "location":{
                        "longitude":-75.16,
                        "latitude":39.95
                    },
                    "k":1,
                    "epsilon": 100.0,
                    "minPts":10,
                    "maxDist":-1
                },
                loading: false,
                timeout: false,
                running: false,
                lastKeywords:[]
            },
            spatial_skylines:{
                labelPosition:"right",
                location:"",
                layerLoaded: 0,
                markers: [],
                query:{
                    longitude: -75.16,
                    latitude: 39.95,
                    keywords: "Fast,Food,Burgers,Salad"
                },
                loading: false,
                timeout: false,
                lastKeywords:[]
            },
          // /**加**/
            topk: {
              labelPosition:"right",
              location:"",
              layerLoaded: 0,
              query:{
                longitude_topk: -3.483,
                latitude_topk: 52.983,
                keywords_topk: 'museum painting david',
                k_topk: 3
              }
            },
            kdv: {
                dataFileName: "./cases.csv",
                kdv_type: 3,
                num_threads: 1,
                x_L: 113.8482,
                x_U: 114.4473,
                y_L: 22.2025,
                y_U: 22.4655,
                row_pixels: 500,
                col_pixels: 250,
                kernel_s_type: 1,
                bandwidth_s: 1000,
                t_L: 1,
                t_U: 1,
                kernel_t_type: 1,
                bandwidth_t: 1000,
                cur_time: 1
            }
        }
    },
    methods: {
        async paramsSwitch(state){
            this.$forceUpdate();
            if(state === ''){
                this.switchStatus = this.currentAlgorithm;
            }
            else if(state === 'DCPGS_UPDATE') {
                this.DCPGS.loading = true;
                this.sideBarDisabled = true;
                await dcpgs.updateParams(this)
                    .then(()=>{
                        console.log("DCPGS params running finished")
                        this.switchStatus = "DCPGS";
                        this.DCPGS.loading = false;
                        this.sideBarDisabled = false;
                    });
            }else if(state === 'KSTC_UPDATE'){
                this.switchStatus = "KSTC"
                await kstc.loadKSTC(this);
            }
            else if (state === 'spatial_skylines_UPDATE') {
                this.currentAlgorithm = "spatial_skylines";
                this.switchStatus = "spatial_skylines";
                await bstd.LoadBSTD(this);
            }
            else if(state === 'topK') {
              this.switchStatus = 'topK'
              var lon = this.topk.query.longitude_topk;
              var la = this.topk.query.latitude_topk;
              await topk.LoadtopK(this,lon,la);
            }
            else if(state === 'topK_UPDATE') {
              this.switchStatus = 'topK'
              var lon = this.topk.query.longitude_topk;
              var la = this.topk.query.latitude_topk;
              var key = this.topk.query.keywords_topk;
              var k = this.topk.query.k_topk;
              //await topk.LoadtopK(this, lon, la);
              await topk.PostTopK(this, lon, la, key, k);
              //await topk.LoadtopK(this,lon,la);
            }
            else{
                this.switchStatus = state;
            }
        },

        paramAdjust(param, plus, maxValue, minValue, gap, fractionDigits){
            let ans = parseFloat(param);
            if(plus) {
                ans = ans + gap > maxValue ? maxValue : ans + gap;
            }else{
                ans = ans - gap < minValue ? minValue : ans - gap;
            }
            return Number(ans.toFixed(fractionDigits));
        },



        sideBarSwitch(id, switchId,inName, outName, switchInName, switchOutName){
            let sideBar = document.getElementById(id);
            if(sideBar.classList.contains(outName)){
                sideBar.classList.add(inName);
                sideBar.classList.remove(outName);
            }else if(sideBar.classList.contains(inName)){
                sideBar.classList.add(outName);
                sideBar.classList.remove(inName);
            }
            let barSwitch = document.getElementById(switchId);
            if(barSwitch.classList.contains(switchOutName)){
                barSwitch.classList.add(switchInName);
                barSwitch.classList.remove(switchOutName);
            }else if(barSwitch.classList.contains(switchInName)){
                barSwitch.classList.add(switchOutName);
                barSwitch.classList.remove(switchInName);
            }
        },

        updateClusterNums(){
            dcpgs.updateClusterNums(this);
        },

        async loadDSPGS(location, zoom){
            this.currentAlgorithm = "DCPGS";

            this.paramsSwitch('DCPGS');
            if(location === '')
                location = this.DCPGS.location;
            if(zoom === -1)
                zoom = this.map.getZoom();
            await dcpgs.loadDCPGS(this,location, zoom);
            console.log("location: ",this.DCPGS.location)
        },

        loadKDV(){
            this.currentAlgorithm = "kdv";
            this.paramsSwitch('kdv');
            kdv.loadHeatMap(this);
        },

        loadKStc(str){
            this.currentAlgorithm = "KSTC";
            this.switchStatus = "KSTC"

            kstc.loadKSTC(
                this
            )

            this.$alert(
                '1. 双击左键可以修改当前位置。              '
                +'2. 多个关键词以","分割。                 '
                +'注意：程序会尽量在可接受时间内返回已计算出的结果，后续结果转为后台计算，稍后重试可查看全部结果。',
                '提示', {
                confirmButtonText: '明白',
                callback: action => {
                }
            });
        },

        loadBSTD() {
            this.currentAlgorithm = "spatial_skylines";
            this.switchStatus = "spatial_skylines";
            bstd.LoadBSTD(this);
        },

        loadTopK(){
          this.currentAlgorithm = "topK";
          this.paramsSwitch('topK');
          this.switchStatus = "topK"
          var lon = this.topk.query.longitude_topk;
          var la = this.topk.query.latitude_topk;
          var key = this.topk.query.keywords_topk;
          var k = this.topk.query.k_topk;
         // topk.LoadtopK(this, lon, la);
          /**页面自动加载首次查询结果**/
          topk.PostTopK(this, lon, la, key, k);
        },
        loadTest(){
            test.testTree(this);
        }

    },

    //挂载
    mounted() {
        if(this.env === "szu_server"){
            this.baseUrl = "http://172.31.238.174:8080";
        }
        console.log("mounted, baseUrl: ", this.baseUrl);
        this.loadDSPGS('StockholmSweden', 13);
    },
})
