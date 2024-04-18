import utils from "./utils.js";

async function LoadBSTD(vueThis) {
    vueThis.spatial_skylines.loading = true;
    vueThis.spatial_skylines.timeout = false;

    let longitude = vueThis.spatial_skylines.query.longitude;
    let latitude = vueThis.spatial_skylines.query.latitude;

    vueThis.spatial_skylines.query.keywords = vueThis
        .spatial_skylines.query.keywords.replace(/\s/g, "");
    vueThis.spatial_skylines.lastKeywords = vueThis.spatial_skylines.query.keywords.split(",");

    let objectData = await loadData(vueThis);

    paintMap(vueThis, longitude, latitude, objectData);

    vueThis.spatial_skylines.curmarker = paintCurrentLocation(vueThis, longitude, latitude, objectData);

    doubleClickCoordinate(vueThis);

    await paintPoints(vueThis, objectData.data.length);

    vueThis.spatial_skylines.loading = false;
}

// 异步加载后端运行的Skyline结果集
async function loadData(vueThis) {
    let path = vueThis.baseUrl + '/bstd/objectPoints?'
        + 'longitude=' + vueThis.spatial_skylines.query.longitude
        + '&latitude=' + vueThis.spatial_skylines.query.latitude
        + '&keywords=' + vueThis.spatial_skylines.query.keywords;
    let res = await requestObjectPoints(path);
    return res;
}

// get方法请求
async function requestObjectPoints(url) {
    return axios({
        method: 'get',
        url: url
    });
}

// 绘制地图
function paintMap(vueThis, longitude, latitude, objectData) {
    let minLng = 180.0, maxLng = -180.0, minLat = 90.0, maxLat = 0.0;
    for (const objectDatum of objectData.data) {
        if (objectDatum.coordinate.longitude < minLng) {
            minLng = objectDatum.coordinate.longitude;
        }
        if (objectDatum.coordinate.longitude > maxLng) {
            maxLng = objectDatum.coordinate.longitude;
        }
        if (objectDatum.coordinate.latitude < minLat) {
            minLat = objectDatum.coordinate.latitude;
        }
        if (objectDatum.coordinate.latitude > maxLat) {
            maxLat = objectDatum.coordinate.latitude;
        }
    }
    if (vueThis.spatial_skylines.query.longitude < minLng) {
        minLng = vueThis.spatial_skylines.query.longitude
    }
    if (vueThis.spatial_skylines.query.longitude > maxLng) {
        maxLng = vueThis.spatial_skylines.query.longitude;
    }
    if (vueThis.spatial_skylines.query.latitude < minLat) {
        minLat = vueThis.spatial_skylines.query.latitude;
    }
    if (vueThis.spatial_skylines.query.latitude > maxLat) {
        maxLat = vueThis.spatial_skylines.query.latitude;
    }

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: vueThis.mapStyle,
        center: [longitude, latitude],
        zoom: 17
    });

    vueThis.map.fitBounds([
        [minLng - 0.002, minLat - 0.002], // southwestern corner of the bounds
        [maxLng + 0.002, maxLat + 0.002] // northeastern corner of the bounds
    ]);
}

// 绘制当前位置标记
function paintCurrentLocation(vueThis, longitude, latitude, objectData) {
    // let currentLocationMarker = new mapboxgl.Marker({
    //     color: '#ff0505',
    //     scale: 1.4
    // }).setLngLat([longitude, latitude]);
    let currentLocationMarker = utils.getCustomMark(longitude, latitude, 1);

    currentLocationMarker.setPopup(utils.getNewPopUp(
        "<strong>当前位置</strong>",
        "pointNum: " + objectData.data.length,

        false));
    currentLocationMarker.addTo(vueThis.map);
    return currentLocationMarker;
}

