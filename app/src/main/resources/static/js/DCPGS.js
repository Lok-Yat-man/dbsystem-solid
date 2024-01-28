import utils from "./utils.js";

function getPathFromLocation(location, env, dataset) {
    let basePath = "http://localhost:8080/dcpgs/" + dataset;
    let geoJsonPath = "";
    let clusterPath = "";
    if (env === "local") {
        geoJsonPath = "data/geoJson/" + location + ".geojson";
        clusterPath = "./data/" + location + ".json";
    } else if (env === "prod") {
        geoJsonPath = basePath + "/geoJson/" + location;
        clusterPath = basePath + "/json/" + location;
    }
    return [clusterPath, geoJsonPath];
}

function addLayer(i,vueThis){
    let color = utils.getColor(i, vueThis.DCPGS.maxClusterNums);
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
    let color = utils.getColor(i, vueThis.DCPGS.maxClusterNums);
    vueThis.map.on('click', 'layer' + i, function (e) {
        let coordinates = e.features[0].geometry.coordinates.slice();
        let clusterId = e.features[0].properties.clusterId;
        let index = Number(clusterId) + 1;
        utils.getPopUp("cluster " + index, false).
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

async function loadDCPGS(vueThis, location, zoom) {
    vueThis.DCPGS.location = location;
    let env = vueThis.env;
    let path = getPathFromLocation(location, env, vueThis.DCPGS.dataset);
    let basePath = "http://localhost:8080/dcpgs/" + vueThis.DCPGS.dataset;
    vueThis.sideBarDisabled = true;
    vueThis.DCPGS.loading = true;
    vueThis.mapLoading = true;
    if (env === "prod") {
        await axios({
            method: "get",
            url: basePath + "/run/" + location
        }).then((response) => {
            const runningStatus = response.data;
            console.log("DCPGS running status: " + runningStatus);
            getParams(vueThis, location);
        });
    }
    vueThis.DCPGS.maxClusterNums = await getClusters(path[1], path[0], zoom, vueThis);
    loadPoints(vueThis, path[1], zoom);
    loadMarkers(vueThis);
    vueThis.sideBarDisabled = false;
    vueThis.DCPGS.loading = false;
    vueThis.mapLoading = false;
}

async function getParams(vueThis, location) {
    let basePath = "http://localhost:8080/dcpgs/" + vueThis.DCPGS.dataset
    await axios({
        method: "get",
        url: basePath + "/params/" + location
    }).then(response => {
        const params = response.data;
        console.log("location: " + location + " params: ");
        console.log(params);
        vueThis.DCPGS.params = params;
    });
}

async function updateParams(vueThis) {
    let basePath = "http://localhost:8080/dcpgs/" + vueThis.DCPGS.dataset;
    await axios({
        method: "put",
        data: vueThis.DCPGS.params,
        url: basePath + "/params/" + vueThis.DCPGS.location
    }).then(response => {
        const status = response.data;
        console.log("update status: " + status);
    });
    await loadDCPGS(vueThis, vueThis.DCPGS.location, vueThis.map.getZoom());
}

function loadPoints(vueThis, geoJsonPath, zoom) {
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        // style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [-97.7575966669, 30.2634181234],
        zoom: zoom
    });

    vueThis.map.on('load', function () {

        vueThis.map.addSource('points-source', {
            type: 'geojson',
            data: geoJsonPath
        });
        for (let i = 0; i < vueThis.DCPGS.clusterNums; ++i)
            addLayer(i,vueThis);
        vueThis.DCPGS.layerLoaded = vueThis.DCPGS.clusterNums;
    });
}

//加载地图并添加地点标记
function loadMarkers(vueThis) {

    vueThis.map.setCenter([vueThis.DCPGS.clusters[0].checkIns[0].longitude,
        vueThis.DCPGS.clusters[0].checkIns[0].latitude]);

    let makers = [];
    for (let i = 0; i < vueThis.DCPGS.maxClusterNums; ++i) {
        let clusterId = vueThis.DCPGS.clusters[i].clusterId;
        let color = utils.getColor(clusterId, vueThis.DCPGS.maxClusterNums);
        let locations = vueThis.DCPGS.clusters[i].checkIns;
        let checkIn = locations[0];
        let marker = utils.getDefaultMark(checkIn.longitude, checkIn.latitude, color);
        marker.setPopup(utils.getPopUp("cluster " + (i+1), false));
        makers.push(marker);
        if(i < vueThis.DCPGS.clusterNums) {
            marker.addTo(vueThis.map);
        }
    }
    vueThis.DCPGS.markers = makers;
    console.log("maker nums: ",makers.length)
}

//HTTP请求获取数据
async function getClusters(geoJsonPath, clusterPath, zoom, vueThis) {
    let nums = 0;
    await axios({
        method: "get",
        url: clusterPath
    }).then(response => {
        const jsonData = response.data;
        vueThis.DCPGS.clusters = jsonData.data;
        vueThis.DCPGS.maxClusterNUms = vueThis.DCPGS.clusters.length;
        vueThis.DCPGS.clusterNums = Math.min(10, vueThis.DCPGS.maxClusterNUms);
        nums = vueThis.DCPGS.clusters.length;
        console.log("get cluster finished, clusterNums: ", vueThis.DCPGS.maxClusterNUms);
    });
    return nums;
}

function updateClusterNums(vueThis){
    let dcpgs = vueThis.DCPGS;
    if(dcpgs.clusterNums < dcpgs.layerLoaded){
        for(let i = dcpgs.clusterNums;i<dcpgs.layerLoaded;++i){
            vueThis.map.removeLayer("layer"+i);
            vueThis.DCPGS.markers[i].remove();
        }
    }else if(dcpgs.clusterNums > dcpgs.layerLoaded){
        for(let i = dcpgs.layerLoaded;i<dcpgs.clusterNums;++i){
            addLayer(i,vueThis);
            vueThis.DCPGS.markers[i].addTo(vueThis.map);
        }
    }
    dcpgs.layerLoaded = dcpgs.clusterNums;
}

export default {
    loadDCPGS,
    updateParams,
    updateClusterNums,
}
