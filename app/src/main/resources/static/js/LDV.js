function loadHeatMap(vueThis) {
    return new Promise(async (resolve, reject) => {
        vueThis.map = new mapboxgl.Map({
            container: 'map',
            style: vueThis.mapStyle,
            center: [116.51135, 39.93883],
            zoom: 13
        });

        vueThis.map.on('load', async function () {
            // 1. 转换output_result_1为GeoJSON
            const geojson = await convertOutputToGeoJSON('/data/LDV/output_result_Los_Angeles');

            console.log("geojson", geojson);

            // 2. 添加数据源
            vueThis.map.addSource('ldv', {
                type: 'geojson',
                data: geojson
            });

            // 3. 添加点图层，按density渐变色
            vueThis.map.addLayer({
                id: 'ldv-points',
                type: 'circle',
                source: 'ldv',
                paint: {
                    'circle-radius': 4,
                    'circle-color': [
                        'interpolate',
                        ['linear'],
                        ['get', 'density'],
                        0, '#0000ff',
                        0.2, '#00ffff',
                        0.4, '#00ff00',
                        0.6, '#ffff00',
                        0.8, '#ff8c00',
                        1.0, '#ff0000'
                    ],
                    'circle-opacity': vueThis.LDV.opacity
                }
            });

            resolve();
        });
    });
}

function upadateMap(vueThis) {

    console.log("geojson")
    // vueThis.map.getSource('ldv').setData(`/data/NKDV/network_density_${vueThis.NKDV.bandwidth}.geojson`);

}

async function convertOutputToGeoJSON(url) {
    const response = await fetch(url);
    const text = await response.text();
    const lines = text.trim().split('\n');

    // 解析所有行，提取x, y, density
    const parsed = lines.map(line => {
        const [x, y, density] = line.trim().split(/\s+/).map(Number);
        return { x, y, density };
    });

    // 找x, y, density的最大最小值
    const xValues = parsed.map(p => p.x);
    const yValues = parsed.map(p => p.y);
    const densityValues = parsed.map(p => p.density);

    const minX = Math.min(...xValues);
    const maxX = Math.max(...xValues);
    const minY = Math.min(...yValues);
    const maxY = Math.max(...yValues);
    const minDensity = Math.min(...densityValues);
    const maxDensity = Math.max(...densityValues);

    const features = parsed.map(p => {
        // 归一化
        // const normX = maxX > minX ? (p.x - minX) / (maxX - minX) : 0;
        // const normY = maxY > minY ? (p.y - minY) / (maxY - minY) : 0;
        const normX = p.x*180/20037508.34
        const normY = p.y*180/20037508.34
        const normDensity = maxDensity > minDensity ? (p.density - minDensity) / (maxDensity - minDensity) : 0;

        return {
            type: "Feature",
            geometry: {
                type: "Point",
                coordinates: [normX, normY]  // 归一化坐标
            },
            properties: {
                density: normDensity
            }
        };
    });

    return {
        type: "FeatureCollection",
        features: features
    };
}


export default {
    loadHeatMap,
    upadateMap
}