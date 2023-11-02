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
    // 数据集，这里假设有 n 个 div
    let data = d3.range(size);
    // 使用 ColorBrewer 调色板
    let colorScale = d3.scaleOrdinal()
        .domain(data) // 数据域
        .range(d3.schemeCategory10); // 使用 ColorBrewer 的 Category10 调色板
    console.log(colorScale(clusterId));
    return colorScale(clusterId)
}

//message可以是html
function getPopUp(message, needCloseButton){
    //添加气泡弹窗
    let mhtml = '<div class="malert">' +
                                    message +
                        '</div>'
    let popup = new mapboxgl.Popup({closeButton: needCloseButton})
        .setHTML(mhtml)
    return popup;
}

export default {
    getCustomMark,
    getDefaultMark,
    getColor,
    getPopUp,
}
