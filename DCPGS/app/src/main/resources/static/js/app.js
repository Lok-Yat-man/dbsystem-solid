import normal from "./normal.js";
import kdv from "./kdv.js";
mapboxgl.accessToken = 'pk.eyJ1IjoiY29uZ3dhbmciLCJhIjoiY2tjZWwxNW5uMDdoMjJ3cDZnaGF2bmJlYiJ9.NOKscgbt1C-DCo38sxtUFw';
new Vue({
    el: "#app",
    data(){
        return {
            map: "",
            API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
            env: "local",
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
                // url: "http://localhost:8080/dcpgs/gowalla"
                url: clusterPath
            }).then(response => {
                const jsonData = response.data;
                console.log(jsonData);
                this.DCPGS.clusters = jsonData.data;
                vueThis.initMap(geoJsonPath, zoom);
            });
        },

        //加载地图并添加地点标记
        loadMarkers(){
            this.map.setCenter([this.DCPGS.clusters[0].checkIns[0].longitude,this.DCPGS.clusters[0].checkIns[0].latitude]);
            for(let i=0;i<this.DCPGS.clusterNums;++i){
                let clusterId = this.DCPGS.clusters[i].clusterId;
                let color = normal.getColor(clusterId,this.DCPGS.clusterNums);
                let locations = this.DCPGS.clusters[i].checkIns;
                for(let j=0;j<1;++j){
                    let checkIn = locations[j];
                    let marker = normal.getDefaultMark(checkIn.longitude,checkIn.latitude, color)
                        .addTo(this.map);
                }
            }
        },

        //初始化mapbox
        initMap(geoJsonPath, zoom){
            this.map = new mapboxgl.Map({
                container: 'map', // container id
                // style: 'mapbox://styles/congwang/ckm4lk51m0mle17pddig9wem1',
                // style: 'mapbox://styles/mapbox/light-v11',
                style: 'mapbox://styles/mapbox/streets-v12',
                // style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + this.API_TOKEN,
                center: [-97.7575966669, 30.2634181234],
                // starting position [lng, lat]
                zoom: zoom
            });
            let vueThis = this;
            this.map.on('load', function () {
                // 添加 GeoJSON 数据源
                vueThis.map.addSource('points-source', {
                    type: 'geojson',
                    // data: 'data/EuropeTop20G.geojson', // 替换为包含数据的文件路径或URL
                    data: geoJsonPath
                });
                for(let i = 0;i<vueThis.DCPGS.clusterNums;++i){
                    vueThis.map.addLayer({
                        id: 'layer'+i,
                        type: 'circle',
                        source: 'points-source',
                        filter: ['==', 'clusterId', i],
                        paint: {
                            'circle-radius': 3.5,
                            'circle-color': normal.getColor(i,vueThis.DCPGS.clusterNums),
                            'circle-opacity': 0.7,
                        },
                    });
                }
            });
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

        loadDSPGS(location){
            normal.loadDCPGS(this,location)
        },

        loadKDV(){
            let kdvDataPath = "data/kdv/kdv2.geojson"
            if(this.env === "local"){
                kdvDataPath = "data/kdv/kdv2.geojson"
            }else if(this.env === "prod") {
                kdvDataPath = "localhost:8080/kdv/geojson"
            }
            kdv.loadKDV(this,kdvDataPath);
        }
    },

    //挂载
    mounted() {
        console.log("mounted")
        // this.loadDSPGS("Europe");
        this.loadKDV();
    },
})
