import utils from "./utils.js";

async function LoadBSTD(vueThis, longitude, latitude, keywords) {

    let markers = await loadObjectPoints(vueThis);

    vueThis.map = new mapboxgl.Map({
        container: 'map',
        style: vueThis.mapStyle,
        center: [longitude, latitude],
        zoom: 10
    });

    vueThis.map.doubleClickZoom.disable();
    console.log("current position: " + longitude + "," + latitude);
    let marker = utils.currentPosition(longitude, latitude);
    marker.setPopup(utils.getPopUp("当前位置", false));
    marker.addTo(vueThis.map);
    vueThis.spatial_skylines.curMarker = marker;

    vueThis.map.on('dblclick', (e) => {
        console.log(`A click event has occurred at ${e.lngLat}`);
        if (vueThis.spatial_skylines.curMarker != null) {
            vueThis.spatial_skylines.curMarker.remove();
        }
        vueThis.spatial_skylines.query.longitude = e.lngLat.lng;
        vueThis.spatial_skylines.query.latitude = e.lngLat.lat;

        let marker = utils.currentPosition(e.lngLat.lng, e.lngLat.lat);
        marker.setPopup(utils.getPopUp("当前位置", false));
        vueThis.spatial_skylines.curMarker = marker;
        marker.addTo(vueThis.map);
    });

    await paintPoints(vueThis, markers.length);

    await paintMarker(vueThis, markers);

}

async function loadObjectPoints(vueThis) {
    let url = vueThis.baseUrl + '/bstd/objectPoints?'
        + 'longitude=' + vueThis.spatial_skylines.query.longitude
        + '&latitude=' + vueThis.spatial_skylines.query.latitude
        + '&keywords=' + vueThis.spatial_skylines.query.keywords;

    let objectPoints = [];
    let res = requestObjectPoints(url);
    for (let i = 0; i < res.data.length; i++) {
        let objPoint = res.data[i];
        let color = utils.getColor(i, res.data.length);
        let marker = utils.getDefaultMark(objPoint.longitude, objPoint.latitude, color);

        /**
         * Todo
         */
        marker.setPopup(
            utils.getPopUp(
                "<strong>skylineId " + objPoint.objId + "</strong>"
            )
        );
        objectPoints.push(marker);
    }
    return objectPoints;
}

async function requestObjectPoints(url) {
    return axios({
        method: 'get',
        url: url
    });
}

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
            vueThis.map.addLayer({
                id: 'layer' + i,
                type: 'circle',
                source: 'points-source',
                filter: ['==', 'skylineId', "" + i],
                paint: {
                    'circle-radius': 5,
                    'circle-color': utils.getColor(i, size),
                    'circle-opacity': 0.7,
                },
            });
            //layerPopup(i, vueThis);
        }
    })
}

function layerPopup(i, vueThis) {
    vueThis.map.on('click', 'layer' + i, function (e) {
        let coordinates = e.features[0].geometry.coordinates.slice();
        let labels = JSON.parse(e.features[0].properties.labels);
        var strings = vueThis.spatial_skylines.lastKeywords;

        let str = "";
        for (let j = 0; j < labels.length; j++) {
            let flag = false;
            let label = labels[j].toLowerCase();
            for (let k = 0; k < strings.length; k++) {
                var string = strings[k].toLowerCase();
                if (label === string || label.indexOf(string) >= 0) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                str += "<div><strong><font size='2' color='red'>" + labels[j] + "</font></strong></div>";
            } else {
                str += "<div><font size='2' color='black'>" + labels[j] + "</font></div>";
            }
        }

        utils.getPopUp(
            "<div><font size='2' color='black'>" + e.features[0].properties.name + "</font></div>"
            + "<hr/>"
            + str,
            false
        ).setLngLat(coordinates)
            .addTo(vueThis.map);
    });
    vueThis.map.on('mouseenter', 'layer' + i, () => {
        vueThis.map.getCanvas().style.cursor = 'pointer';
    });
    vueThis.map.on('mouseleave', 'layer' + i, () => {
        vueThis.map.getCanvas().style.cursor = '';
    });
}

async function paintMarker(vueThis, markers) {
    for (let i = 0; i < markers.length; i++) {
        markers[i].addTo(vueThis.map);
    }
}

async function LoadBSTD2(vueThis, longitude, latitude) {
    vueThis.KSTC.loading=true;
    vueThis.KSTC.timeout=false;

    let path = vueThis.baseUrl + '/bstd/objectPoints?'
        + 'longitude=' + vueThis.spatial_skylines.query.longitude
        + '&latitude=' + vueThis.spatial_skylines.query.latitude
        + '&keywords=' + vueThis.spatial_skylines.query.keywords;
    let markers = [];
    let res = await requestObjectPoints(path);
    for (let i = 0; i < res.data.length; i++) {
        let mrk = res.data[i];
        let color = utils.getColor(i, res.data.length);
        let marker = utils.getDefaultMark(mrk.coordinate.longitude, mrk.coordinate.latitude, color);
        marker.setPopup(
            utils.getPopUp(
                "<strong>objectId " + mrk.objId + "</strong>"
            )
        );
        markers.push(marker);
    }
    vueThis.spatial_skylines.lastKeywords = vueThis.spatial_skylines.query.keywords.split(",");

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: vueThis.mapStyle,
        center: [longitude, latitude],
        zoom: 10
    });

    let currentLocationMarker = new mapboxgl.Marker({
        color: '#ff0505',
        scale: 1
    }).setLngLat([longitude, latitude]);
    currentLocationMarker.setPopup(utils.getPopUp("当前位置", false));
    currentLocationMarker.addTo(vueThis.map);

    await paintPoints(vueThis, markers.length);

    await paintMarker(vueThis, markers);

    vueThis.spatial_skylines.loading=false;

    // await axios({
    //     method: "get",
    //     url: path
    // });
    //
    // vueThis.map.on('load', () => {
    //     vueThis.map.addSource('points-source', {
    //         type: 'geojson',
    //         url: url
    //     });
    // });
    //
    // vueThis.map.addLayer({
    //     'type': 'circle',
    //     'paint': {
    //         'circle-radius': 3,
    //         'circle-color': '#e55e5e',
    //         'circle-opacity': 0.7
    //     }
    // });
}

export default {
    //LoadBSTD,
    LoadBSTD2
}