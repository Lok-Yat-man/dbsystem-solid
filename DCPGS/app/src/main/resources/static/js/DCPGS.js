//获取对应颜色的地图标记 还没调好不好看
function getCustomMark(lon, lat, color){
    //自定义样式
    let customMarker = document.createElement('div');
    customMarker.className = 'marker'; // 自定义CSS类名
    customMarker.style.width = '0.3%'; // 设置标记的宽度
    customMarker.style.height = '0.3%'; // 设置标记的高度
    customMarker.style.borderRadius = '5px'; // 设置标记的高度
    customMarker.style.backgroundColor = color; // 设置标记的背景颜色
    // customMarker.style.backgroundImage = './point.png';
    customMarker.style.backgroundSize = '100%';

    let marker = new mapboxgl.Marker(customMarker)
        .setLngLat([lon, lat]); // 设置标记的经纬度
    return marker;
}

//获取对应颜色的地图标记
function getDefaultMark(lon,lat,color){
    //默认样式
    let marker = new mapboxgl.Marker({
        color: color,
        scale: 0.7
    }).setLngLat([lon,lat]) // 设置点的经纬度
    return marker;
}

//根据集群数量将0xffffff颜色均匀划分后分配
function getColor(clusterId,size){
    let color = Math.round((0xffffff / size) * (clusterId + 1));
    return "#" + color.toString(16).padStart(6,"0");
}

function loadDCPGS(vueThis, location, zoom){
    vueThis.DCPGS.location = location;
    let env = vueThis.env;
    let basePath = "http://localhost:8080/dcpgs/";
    let geoJsonPath = "";
    let clusterPath = "";
    if(env === "local") {
        geoJsonPath = "data/geoJson/" + location + ".geojson";
        clusterPath = "./data/" + location + ".json";
        vueThis.getClusters(geoJsonPath, clusterPath, zoom);
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
            vueThis.getClusters(geoJsonPath, clusterPath, zoom);
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
    let normalThis = this;
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        // style: 'mapbox://styles/mapbox/light-v11',
        style: 'mapbox://styles/mapbox/streets-v12',
        // style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + this.API_TOKEN,
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
                    'circle-color': normalThis.getColor(i,vueThis.DCPGS.clusterNums),
                    'circle-opacity': 0.7,
                },
            });
        }
    });
}

export default {
    getCustomMark,
    getDefaultMark,
    getColor,
    loadDCPGS,
    loadPoints,
    updateParams,
}
