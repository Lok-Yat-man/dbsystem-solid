import util from "./utils.js";

function loadHeatMap(vueThis) {
    return new Promise((resolve, reject) => {
        vueThis.map = new mapboxgl.Map({
            container: 'map', // container id
            style: vueThis.mapStyle,
            center: [(vueThis.kdv.x_U + vueThis.kdv.x_L) / 2,
            (vueThis.kdv.y_L + vueThis.kdv.y_U) / 2],
            zoom: 10.9
        });
        vueThis.map.on('load', function () {
            // getBounds(vueThis);
            let request = compute(vueThis.kdv);
            axios.post(vueThis.baseUrl + "/kdv/geojson", request)
                .then(function (response) {
                    //console.log(response.data);
                    // 添加 GeoJSON 数据源
                    vueThis.map.addSource('points-source', {
                        type: 'geojson',
                        data: response.data
                    });
                    //console.log(response.data);
                    buildHeatmap(vueThis);
                    buildPointsmap(vueThis);
                    resolve()
                });
        });
        vueThis.map.on('zoomend', function () {
            vueThis.map.removeLayer('trees-heat');
            vueThis.map.removeLayer('trees-points');
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
                    buildPointsmap(vueThis);

                });
        });
        // vueThis.map.on('move', function() {
        //     vueThis.map.removeLayer('trees-heat');
        //     getBounds(vueThis);
        //     let request = compute(vueThis.kdv);
        //     axios.post(vueThis.baseUrl + "/kdv/geojson", request)
        //         .then(function (response) {
        //             console.log(response.data);
        //             // 添加 GeoJSON 数据源
        //             vueThis.map.removeSource('points-source');
        //             vueThis.map.addSource('points-source', {
        //                 type: 'geojson',
        //                 data: response.data
        //             });
        //             console.log(response.data);
        //             buildHeatmap(vueThis);
        //         });
        // });
    })

}

