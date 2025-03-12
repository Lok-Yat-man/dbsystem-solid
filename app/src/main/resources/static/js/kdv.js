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
            Module.load_data();//首次计算加载数据集，不能放在compute中加载

            let request = compute(vueThis.kdv);
            vueThis.map.addSource('matrix-source', {
                type: 'geojson',
                data: {
                    "type": "FeatureCollection",
                    "features": computeHeatMatrix(vueThis.kdv, matrixDataProcess(request, vueThis.kdv.kdv_type))
                }
            });
            buildMatrixHeatmap(vueThis, 'matrix-source')
            resolve()
        });

        vueThis.map.on('zoomend', function () {
            upadateMap(vueThis)
        });

        vueThis.map.on('moveend', function () {
            upadateMap(vueThis)
        });
    })

}

function upadateMap(vueThis) {

    getBounds(vueThis);
    console.time("module")
    let request = compute(vueThis.kdv);
    console.timeEnd("module")
    // axios.post(vueThis.baseUrl + "/kdv/geojson", request)
    //     .then(function (response) {
    //         // 添加 GeoJSON 数据源
    //         vueThis.map.addSource('points-source', {
    //             type: 'geojson',
    //             data: response.data
    //         });
    //     });
    // if (vueThis.map.getSource('matrix-source'))
    //     vueThis.map.removeSource('matrix-source');
    console.time("geojson")
    vueThis.map.getSource('matrix-source').setData({
        type: "FeatureCollection",
        features: computeHeatMatrix(vueThis.kdv, matrixDataProcess(request, vueThis.kdv.kdv_type))
    });
    console.timeEnd("geojson")

    // if (vueThis.kdv.kdv_type == 1)
    //     buildMatrixHeatmap(vueThis, 'matrix-source')
    // else {

    // }
}

function updateCurrentTime(vueThis){
    if(vueThis.kdv.kdv_type==3){
        const filters = ['==', 'time', vueThis.kdv.cur_t];
        vueThis.map.setFilter('matrix-heat',filters);
    }else if(vueThis.kdv.kdv_type==1){
        vueThis.map.setFilter('matrix-heat',null);
    }
    
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
        vueThis.kdv.col_pixels = Math.round((vueThis.kdv.y_U - vueThis.kdv.y_L) * vueThis.kdv.row_pixels / (vueThis.kdv.x_U - vueThis.kdv.x_L))
    }
}

// function computeHeatForCell(xMin, yMin, xMax, yMax, geojsonData) {
//     let totalHeat = 0;
//     let count = 0;

//     // 遍历 GeoJSON 数据中的所有点
//     geojsonData.features.forEach(function (point) {
//         const coordinates = point.geometry.coordinates;
//         const dph = point.properties.dph;

//         // 检查点是否在当前网格内
//         if (coordinates[0] >= xMin && coordinates[0] <= xMax &&
//             coordinates[1] >= yMin && coordinates[1] <= yMax) {
//             // 将该点的 dph 值加到总热力值中
//             totalHeat += dph;
//             count++;
//         }
//     });

//     // 如果该网格内有点，返回平均 dph 作为热力值
//     return count > 0 ? totalHeat / count : 0; // 如果没有点，返回 0
// }


// 将wasm模块计算后的结果转成存储着经纬度和dph对象的数组，并对dph归一化到0~1
// 返回格式：[{longitude, latitude, dph, normalizedDph}]
function matrixDataProcess(data, kdv_type) {
    // Step 1: Split the data into lines
    const lines = data.trim().split('\n');
    let result = [];

    // Step 2: Process each line into an object with lat, long, and dph
    if (kdv_type == 1) {
        result = lines.map(line => {
            const [longitude, latitude, dph] = line.split(',').map(Number); // Convert to numbers
            return { longitude, latitude, dph };
        })
    } else if (kdv_type == 3) {
        result = lines.map(line => {
            const [longitude, latitude, time, dph] = line.split(',').map(Number); // Convert to numbers
            return { longitude, latitude, time, dph };
        })
    }

    // Step 3: Find the min and max DPH values
    const dphValues = result.map(item => item.dph);
    const minDph = dphValues.reduce((min, value) => value < min ? value : min, Infinity);
    const maxDph = dphValues.reduce((max, value) => value > max ? value : max, -Infinity);


    // Step 4: Normalize DPH values to the range [0, 1]
    result.forEach(item => {
        item.dph = (item.dph - minDph) / (maxDph - minDph); // Normalize DPH
    })
    return result;
}

