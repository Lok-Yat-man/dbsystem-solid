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

export default {
    getCustomMark,
    getDefaultMark,
    getColor,
}
