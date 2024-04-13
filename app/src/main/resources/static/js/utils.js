//获取对应颜色的地图标记 还没调好不好看
function getCustomMark(lon, lat, index){
    let i = index % 12 + 1;
    //自定义样式
    let customMarker = document.createElement('img');
    customMarker.className = 'marker-new'; // 自定义CSS类名
    customMarker.src = './img/point/point_'+ i +'.png'; // 设置标记的图片

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

//获取对应颜色的地图标记
function currentPosition(lon,lat){
    //默认样式
    return getCustomMark(lon,lat,1)
}

//根据集群数量将0xffffff颜色均匀划分后分配
function getColor(clusterId,size){
    // 数据集，这里假设有 n 个 div
    let data = d3.range(size);
    // 使用 ColorBrewer 调色板
    let colorScale = d3.scaleOrdinal()
        .domain(data) // 数据域
        .range(d3.schemeCategory10); // 使用 ColorBrewer 的 Category10 调色板
    // console.log(colorScale(clusterId));
    return colorScale(clusterId)
}

let colors = [
    '#AA9915',
    '#B52020',
    '#106FB3',
    '#B06B17',
    '#B2377C',
    '#19AAB1',
    '#AA5015',
    '#B937BB',
    '#0B8F4C',
    '#6A45DC',
    '#7C960E',
    '#205AEB'
];

function getNewColor(clusterId){
    let index = clusterId % 12;
    return colors[index];
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

function getNewPopUp(title, message, needCloseButton){
    //添加气泡弹窗
    let mhtml = '<div class="popup-window">' +
        '<div class="popup-title">'+ title +
        '<img src="./img/popup/bar-nameBar.png" class="popup-img"></div>' +
        '<p class="popup-message">'+ message +'</p>' +
        '</div>'
    let popup = new mapboxgl.Popup({closeButton: needCloseButton})
        .setHTML(mhtml)
    return popup;
}

export default {
    getCustomMark,
    currentPosition,
    getDefaultMark,
    getColor,
    getPopUp,
    getNewPopUp,
    getNewColor,
}
