import utils from "./utils.js";

async function loadBSTD(vueThis, longitude, latitude, keywords) {
    vueThis.spatial_skylines.loading = true;
    vueThis.spatial_skylines.timeout = false;

    let markers = await loadMarkers(vueThis);

    vueThis.spatial_skylines.lastKeywords = vueThis.spatial_skylines.query.keywords.split(",");
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        mapStyle: vueThis.mapStyle,
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [longitude, latitude],
        zoom: 3
    });

    console.log("current position: " + longitude + "," + latitude);
    let marker = utils.currentPosition(longitude, latitude);
    marker.setPopup(utils.getPopUp("当前位置", false));
    marker.addTo(vueThis.map);
    //vueThis.KSTC.curMarker = marker;

    vueThis.map.on('dblclick', (e) => {
        console.log(`A click event has occurred at ${e.lngLat}`);
        if (vueThis.KSTC.curMarker != null) {
            vueThis.KSTC.curMarker.remove();
        }
        vueThis.KSTC.query.location.longitude = e.lngLat.lng;
        vueThis.KSTC.query.location.latitude = e.lngLat.lat;

        let marker = utils.currentPosition(e.lngLat.lng, e.lngLat.lat);
        marker.setPopup(utils.getPopUp("当前位置", false));
        vueThis.KSTC.curMarker = marker;
        marker.addTo(vueThis.map);
    });

    await paintPoints(vueThis, markers.length);

    await paintMarker(vueThis, markers);

    vueThis.KSTC.loading = false;
}

export default {
    loadBSTD
}