import utils from "./utils.js";
function loadDCPGS(vueThis, location, zoom){
    vueThis.DCPGS.location = location;
    let env = vueThis.env;
    let basePath = "http://localhost:8080/dcpgs/";
    let geoJsonPath = "";
    let clusterPath = "";
    if(env === "local") {
        geoJsonPath = "data/geoJson/" + location + ".geojson";
        clusterPath = "./data/" + location + ".json";
        getClusters(geoJsonPath, clusterPath, zoom, vueThis);
    }else if(env === "prod") {
        geoJsonPath = basePath + "gowalla/geoJson/" + location;
        clusterPath = basePath + "gowalla/json/" + location;
        axios({
            method: "get",
            url: basePath + "gowalla/run/" + location
        }).then(response => {
            const runningStatus = response.data;
            console.log("DCPGS running status: " + runningStatus);
            getParams(vueThis,location);
            getClusters(geoJsonPath, clusterPath, zoom, vueThis);
        });
    }
}

function getParams(vueThis, location){
    let basePath = "http://localhost:8080/dcpgs/";
    axios({
        method: "get",
        url: basePath + "gowalla/params/" + location
    }).then(response => {
        const params = response.data;
        console.log("location: "+ location +" params: " + params);
        vueThis.DCPGS.params = params;
    });
}

function updateParams(vueThis){
    let basePath = "http://localhost:8080/dcpgs/";
    axios({
        method: "put",
        data: vueThis.DCPGS.params,
        url: basePath + "gowalla/params/" + vueThis.DCPGS.location
    }).then(response => {
        const status = response.data;
        console.log("update status: " + status);
        loadDCPGS(vueThis, vueThis.DCPGS.location, vueThis.map.getZoom());
    });
}

function loadPoints(vueThis, geoJsonPath, zoom){
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        // style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [-97.7575966669, 30.2634181234],
        zoom: zoom
    });
    vueThis.map.on('load', function () {
        vueThis.map.addSource('points-source', {
            type: 'geojson',
            data: geoJsonPath
        });
        for(let i = 0;i<vueThis.DCPGS.clusterNums;++i){
            vueThis.map.addLayer({
                id: 'layer'+i,
                type: 'circle',
                source: 'points-source',
                filter: ['==', 'clusterId', ""+i],
                paint: {
                    'circle-radius': 3.5,
                    'circle-color': utils.getColor(i,vueThis.DCPGS.clusterNums),
                    'circle-opacity': 0.7,
                },
            });
        }
    });
}

//加载地图并添加地点标记
function loadMarkers(vueThis){
    vueThis.map.setCenter([vueThis.DCPGS.clusters[0].checkIns[0].longitude,
        vueThis.DCPGS.clusters[0].checkIns[0].latitude]);
    for(let i=0;i<vueThis.DCPGS.clusterNums;++i){
        let clusterId = vueThis.DCPGS.clusters[i].clusterId;
        let color = utils.getColor(clusterId,vueThis.DCPGS.clusterNums);
        let locations = vueThis.DCPGS.clusters[i].checkIns;
        for(let j=0;j<1;++j){
            let checkIn = locations[j];
            utils.getDefaultMark(checkIn.longitude,checkIn.latitude, color)
                .addTo(vueThis.map);
        }
    }
}

//HTTP请求获取数据
function getClusters(geoJsonPath, clusterPath, zoom, vueThis){
    axios({
        method: "get",
        url: clusterPath
    }).then(response => {
        const jsonData = response.data;
        vueThis.DCPGS.clusters = jsonData.data;
        vueThis.DCPGS.clusterNums = vueThis.DCPGS.clusters.length;
        console.log(vueThis.DCPGS.clusterNums);
        loadPoints(vueThis,geoJsonPath,zoom);
        loadMarkers(vueThis);
    });
}

export default {
    loadDCPGS,
    updateParams,
}
