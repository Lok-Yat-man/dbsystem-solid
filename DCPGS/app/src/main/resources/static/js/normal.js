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

function loadDCPGS(vueThis, location){
    let env = "local";
    let basePath = "http://localhost:8080/dcpgs/";
    let geoJsonPath = "";
    let clusterPath = "";
    switch (location){
        case "Australia":
            vueThis.DCPGS.clusterNums = 46;
            if(env === "local") {
                geoJsonPath = "data/geoJson/Australia.geojson";
                clusterPath = "./data/Australia.json";
            }else if(env === "prod"){
                geoJsonPath = basePath + "geoJson/Australia";
                clusterPath = basePath + "json/Australia";
            }
            vueThis.getClusters(geoJsonPath, clusterPath, 3.5);
            break;
        case "Europe":
            vueThis.DCPGS.clusterNums = 10;
            if(env === "local"){
                geoJsonPath = "data/geoJson/EuropeTop20.geojson";
                clusterPath = "./data/EuropeTop500.json";
            }
            else if(env === "prod") {
                geoJsonPath = basePath + "geoJson/Europe";
                clusterPath = basePath + "json/Europe";
            }
            vueThis.getClusters(geoJsonPath, clusterPath, 3.5);
            break;
        case "NorthernAmerica":
            vueThis.DCPGS.clusterNums = 40
            if(env === "local") {
                geoJsonPath = "data/geoJson/NorthernAmericaTop40.geojson";
                clusterPath = "./data/NorthernAmericaTop40.json";
            }
            else if(env === "prod") {
                geoJsonPath = basePath + "geoJson/NorthernAmerica";
                clusterPath = basePath + "json/NorthernAmerica";
            }
            vueThis.getClusters(geoJsonPath, clusterPath,3.0);
            break;
        case "Africa":
            vueThis.DCPGS.clusterNums = 30
            if(env === "local") {
                geoJsonPath = "data/geoJson/SouthAfrica.geojson";
                clusterPath = "./data/SouthAfrica.json";
            }
            else if(env === "prod") {
                geoJsonPath = basePath + "geoJson/SouthAfrica";
                clusterPath = basePath + "json/SouthAfrica";
            }
            vueThis.getClusters(geoJsonPath, clusterPath,5.0);
            break;
        case "SoutheastAsia":
            vueThis.DCPGS.clusterNums = 30
            if(env === "local") {
                geoJsonPath = "data/geoJson/SoutheastAsia.geojson";
                clusterPath = "./data/SoutheastAsia.json";
            }
            else if(env === "prod") {
                geoJsonPath = basePath + "geoJson/SoutheastAsia";
                clusterPath = basePath + "json/SoutheastAsia";
            }
            vueThis.getClusters(geoJsonPath, clusterPath,3.0);
            break;
        case "WesternAsia":
            vueThis.DCPGS.clusterNums = 30
            if(env === "local") {
                geoJsonPath = "data/geoJson/WesternAsia.geojson";
                clusterPath = "./data/WesternAsia.json";
            }else if(env === "prod") {
                geoJsonPath = basePath + "geoJson/WesternAsia";
                clusterPath = basePath + "json/WesternAsia";
            }
            vueThis.getClusters(geoJsonPath, clusterPath,3.0);
            break;
        
    }
}

export default {
    getCustomMark,
    getDefaultMark,
    getColor,
    loadDCPGS,
}
