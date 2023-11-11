function loadHeatMap(vueThis, path, center, zoom){
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: 'mapbox://styles/mapbox/light-v11',
        // style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: center,
        zoom: zoom
    });
    vueThis.map.on('load', function () {
        // 添加 GeoJSON 数据源
        vueThis.map.addSource('points-source', {
            type: 'geojson',
            // data: 'data/EuropeTop20G.geojson', // 替换为包含数据的文件路径或URL
            data: path
        });
        vueThis.map.addLayer(
            {
                id: 'trees-heat',
                type: 'heatmap',
                source: 'points-source',
                maxzoom: 16,
                paint: {
                    // increase weight as diameter breast height increases
                    'heatmap-weight': {
                        // property: 'clusterId',
                        type: 'exponential',
                        stops: [
                            [1, 0],
                            [62, 1]
                        ]
                    },
                    // increase intensity as zoom level increases
                    'heatmap-intensity': {
                        stops: [
                            [11, 1],
                            [15, 3]
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
                            [11, 15],
                            [15, 20]
                        ]
                    },
                    // decrease opacity to transition into the circle layer
                    'heatmap-opacity': {
                        default: 1,
                        stops: [
                            [14, 1],
                            [15, 0]
                        ]
                    }
                }
            },
        );
    });
}

function compute(kdv){
    // Module.onRuntimeInitialized = function() {
    // };
    return Module.compute(kdv.kdv_type, kdv.num_threads, kdv.x_L, kdv.x_U,
        kdv.y_L,kdv.y_U,kdv.row_pixels,kdv.col_pixels,kdv.kernel_s_type, kdv.bandwidth_s,
        kdv.t_L,kdv.t_U,kdv.kernel_s_type,kdv.bandwidth_t,kdv.cur_time);
}

function callKdvCpp(vueThis){
    // 2, 1, 113.5, 114.5, 22, 22.6, 10, 10, 1, 1000, 1, 1, 1, 1000, 1
    return compute(vueThis.kdv);
}

export default {
    loadHeatMap,
    callKdvCpp,
}
