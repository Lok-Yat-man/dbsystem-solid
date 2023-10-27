import dcpgs from "./DCPGS.js";
import kdv from "./kdv.js";
mapboxgl.accessToken = 'pk.eyJ1IjoiY29uZ3dhbmciLCJhIjoiY2tjZWwxNW5uMDdoMjJ3cDZnaGF2bmJlYiJ9.NOKscgbt1C-DCo38sxtUFw';
new Vue({
    el: "#app",
    data(){
        return {
            map: "",
            API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
            env: "local",//local or prod
            switchStatus: "SWITCH",
            currentAlgorithm: 'DCPGS',
            DCPGS: {
                labelPosition: "right",
                location: "",
                clusters: "",
                clusterNums: 10,
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
                message:"",
                query:{
                    keyword: [],
                    location:{
                        longitude:0,
                        latitude:0
                    },
                    k:5,
                    epsilon: 50.0,
                    minPts:10
                }
            }
        }
    },
    methods: {
        paramsSwitch(state){
            if(state === ''){
                this.switchStatus = this.currentAlgorithm;
            }
            else if(state === 'DCPGS_UPDATE') {
                dcpgs.updateParams(this);
                this.switchStatus = "SWITCH";
            }else if(state === 'KSTC_UPDATE'){


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

        loadDSPGS(location, zoom){
            this.currentAlgorithm = "DCPGS";
            this.paramsSwitch('DCPGS');
            dcpgs.loadDCPGS(this,location, zoom);
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
        },

        loadKStc(){
            this.currentAlgorithm = "KSTC";
            this.paramsSwitch('KSTC');
        }
    },

    //挂载
    mounted() {
        console.log("mounted")
        this.loadDSPGS('StockholmSweden', 13)
    },
})