function doubleClickCoordinate(vueThis) {
    vueThis.map.doubleClickZoom.disable();
    vueThis.map.on('dblclick', (e) => {
        if (vueThis.spatial_skylines.curmarker != null) {
            vueThis.spatial_skylines.curmarker.remove();
        }
        vueThis.spatial_skylines.query.longitude = e.lngLat.lng;
        vueThis.spatial_skylines.query.latitude = e.lngLat.lat;
        let marker = utils.getCustomMark(e.lngLat.lng, e.lngLat.lat, 1);
        marker.setPopup(utils.getNewPopUp(
            "<strong>当前位置</strong>",
            "",
            false
        ));
        vueThis.spatial_skylines.curmarker = marker;
        marker.addTo(vueThis.map);
    });
}

// 绘制Skyline结果点
async function paintPoints(vueThis, size) {
    let url = vueThis.baseUrl + '/bstd/geojson?'
        + 'longitude=' + vueThis.spatial_skylines.query.longitude
        + '&latitude=' + vueThis.spatial_skylines.query.latitude
        + '&keywords=' + vueThis.spatial_skylines.query.keywords;

    vueThis.map.on('load', function () {
        vueThis.map.addSource('points-source', {
            type: 'geojson',
            data: url
        });

        for (let i = 0; i < size; ++i) {
            let color = utils.getColor(i, size);
            vueThis.map.addLayer({
                id: 'layer' + i,
                type: 'circle',
                source: 'points-source',
                filter: ['==', 'skylineId', "" + i],
                paint: {
                    'circle-radius': 5.0,
                    'circle-color': color,
                    'circle-opacity': 0.7,
                },
            });
            layerPopup(i, vueThis, color);
        }
    });
}

// 添加功能：点击Skyline点可以展示详细信息
function layerPopup(i, vueThis, color) {
    vueThis.map.on('click', 'layer' + i, function (e) {
        let coordinates = e.features[0].geometry.coordinates.slice();
        let labels = JSON.parse(e.features[0].properties.labels);
        let strings = vueThis.spatial_skylines.lastKeywords;

        let str = "";
        for (let j = 0; j < labels.length; j++) {
            let flag = false;
            let label = labels[j].toLowerCase();
            for (let k = 0; k < strings.length; k++) {
                let string = strings[k].toLowerCase();
                if (label === string || (label.indexOf(string) >= 0 && label.endsWith("s"))) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                str += '<div class="popup-message"><font color=\'red\'>' + labels[j] + '</font></div>';
            } else {
                str += '<div class="popup-message">' + labels[j] + '</div>';
            }
        }

        utils.getNewPopUp(
            "<strong>" + e.features[0].properties.name + "</strong>",
            str,
            false
        ).setLngLat(coordinates)
            .addTo(vueThis.map);
    });

    // vueThis.map.on('mouseenter', 'layer' + i, () => {
    //     vueThis.map.getCanvas().style.cursor = 'pointer';
    // });
    // vueThis.map.on('mouseleave', 'layer' + i, () => {
    //     vueThis.map.getCanvas().style.cursor = '';
    // });
    vueThis.map.on('mouseenter', 'layer' + i, () => {
        vueThis.map.removeLayer('layer' + i);
        vueThis.map.addLayer({
            id: 'layer' + i,
            type: 'circle',
            source: 'points-source',
            filter: ['==', 'skylineId', "" + i],
            paint: {
                'circle-radius': 7.5,
                'circle-color': color,
                'circle-opacity': 0.7,
            },
        });
        vueThis.map.getCanvas().style.cursor = 'pointer';
    });
    vueThis.map.on('mouseleave', 'layer' + i, () => {
        vueThis.map.removeLayer('layer' + i);
        vueThis.map.addLayer({
            id: 'layer' + i,
            type: 'circle',
            source: 'points-source',
            filter: ['==', 'skylineId', "" + i],
            paint: {
                'circle-radius': 5.0,
                'circle-color': color,
                'circle-opacity': 0.7,
            },
        });
        vueThis.map.getCanvas().style.cursor = '';
    });
}

export default {
    LoadBSTD
}