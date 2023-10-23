import dcpgs from "./DCPGS.js";
import kdv from "./kdv.js";
mapboxgl.accessToken = 'pk.eyJ1IjoiY29uZ3dhbmciLCJhIjoiY2tjZWwxNW5uMDdoMjJ3cDZnaGF2bmJlYiJ9.NOKscgbt1C-DCo38sxtUFw';
new Vue({
    el: "#app",
    data(){
        return {
            map: "",
            API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
            env: "prod",
            DCPGS: {
                enable: true,
                clusters: "",
                clusterNums: 10,
                epsilon: 0.5,
                maxD: 120,
                omega: 0.5,
                tau: 0.7
            }
        }
    },
    methods: {
        //HTTP请求获取数据
        getClusters(geoJsonPath, clusterPath, zoom){
            let vueThis = this;
            axios({
                method: "get",
                url: clusterPath
            }).then(response => {
                const jsonData = response.data;
                this.DCPGS.clusters = jsonData.data;
                this.DCPGS.clusterNums = this.DCPGS.clusters.length;
                console.log(this.DCPGS.clusterNums);
                vueThis.initMap(geoJsonPath, zoom);
            });
        },

        //加载地图并添加地点标记
        loadMarkers(){
            this.map.setCenter([this.DCPGS.clusters[0].checkIns[0].longitude,this.DCPGS.clusters[0].checkIns[0].latitude]);
            for(let i=0;i<this.DCPGS.clusterNums;++i){
                let clusterId = this.DCPGS.clusters[i].clusterId;
                let color = dcpgs.getColor(clusterId,this.DCPGS.clusterNums);
                console.log("cluster: " + i +"color: " + color);
                let locations = this.DCPGS.clusters[i].checkIns;
                for(let j=0;j<1;++j){
                    let checkIn = locations[j];
                    let marker = dcpgs.getDefaultMark(checkIn.longitude,checkIn.latitude, color)
                        .addTo(this.map);
                }
            }
        },

        //初始化mapbox
        initMap(geoJsonPath, zoom){
            dcpgs.loadPoints(this,geoJsonPath,zoom);
            this.loadMarkers();
        },

        paramsSwitch(){
            let paramsDis = document.getElementById("DCPGSParams")
                .style.display;
            if(paramsDis === "none"){
                document.getElementById("DCPGSParams")
                    .style.display = "";
                document.getElementById("DCPGSParamsSwitch")
                    .style.display = "none";
            }else{
                document.getElementById("DCPGSParams")
                    .style.display = "none";
                document.getElementById("DCPGSParamsSwitch")
                    .style.display = "";
            }
        },

        loadDSPGS(location, zoom){
            dcpgs.loadDCPGS(this,location, zoom);
        },

        loadKDV(){
            let kdvDataPath = "data/kdv/kdv2.geojson"
            if(this.env === "local"){
                kdvDataPath = "data/kdv/kdv2.geojson"
            }else if(this.env === "prod") {
                kdvDataPath = "http://localhost:8080/kdv/geojson"
            }
            kdv.loadHeatMap(this,kdvDataPath,[114.0253382853974,22.442117078178544],12);
        }
    },

    //挂载
    mounted() {
        console.log("mounted")
        this.loadKDV();
    },
})
