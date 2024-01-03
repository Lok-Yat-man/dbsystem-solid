import util from "./utils.js";

function loadHeatMap(vueThis){
    let request = compute(vueThis.kdv);
    console.log(request);
    axios.post("http://localhost:8080/kdv/geojson", request)
        .then(function (response) {
            console.log(response.data);
            vueThis.map = new mapboxgl.Map({
                container: 'map', // container id
                style: 'mapbox://styles/mapbox/light-v11',
                center: response.data.features[0].geometry.coordinates,
                zoom: 9
            });
            vueThis.map.on('load', function () {
                // 添加 GeoJSON 数据源
                vueThis.map.addSource('points-source', {
                    type: 'geojson',
                    data: response.data
                });
                console.log(response.data);
                vueThis.map.addLayer(
                    {
                        id: 'trees-heat',
                        type: 'heatmap',
                        source: 'points-source',
                        maxzoom: 24,
                        minzoom: 5,
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
                                    [24, 3]
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
                                    [9,10],
                                    [24, 30]
                                ]
                            },
                            // decrease opacity to transition into the circle layer
                            'heatmap-opacity': {
                                default: 0.6,
                                stops: [
                                    [9, 0.6],
                                    [24, 0.6]
                                ]
                            }
                        }
                    },
                );
            });
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
    let request = compute(vueThis.kdv);
    axios.post("http://localhost:8080/kdv/geojson", request)
        .then(function (response) {
            console.log(response.data);
            vueThis.map = new mapboxgl.Map({
                container: 'map', // container id
                style: 'mapbox://styles/mapbox/light-v11',
                center: [114.1161616, 22.36363636],
                zoom: 9
            });
            vueThis.map.on('load', () => {
                vueThis.map.addSource('kdvPolygon', {
                    'type': 'geojson',
                    'data': response.data
                });
                response.data.features.forEach((feature, i) => {
                    vueThis.map.addLayer({
                        'id': 'poly' + i,
                        'type': 'fill',
                        'source': 'kdvPolygon',
                        'layout': {},
                        "filter": ["==", "index", i],
                        'paint': {
                            'fill-color': feature.properties.color,
                            'fill-opacity': 0.5
                        }
                    });
                    // 添加轮廓
                    vueThis.map.addLayer({
                        'id': 'outline' + i,
                        'type': 'line',
                        'source': 'kdvPolygon',
                        'layout': {},
                        "filter": ["==", "index", i],
                        'paint': {
                            'line-color': feature.properties.color,
                            'line-width': 2,
                            'line-opacity': 0.5
                        }
                    });
                });
            });
        });
}

export default {
    loadHeatMap,
    callKdvCpp,
}
