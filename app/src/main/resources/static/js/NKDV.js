import createModule from '/js/nkdvWasm/nkdvCpp.js';

let nkdvModule = await createModule({
  locateFile: (path, prefix) => {
    if (path.endsWith('.data')) {
      return '/js/nkdvWasm/nkdvCpp.data'; // 或根据变量动态返回路径
    }
    return prefix + path;
  }
});

async function compute_nkdv(lixel,bandwidth) { 
    console.log(lixel)
    nkdvModule._load_parameters(3,lixel,2,bandwidth); 
    const response = await fetch('/data/NKDV/edges_geometry.csv');
    if (!response.ok) throw new Error('网络响应错误');
    const text = await response.text();
    let csvText = text;
    // console.log("load network");
    let data =  nkdvModule.UTF8ToString(nkdvModule._compute())
    // console.log("compute data:"+data)

    const edgeCoords = parseCsv(csvText);
    const lixelData = parseOutputText(data);
    const geojson = buildLineGeoJson(edgeCoords, lixelData,lixel);
    // const geojson = buildGeoJson(edgeCoords, lixelData);

    // 查看结果
    // console.log(JSON.stringify(geojson, null, 2));
    return geojson;
}
async function loadHeatMap(vueThis) {
    
    return new Promise((resolve, reject) => {
        vueThis.map = new mapboxgl.Map({
            container: 'map', // container id
            style: vueThis.mapStyle,
            center: [-122.4194, 37.7749], // 修改为旧金山的经纬度
            zoom: 13  // 调整缩放级别以更好地显示城市
        });
        vueThis.map.on('load', async function () {
            nkdvModule._load_network()
            let geojson = await compute_nkdv(vueThis.NKDV.lixel,vueThis.NKDV.bandwidth)
            vueThis.map.addSource('nkdv', {
                type: 'geojson',
                data: geojson
            });

            // 根据 density 渐变颜色渲染
            // vueThis.map.addLayer({
            //     id: 'nkdv-points',
            //     type: 'circle',
            //     source: 'nkdv',
            //     paint: {
            //         'circle-radius': 5,  // 点的大小
            //         'circle-color': [
            //             'interpolate',
            //             ['linear'],
            //             ['get', 'density'],
            //             0, '#0000ff',    // 深蓝色 - 最低密度
            //             0.2, '#00ffff',  // 青色
            //             0.4, '#00ff00',  // 绿色
            //             0.6, '#ffff00',  // 黄色
            //             0.8, '#ff8c00',  // 橙色
            //             1.0, '#ff0000'   // 红色 - 最高密度
            //         ],
            //         'circle-opacity': vueThis.NKDV.opacity
            //     }
            // });
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

async function upadateMap(vueThis) {
    let geojson = await compute_nkdv(vueThis.NKDV.lixel,vueThis.NKDV.bandwidth)
    vueThis.map.getSource('nkdv').setData(geojson);
}

export default {
    loadHeatMap,
    upadateMap
}

function parseCsv(csvText) {
    const lines = csvText.trim().split('\n');
    const edgeCoords = {};
    for (let i = 1; i < lines.length; i++) { // 跳过标题行
        const [edgeIndex, n1_lon, n1_lat, n2_lon, n2_lat] = lines[i].split(',');
        edgeCoords[parseInt(edgeIndex)] = [
            [parseFloat(n1_lon), parseFloat(n1_lat)],
            [parseFloat(n2_lon), parseFloat(n2_lat)],
        ];
    }
    return edgeCoords;
}

function parseOutputText(outputText) {
    const lines = outputText.trim().split('\n');
    const numLixels = parseInt(lines[0]);
    const lixelData = [];

    for (let i = 1; i <= numLixels; i++) {
        const [edgeIndex, dist_n1, dist_n2, kde_value] = lines[i].split(/\s+/).map(Number);
        lixelData.push([edgeIndex, dist_n1, dist_n2, kde_value]);
    }
    return lixelData;
}

function calculateLixelCoords(edgeCoords, edgeIndex, dist_n1, dist_n2) {
    const [n1, n2] = edgeCoords[edgeIndex];
    const ratio = dist_n1 / (dist_n1 + dist_n2);
    const lon = n1[0] + ratio * (n2[0] - n1[0]);
    const lat = n1[1] + ratio * (n2[1] - n1[1]);
    return [lon, lat];
}

function normalizeKdeValues(values, method = 'minmax') {
    // 归一化前：将小于 0 的 KDE 值设为 0
    const clippedValues = values.map(v => Math.max(v, 0));

    const arr = Float64Array.from(clippedValues);
    let normalized;

    if (method === 'sum') {
        const total = arr.reduce((a, b) => a + b, 0);
        normalized = total > 0 ? arr.map(v => v / total) : arr;
    } else if (method === 'minmax') {
        const min = Math.min(...arr);
        const max = Math.max(...arr);
        normalized = arr.map(v => (v - min) / (max - min + 1e-8));
    } else if (method === 'zscore') {
        const mean = arr.reduce((a, b) => a + b, 0) / arr.length;
        const std = Math.sqrt(arr.reduce((a, b) => a + (b - mean) ** 2, 0) / arr.length);
        normalized = arr.map(v => (v - mean) / (std + 1e-8));
    } else {
        throw new Error("Unsupported normalization method");
    }

    return normalized;
}

function buildGeoJson(edgeCoords, lixelData) {
    const kdeValues = lixelData.map(item => item[3]);
    const normKde = normalizeKdeValues(kdeValues, 'minmax');

    const features = lixelData.map(([edgeIndex, dist_n1, dist_n2], i) => {
        const [lon, lat] = calculateLixelCoords(edgeCoords, edgeIndex, dist_n1, dist_n2);
        return {
            type: "Feature",
            geometry: {
                type: "Point",
                coordinates: [lon, lat]
            },
            properties: {
                edge_index: edgeIndex,
                dist_n1,
                dist_n2,
                density: normKde[i]
            }
        };
    });

    return {
        type: "FeatureCollection",
        features
    };
}

function buildLineGeoJson(edgeCoords, lixelData,lixel_reg_length) {
    // 提取所有 KDE 值用于归一化
    const kdeValues = lixelData.map(item => item[3]);

    // 选择归一化方法：'sum', 'minmax', 'zscore'
    const normalizedValues = normalizeKdeValues(kdeValues, 'minmax');

    const features = [];

    // 按 edge_index 分组 lixel 数据
    const edgeLixels = {};
    lixelData.forEach((data, i) => {
        const [edgeIndex, distN1, distN2] = data;
        const kdeValue = normalizedValues[i];
        if (!edgeLixels[edgeIndex]) {
            edgeLixels[edgeIndex] = [];
        }
        edgeLixels[edgeIndex].push([distN1, distN2, kdeValue]);
    });

    for (const edgeIndex in edgeCoords) {
        const coords = edgeCoords[edgeIndex];
        const lixelList = edgeLixels[edgeIndex];

        if (!lixelList) {
            continue; // 跳过没有 lixel 数据的边
        }

        const [n1Coords, n2Coords] = coords;

        // 按 distN1 排序
        lixelList.sort((a, b) => a[0] - b[0]);

        let prevPoint = [...n1Coords]; // 初始起点为 n1

        lixelList.forEach((data) => {
            const [distN1, distN2, kdeValue] = data;
            const totalLength = distN1 + distN2;

            // 防止最后一段越界
            let ratio = (distN1 + lixel_reg_length/2) / totalLength;
            if (ratio > 1) {
                ratio = 1;
            }

            const currentPoint = [
                n1Coords[0] + ratio * (n2Coords[0] - n1Coords[0]),
                n1Coords[1] + ratio * (n2Coords[1] - n1Coords[1]),
            ];

            // 构建当前 lixel 对应的小段（上一个点 -> 当前点）
            const segment = {
                type: "Feature",
                geometry: {
                    type: "LineString",
                    coordinates: [prevPoint, currentPoint],
                },
                properties: {
                    edge_index: parseInt(edgeIndex),
                    density: parseFloat(kdeValue),
                },
            };
            features.push(segment);

            // 更新上一个点为当前点
            prevPoint = currentPoint;
        });
    }

    return {
        type: "FeatureCollection",
        features,
    };
}