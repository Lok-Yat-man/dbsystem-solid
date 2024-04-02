import utils from "./utils.js";

function getPathFromLocation(env, baseUrl, dataset) {
    let basePath = baseUrl + "/pa/" + dataset;
    let geoJsonPath = "";
    let clusterPath = "";
    if (env === "local") {
        geoJsonPath = "./data/paGeoJson/" + dataset + ".json";
        clusterPath = "./data/pa" + dataset+ ".json";
    } else {
        geoJsonPath = basePath + "/paGeoJson/" + dataset;
        clusterPath = basePath + "/json/" + dataset;
    }
    return [clusterPath, geoJsonPath];

}

function addLayer(i,vueThis){
    //let color = utils.getColor(i, vueThis.pa.maxClusterNums);
    let color = utils.getNewColor(i);
    vueThis.map.addLayer({
        id: 'layer' + i,
        type: 'circle',
        source: 'points-source',
        filter: ['==', 'clusterId', "" + i],
        paint: {
            'circle-radius': 3.5,
            'circle-color': color,
            'circle-opacity': 0.7,
        },
    });
    layerPopup(i, vueThis);
}

function layerPopup(i, vueThis){
    //let color = utils.getColor(i, vueThis.pa.maxClusterNums);
    let color = utils.getNewColor(i);
    vueThis.map.on('click', 'layer' + i, function (e) {
        let coordinates = e.features[0].geometry.coordinates.slice();
        let clusterId = e.features[0].properties.clusterId;
        let index = Number(clusterId) + 1;
        utils.getNewPopUp("clustering index","clustering " + index, false).
        setLngLat(coordinates).addTo(vueThis.map);
    });
    vueThis.map.on('mouseenter', 'layer' + i, () => {
        vueThis.map.removeLayer('layer' + i);
        vueThis.map.addLayer({
            id: 'layer' + i,
            type: 'circle',
            source: 'points-source',
            filter: ['==', 'clusterId', "" + i],
            paint: {
                'circle-radius': 5.0,
                'circle-color': color,
                'circle-opacity': 0.7,
            },
        });
        vueThis.map.getCanvas().style.cursor = 'pointer';
    });
    vueThis.map.on('mouseleave', 'layer' + i, () => {
        vueThis.map.removeLayer('layer' + i);
        vueThis.map.addLayer({
            id: 'layer' + i,
            type: 'circle',
            source: 'points-source',
            filter: ['==', 'clusterId', "" + i],
            paint: {
                'circle-radius': 3.5,
                'circle-color': color,
                'circle-opacity': 0.7,
            },
        });
        vueThis.map.getCanvas().style.cursor = '';
    });
}

async function loadPA(dataset,vueThis, zoom) {
    let env = vueThis.env;
    let path = getPathFromLocation(env, vueThis.baseUrl, dataset);
    console.log("clusterPath:"+path[0]+" geoJsonPath:"+path[1]);
    let basePath = vueThis.baseUrl + path[0]
    vueThis.sideBarDisabled = true;
    vueThis.pa.loading = true;
    vueThis.mapLoading = true;
    //get result
    if(env!=="local"){
      await axios({
          method: "post",
          url: vueThis.baseUrl + "/data/pa/" + dataset+".txt/run/"+dataset
      }).then((response) => {
          const runningStatus = response.data;
          console.log("PA running status: " + runningStatus);
      });
    }
    //get GeoJson
    if(env!=="local"){
      await axios({
          method: "Post",
          url: vueThis.baseUrl + "/data/pa/" +dataset+".json"
      }).then((response) => {
          const runningResult = response.data;
          console.log("GeoJson Address: " + runningResult);
      });
    }
    vueThis.pa.maxClusterNums = await getClusters(dataset, zoom, vueThis);
    loadPoints(vueThis, path[1], zoom);
    loadMarkers(vueThis);
    vueThis.sideBarDisabled = false;
    vueThis.pa.loading = false;
    vueThis.mapLoading = false;
}
//HTTP请求获取数据
async function getClusters(dataset, zoom, vueThis) {
    let nums = 0;
    await axios({
        method: "get",
        url: vueThis.baseUrl + "/data/pa/" +dataset+".json"
    }).then(response => {
        const jsonData = response.data;
        console.log("jsonData:"+jsonData);
        vueThis.pa.clusters = jsonData.data;
        console.log("jsonData.data:"+jsonData.data);
        vueThis.pa.maxClusterNums = vueThis.pa.clusters.length;
        vueThis.pa.clusterNums = Math.min(10, vueThis.pa.maxClusterNums);
        nums = vueThis.pa.clusters.length;
        console.log("get cluster finished, clusterNums: ", vueThis.pa.maxClusterNums);
    });
    return nums;
}
function loadPoints(vueThis, geoJsonPath, zoom) {
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: vueThis.mapStyle,
        // style: 'mapbox://styles/mapbox/streets-v12',
        // style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [-97.7575966669, 30.2634181234],
        zoom: zoom
    });
    console.log("geoJsonPath:"+geoJsonPath);
    vueThis.map.on('load', function () {
        vueThis.map.addSource('points-source', {
            type: 'geojson',
            data: geoJsonPath
        });
        for (let i = 0; i < vueThis.pa.clusterNums; ++i)
            addLayer(i,vueThis);
        vueThis.pa.layerLoaded = vueThis.pa.clusterNums;
    });
}

//加载地图并添加地点标记
function loadMarkers(vueThis) {

    vueThis.map.setCenter([vueThis.pa.clusters[0].checkIns[0].longitude,
        vueThis.pa.clusters[0].checkIns[0].latitude]);

    let makers = [];
    for (let i = 0; i < vueThis.pa.maxClusterNums; ++i) {
        let clusterId = vueThis.pa.clusters[i].clusterId;
        let color = utils.getColor(clusterId, vueThis.pa.maxClusterNums);
        let locations = vueThis.pa.clusters[i].checkIns;
        let checkIn = locations[0];
        let marker = utils.getCustomMark(checkIn.longitude, checkIn.latitude, i);
        marker.setPopup(utils.getNewPopUp("clustering index","cluster " + (i+1), false,));
        makers.push(marker);
        if(i < vueThis.pa.clusterNums) {
            marker.addTo(vueThis.map);
        }
    }
    vueThis.pa.markers = makers;
    console.log("maker nums: ",makers.length)
}

export default{
    loadPA,
}
