
function loadHeatMap(vueThis) {
    return new Promise((resolve, reject) => {
        vueThis.map = new mapboxgl.Map({
            container: 'map', // container id
            style: vueThis.mapStyle,
            center: [-122.4194, 37.7749], // 修改为旧金山的经纬度
            zoom: 13  // 调整缩放级别以更好地显示城市
        });
        vueThis.map.on('load', function () {

            vueThis.map.addSource('nkdv', {
                type: 'geojson',
                data: `/data/NKDV/network_density_${vueThis.NKDV.bandwidth}.geojson`
            });

            // 根据 density 渐变颜色渲染
            vueThis.map.addLayer({
                id: 'nkdv-lines',
                type: 'line',
                source: 'nkdv',
                paint: {
                    'line-width': 4,
                    'line-color': [
                        'interpolate',
                        ['linear'],
                        // 使用 normalize 表达式进行归一化
                        ['get', 'density'],
                        0, '#0000ff',    // 深蓝色 - 最低密度
                        0.2, '#00ffff',  // 青色
                        0.4, '#00ff00',  // 绿色
                        0.6, '#ffff00',  // 黄色
                        0.8, '#ff8c00',  // 橙色
                        1.0, '#ff0000'   // 红色 - 最高密度
                    ],
                    'line-opacity': vueThis.NKDV.opacity
                }
            });

            resolve()
        });

        vueThis.map.on('zoomend', function () {
            // upadateMap(vueThis)
        });

        vueThis.map.on('moveend', function () {
            // upadateMap(vueThis)
        });
    })

}

function upadateMap(vueThis) {

    console.log("geojson")
    vueThis.map.getSource('nkdv').setData(`/data/NKDV/network_density_${vueThis.NKDV.bandwidth}.geojson`);

}

export default {
    loadHeatMap,
    upadateMap
}