// 根据row_pixels和col_pixels以及视窗的大小计算区块数据，返回geojson格式的matrix数据
function computeHeatMatrix(kdv, data) {
    const matrix = [];
    const gridWidth = (kdv.x_U - kdv.x_L) / kdv.row_pixels;
    const gridHeight = (kdv.y_U - kdv.y_L) / kdv.col_pixels;
    data.forEach(line => {
        const xMin = line.longitude - gridWidth / 2;
        const yMin = line.latitude - gridHeight / 2;
        const xMax = line.longitude + gridWidth / 2;
        const yMax = line.latitude + gridHeight / 2;

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
                heat_value: line.dph,
                time: line.time
            }
        });
    });
    return matrix;
}

// 数据点图层
function buildPointsmap(vueThis) {
    vueThis.map.addLayer(
        {
            id: 'trees-points',
            type: 'circle',
            source: 'points-source',
            minzoom: 18,
            paint: {
                'circle-radius': {
                    'property': 'dbh',
                    'type': 'exponential',
                    'stops': [
                        [{ zoom: 18, value: 1 }, 5],
                        [{ zoom: 18, value: 12 }, 10],
                        [{ zoom: 22, value: 1 }, 20],
                        [{ zoom: 22, value: 12 }, 50]
                    ]
                },
                'circle-color': {
                    'property': 'dbh',
                    'type': 'exponential',
                    'stops': [
                        [0, 'rgba(236,222,239,0)'],
                        [0.1, 'rgb(236,222,239)'],
                        [0.2, 'rgb(208,209,230)'],
                        [0.3, 'rgb(166,189,219)'],
                        [4, 'rgb(103,169,207)'],
                        [5, 'rgb(28,144,153)'],
                        [6, 'rgb(1,108,89)']
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

// 栅格化热力图图层
function buildMatrixHeatmap(vueThis, source) {
    vueThis.map.addLayer({
        id: 'matrix-heat',
        type: 'fill',
        source,
        paint: {
            // 填充颜色，基于每个矩形的热力值
            'fill-color': [
                'step',  // 使用step来创建离散的颜色
                ['get', 'heat_value'],  // 获取每个单元的热力值
                'rgba(0, 0, 0, 0)',  // 如果 heat_value 为 0，颜色为透明
                0.1, 'rgb(45, 59, 255)',
                0.2, 'rgb(39, 179, 243)',
                0.3, 'rgb(46, 226, 166)',
                0.4, 'rgb(179, 233, 109)',
                0.5, 'rgb(255, 240, 103)',
                0.6, 'rgb(255, 192, 57)',
                0.7, 'rgb(255, 127, 41)',
                0.8, 'rgb(255, 102, 64)',
                0.90, 'rgb(250, 74, 20)',
                0.95, 'rgb(211, 0, 0)',
                1.0, 'rgb(129, 0, 0)',
            ],
            // 设置透明度
            'fill-opacity': vueThis.kdv.opacity
        }
    });
}

// 自带的热力图图层，需要计算，延迟较大
function buildHeatmap(vueThis) {
    vueThis.map.addLayer({
        id: 'trees-heat', // 图层ID
        type: 'heatmap',  // 图层类型为热力图
        source: 'points-source', // 数据源
        maxzoom: 18,  // 最大缩放级别
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
                0.1, 'rgb(45, 59, 255)',
                0.2, 'rgb(39, 179, 243)',
                0.3, 'rgb(46, 226, 166)',
                0.4, 'rgb(179, 233, 109)',
                0.5, 'rgb(255, 240, 103)',
                0.6, 'rgb(255, 192, 57)',
                0.7, 'rgb(255, 127, 41)',
                0.8, 'rgb(255, 102, 64)',
                0.90, 'rgb(250, 74, 20)',
                0.95, 'rgb(211, 0, 0)',
                1.0, 'rgb(129, 0, 0)',
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

    let bandwidth = kdv.bandwidth_s * (kdv.x_U - kdv.x_L) / 0.111;//动态影响范围
    let tem = Module.compute(1, kdv.kdv_type, bandwidth, kdv.row_pixels, kdv.col_pixels, 1904, 12169, kdv.x_L, kdv.x_U,
        kdv.y_L, kdv.y_U, kdv.t_L, kdv.t_U,
        kdv.t_pixels, kdv.bandwidth_t);
    // console.log(tem)
    return tem

    // return Module.compute(3, 1, 0.01, kdv.row_pixels, kdv.col_pixels,
    //     1904, 12169, kdv.x_L, kdv.x_U, kdv.y_L, kdv.y_U, kdv.t_L, kdv.t_U, kdv.cur_time, kdv.bandwidth_t);

    // return Module.compute(1, 1, kdv.bandwidth_s, kdv.row_pixels, kdv.col_pixels,
    //     st, ed, kdv.x_L, kdv.x_U, kdv.y_L, kdv.y_U, kdv.t_L, kdv.t_U, kdv.cur_time, kdv.bandwidth_t);
}
function updateAtriibution(vueThis) {
    upadateMap(vueThis);
}

export default {
    loadHeatMap,
    updateAtriibution,
    updateCurrentTime,
}