function getBounds(vueThis) {
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

function computeHeatForCell(xMin, yMin, xMax, yMax, geojsonData) {
    let totalHeat = 0;
    let count = 0;

    // 遍历 GeoJSON 数据中的所有点
    geojsonData.features.forEach(function (point) {
        const coordinates = point.geometry.coordinates;
        const dph = point.properties.dph;

        // 检查点是否在当前网格内
        if (coordinates[0] >= xMin && coordinates[0] <= xMax &&
            coordinates[1] >= yMin && coordinates[1] <= yMax) {
            // 将该点的 dph 值加到总热力值中
            totalHeat += dph;
            count++;
        }
    });

    // 如果该网格内有点，返回平均 dph 作为热力值
    return count > 0 ? totalHeat / count : 0; // 如果没有点，返回 0
}

function updateHeatMap(vueThis) {
    vueThis.map.removeLayer('trees-heat');
    getBounds(vueThis);
    let request = compute(vueThis.kdv);
    axios.post(vueThis.baseUrl + "/kdv/geojson", request)
        .then(function (response) {
            // 更新 GeoJSON 数据源
            let geojson = {
                "type": "FeatureCollection",
                "features": computeHeatMatrix(vueThis.kdv, response.data)
            };
            // console.log(geojson)
            vueThis.map.removeSource('points-source');
            vueThis.map.addSource('points-source', {
                type: 'geojson',
                data: geojson
            });
            buildMatrixHeatmap(vueThis);  // 重建矩阵热力图
        });
}
function computeHeatMatrix(kdv, geojson) {
    const gridSize = 200;  // 假设每个矩形的大小
    const matrix = [];
    const gridWidth = (kdv.x_U - kdv.x_L) / gridSize;
    const gridHeight = (kdv.y_U - kdv.y_L) / gridSize;

    // 遍历每个网格，计算热力值
    for (let i = 0; i < gridSize; i++) {
        for (let j = 0; j < gridSize; j++) {
            const xMin = kdv.x_L + i * gridWidth;
            const yMin = kdv.y_L + j * gridHeight;
            const xMax = xMin + gridWidth;
            const yMax = yMin + gridHeight;

            // 计算当前网格内的热力值（例如，基于该区域内的数据点数量）
            const heatValue = computeHeatForCell(xMin, yMin, xMax, yMax, geojson);

            matrix.push({
                type: 'Feature',
                geometry: {
                    type: 'Polygon',
                    coordinates: [[
                        [xMin, yMin],
                        [xMax, yMin],
                        [xMax, yMax],
                        [xMin, yMax],
                        [xMin, yMin]
                    ]]
                },
                properties: {
                    heat_value: heatValue
                }
            });
        }
    }
    return matrix;
}

function buildPointsmap(vueThis) {
    vueThis.map.addLayer(
        {
            'id': 'trees-points',
            'type': 'circle',
            'source': 'points-source',
            'minzoom': 14,
            'paint': {
                'circle-radius': {
                    'property': 'dbh',
                    'type': 'exponential',
                    'stops': [
                        [{ zoom: 15, value: 1 }, 5],
                        [{ zoom: 15, value: 62 }, 10],
                        [{ zoom: 22, value: 1 }, 20],
                        [{ zoom: 22, value: 62 }, 50]
                    ]
                },
                'circle-color': {
                    'property': 'dbh',
                    'type': 'exponential',
                    'stops': [
                        [0, 'rgba(236,222,239,0)'],
                        [10, 'rgb(236,222,239)'],
                        [20, 'rgb(208,209,230)'],
                        [30, 'rgb(166,189,219)'],
                        [40, 'rgb(103,169,207)'],
                        [50, 'rgb(28,144,153)'],
                        [60, 'rgb(1,108,89)']
                    ]
                },
                'circle-stroke-color': 'white',
                'circle-stroke-width': 1,
                'circle-opacity': {
                    'stops': [
                        [14, 0],
                        [15, 1]
                    ]
                }
            }
        },
        // 'waterway-label'
    );
}
function buildMatrixHeatmap(vueThis) {
    vueThis.map.addLayer({
        id: 'trees-heat',
        type: 'fill',
        source: 'points-source',
        paint: {
            // 填充颜色，基于每个矩形的热力值
            'fill-color': [
                'step',  // 使用step来创建离散的颜色
                ['get', 'heat_value'],  // 获取每个单元的热力值
                'rgba(0, 0, 0, 0)',  // 如果 heat_value 为 0，颜色为透明
                0.05, 'rgb(37, 52, 148)',  // 当 heat_value >= 0.05 时，颜色为深蓝色
                0.1, 'rgb(8, 69, 148)',  // 当 heat_value >= 0.1 时，颜色为蓝色
                0.2, 'rgb(7, 107, 158)',  // 当 heat_value >= 0.2 时，颜色为浅蓝色
                0.3, 'rgb(0, 150, 169)',  // 当 heat_value >= 0.3 时，颜色为青色
                0.4, 'rgb(0, 188, 160)',  // 当 heat_value >= 0.4 时，颜色为浅青色
                0.5, 'rgb(95, 205, 125)',  // 当 heat_value >= 0.5 时，颜色为浅绿色
                0.6, 'rgb(199, 233, 109)',  // 当 heat_value >= 0.6 时，颜色为黄色
                0.7, 'rgb(255, 241, 118)',  // 当 heat_value >= 0.7 时，颜色为亮黄色
                0.8, 'rgb(255, 160, 64)',  // 当 heat_value >= 0.8 时，颜色为橙色
                0.9, 'rgb(240, 28, 28)'   // 当 heat_value >= 0.9 时，颜色为红色
            ],
            // 设置透明度，增加渐变效果
            'fill-opacity': vueThis.kdv.opacity
        }
    });
}

function buildHeatmap(vueThis) {
    vueThis.map.addLayer({
        id: 'trees-heat', // 图层ID
        type: 'heatmap',  // 图层类型为热力图
        source: 'points-source', // 数据源
        maxzoom: 24,  // 最大缩放级别
        minzoom: 3,   // 最小缩放级别
        paint: {
            // 根据直径胸高（dph）来增加权重
            "heatmap-weight": {
                property: 'dph',  // 使用'dph'属性来计算权重
                type: 'exponential',
                stops: [
                    [0, 0],
                    [1, 0.25], // 设置热力图点权重
                ]
            },
            // 随着地图缩放级别增加热力图强度
            'heatmap-intensity': 1,  // 热力图的强度
            // 设置颜色的渐变方式，密度越高颜色越热（从透明到红色）
            'heatmap-color': [
                'step',
                ['heatmap-density'],
                'rgba(0, 0, 0, 0)',
                0.05, 'rgb(37, 52, 148)',
                0.1, 'rgb(8, 69, 148)',
                0.2, 'rgb(7, 107, 158)',
                0.3, 'rgb(0, 150, 169)',
                0.4, 'rgb(0, 188, 160)',
                0.5, 'rgb(95, 205, 125)',
                0.6, 'rgb(199, 233, 109)',
                0.7, 'rgb(255, 241, 118)',
                0.8, 'rgb(255, 160, 64)',
                0.85, 'rgb(230, 150, 64)',
                0.9, 'rgb(230, 116, 40)',
                0.95, 'rgb(218, 95, 42)',
                0.97, 'rgb(216, 135, 34)',
                0.98, 'rgb(200, 93, 37)',
                0.985, 'rgb(199, 109, 65)',
                0.99, 'rgb(191, 86, 34)',
                0.995, 'rgb(191, 86, 34)',
                0.998, 'rgb(191, 86, 34)',
                1.0, 'rgb(240, 28, 28)',
            ],
            // 设置热力点的半径（随着缩放级别变化）
            'heatmap-radius': {
                stops: [
                    [11.5, 25],  // 在某个缩放级别下半径为25
                    [24, 40]     // 在最大缩放级别下半径为40
                ]
            },
            // 设置热力图的透明度
            'heatmap-opacity': vueThis.kdv.opacity
        }
    });
}

function compute(kdv) {
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
function updateOpacity(vueThis) {
    vueThis.map.removeLayer("trees-heat");
    buildHeatmap(vueThis);
    // updateHeatMap(vueThis)
}

export default {
    loadHeatMap,
    updateOpacity,
}
