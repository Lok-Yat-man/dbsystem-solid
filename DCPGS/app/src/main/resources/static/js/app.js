import normal from "./normal.js";
mapboxgl.accessToken = 'pk.eyJ1IjoiY29uZ3dhbmciLCJhIjoiY2tjZWwxNW5uMDdoMjJ3cDZnaGF2bmJlYiJ9.NOKscgbt1C-DCo38sxtUFw';
new Vue({
    el: "#app",
    data(){
        return {
            map: "",
            API_TOKEN: "c721d12c7b7f41d2bfc7d46a796b1d50",
            clusters: "",
            clusterNums: 10,
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
                this.clusters = jsonData.data;
                vueThis.initMap(geoJsonPath, zoom);
            });
        },

        //加载地图并添加地点标记
        loadMarkers(){
            this.map.setCenter([this.clusters[0].checkIns[0].longitude,this.clusters[0].checkIns[0].latitude]);
            for(let i=0;i<this.clusterNums;++i){
                let clusterId = this.clusters[i].clusterId;
                let color = normal.getColor(clusterId,this.clusterNums);
                let locations = this.clusters[i].checkIns;
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
                for(let i = 0;i<vueThis.clusterNums;++i){
                    vueThis.map.addLayer({
                        id: 'layer'+i,
                        type: 'circle',
                        source: 'points-source',
                        filter: ['==', 'clusterId', i],
                        paint: {
                            'circle-radius': 3.5,
                            'circle-color': normal.getColor(i,vueThis.clusterNums),
                            'circle-opacity': 0.7,
                        },
                    });
                }
            });
            this.loadMarkers();
        },

        loadDSPGS(location){
            normal.loadDCPGS(this,location)
        },
    },

    //挂载
    mounted() {
        console.log("mounted")
        this.loadDSPGS("Australia");
    },
})
