import util from "./utils.js";

function loadHeatMap(vueThis){
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: vueThis.mapStyle,
        center: [(vueThis.kdv.x_U + vueThis.kdv.x_L)/2,
            (vueThis.kdv.y_L + vueThis.kdv.y_U)/2],
        zoom: 10.9
    });
    vueThis.map.on('load', function () {
        getBounds(vueThis);
        let request = compute(vueThis.kdv);
        axios.post(vueThis.baseUrl + "/kdv/geojson", request)
            .then(function (response) {
                console.log(response.data);
                // 添加 GeoJSON 数据源
                vueThis.map.addSource('points-source', {
                    type: 'geojson',
                    data: response.data
                });
                console.log(response.data);
                buildHeatmap(vueThis);
            });
    });
    vueThis.map.on('zoomend', function() {
        vueThis.map.removeLayer('trees-heat');
        getBounds(vueThis);
        let request = compute(vueThis.kdv);
        axios.post(vueThis.baseUrl + "/kdv/geojson", request)
            .then(function (response) {
                console.log(response.data);
                // 添加 GeoJSON 数据源
                vueThis.map.removeSource('points-source');
                vueThis.map.addSource('points-source', {
                    type: 'geojson',
                    data: response.data
                });
                console.log(response.data);
                buildHeatmap(vueThis);
            });
    });
}

function getBounds(vueThis){
    let bounds = vueThis.map.getBounds(); // 获取当前地图的边界
    if (bounds) {
        let sw = bounds.getSouthWest(); // 西南角经纬度
        let ne = bounds.getNorthEast(); // 东北角经纬度
        vueThis.kdv.x_L = sw.lng;
        vueThis.kdv.x_U = ne.lng;
        vueThis.kdv.y_L = sw.lat;
        vueThis.kdv.y_U = ne.lat;
    }
}

function buildHeatmap(vueThis){
    vueThis.map.addLayer(
        {
            id: 'trees-heat',
            type: 'heatmap',
            source: 'points-source',
            maxzoom: 24,
            minzoom: 9,
            paint: {
                // increase weight as diameter breast height increases
                "heatmap-weight": {
                    property: 'dph',
                    type: 'exponential',
                    stops: [
                        [0, 0],
                        [1, 1]
                    ]
                },
                // increase intensity as zoom level increases
                'heatmap-intensity': {
                    stops: [
                        [15, 1],
                        [24, 1]
                    ]
                },
                // assign color values be applied to points depending on their density
                'heatmap-color': [
                    'interpolate',
                    ['linear'],
                    ['heatmap-density'],
                    0,
                    "rgba(0, 0, 0, 0)",
                    0.1,
                    "rgb(94,79,162)",
                    0.2,
                    "rgb(50,136,189)",
                    0.3,
                    "rgb(102,194,165)",
                    0.4,
                    "rgb(171,221,164)",
                    0.5,
                    "rgb(230,245,152)",
                    0.6,
                    "rgb(254,224,139)",
                    0.7,
                    "rgb(253,174,97)",
                    0.8,
                    "rgb(244,109,67)",
                    0.9,
                    "rgb(213,62,79)",
                    1.0,
                    "rgb(158,1,66)"
                ],
                // increase radius as zoom increases
                'heatmap-radius': {
                    stops: [
                        [11.5,10],
                        [24, 30]
                    ]
                },
                // decrease opacity to transition into the circle layer
                'heatmap-opacity': {
                    default: 0.93,
                    stops: [
                        [9, 0.93],
                        [24, 0.93]
                    ]
                }
            }
        },
    );
}

function compute(kdv){
    // Module.onRuntimeInitialized = function() {
    // };
    // return Module.compute(kdv.kdv_type, kdv.num_threads, kdv.x_L, kdv.x_U,
    //     kdv.y_L,kdv.y_U,kdv.row_pixels,kdv.col_pixels,kdv.kernel_s_type, kdv.bandwidth_s,
    //     kdv.t_L,kdv.t_U,kdv.kernel_s_type,kdv.bandwidth_t,kdv.cur_time);

    // dbsystem
    Module.load_data();
    return Module.compute(1, 1, 0.01, 250, 250,
        1904, 12169, kdv.x_L, kdv.x_U, kdv.y_L, kdv.y_U, kdv.t_L, kdv.t_U, kdv.cur_time, kdv.bandwidth_t);
    // return Module.compute(1, 1, kdv.bandwidth_s, kdv.row_pixels, kdv.col_pixels,
    //     st, ed, kdv.x_L, kdv.x_U, kdv.y_L, kdv.y_U, kdv.t_L, kdv.t_U, kdv.cur_time, kdv.bandwidth_t);
}

export default {
    loadHeatMap,
}
