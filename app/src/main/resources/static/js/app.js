import dcpgs from "./DCPGS.js";
import kdv from "./kdv.js";
import kstc from "./kstc.js"
import test from "./test.js"
// import { Loading } from './environment/elementUI'

mapboxgl.accessToken = 'pk.eyJ1IjoiY29uZ3dhbmciLCJhIjoiY2tjZWwxNW5uMDdoMjJ3cDZnaGF2bmJlYiJ9.NOKscgbt1C-DCo38sxtUFw';


new Vue({
    el: "#app",
    data(){
        return {
            baseUrl: "http://localhost:8080",
            map: "",
            API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
            env: "local",//local or prod
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
                    "keywords": "Food",
                    "location":{
                        "longitude":-75.16,
                        "latitude":39.95
                    },
                    "k":1,
                    "epsilon": 1000,
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
                query:{
                    params_1:0.5,
                    params_2:0.5,
                    params_3:0.5,
                    params_4:0.5
                }
            },
            kdv: {
                dataFileName: "./cases.csv",
                kdv_type: 1,
                num_threads: 1,
                x_L: 113.5,
                x_U: 114.5,
                y_L: 22,
                y_U: 22.6,
                row_pixels: 10,
                col_pixels: 10,
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
                        this.switchStatus = "SWITCH";
                        this.DCPGS.loading = false;
                        this.sideBarDisabled = false;
                    });
            }else if(state === 'KSTC_UPDATE'){
                this.switchStatus = "KSTC"
                await kstc.loadKSTC(this);
            }
            else{
                this.switchStatus = state;
            }
        },

        sideBarSwitch(){
            let sideBar = document.getElementById("sideBar");
            if(sideBar.classList.contains("sideOut")){
                sideBar.classList.add("sideIn");
                sideBar.classList.remove("sideOut");
            }else if(sideBar.classList.contains("sideIn")){
                sideBar.classList.add("sideOut");
                sideBar.classList.remove("sideIn");
            }
            let barSwitch = document.getElementById("sideBarSwitch");
            if(barSwitch.classList.contains("switchOut")){
                barSwitch.classList.add("switchIn");
                barSwitch.classList.remove("switchOut");
                this.sideBar.switchIcon = "el-icon-arrow-left";
            }else if(barSwitch.classList.contains("switchIn")){
                barSwitch.classList.add("switchOut");
                barSwitch.classList.remove("switchIn");
                this.sideBar.switchIcon = "el-icon-arrow-right";
            }
        },

        updateClusterNums(){
            dcpgs.updateClusterNums(this);
        },

        async loadDSPGS(location, zoom){
            this.currentAlgorithm = "DCPGS";

            this.paramsSwitch('SWITCH');
            if(location === '')
                location = this.DCPGS.location;
            if(zoom === -1)
                zoom = this.map.getZoom();
            await dcpgs.loadDCPGS(this,location, zoom);
            console.log("location: ",this.DCPGS.location)
        },

        loadKDV(){
            this.currentAlgorithm = "kdv";
            this.paramsSwitch('SWITCH');
            let kdvDataPath = ""
            if(this.env === "local"){
                kdvDataPath = "data/kdv/kdv2.geojson"
            }else if(this.env === "prod") {
                kdvDataPath = "http://localhost:8080/kdv/geojson"
            }
            kdv.loadHeatMap(this,kdvDataPath,[114.0253382853974,22.442117078178544],12);
            console.log(kdv.callKdvCpp(this));
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

        loadTest(){
            test.testTree(this);
        }

    },

    //挂载
    mounted() {
        console.log("mounted")
        this.loadDSPGS('StockholmSweden', 13)
    },
})
