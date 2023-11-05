import utils from "./utils.js";

async function loadKSTC(vueThis){

    doLoad(
        vueThis,
        vueThis.KSTC.query.location.longitude,
        vueThis.KSTC.query.location.latitude,
        10
    );

}

async function paintPoints(vueThis,size){

    // build url
    var url = vueThis.baseUrl+'/kstc/geojson?'
        +'keywords='+vueThis.KSTC.query.keywords
        +'&lon='+vueThis.KSTC.query.location.longitude
        +'&lat='+vueThis.KSTC.query.location.latitude
        +'&k='+vueThis.KSTC.query.k
        +'&epsilon='+vueThis.KSTC.query.epsilon
        +'&minPts='+vueThis.KSTC.query.minPts
        +'&maxDist='+vueThis.KSTC.query.maxDist;

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
                filter: ['==', 'clusterId', "" + i],
                paint: {
                    'circle-radius': 3.5,
                    'circle-color': utils.getColor(i, size),
                    'circle-opacity': 0.7,
                },
            });
        }

        vueThis.map.on('click','points-source',(geoJson)=>{

            const coordinates = geoJson.features[0].geometry.coordinates.slice();

            let labels = geoJson.features[0].properties.labels;

            let str = "";
            for (let j = 0; j < labels.length; j++) {
                str+="<p>"+labels[j]+"</p>"
            }

            new mapboxgl.Popup()
                .setLngLat(coordinates)
                .setHTML(
                    "<strong>Labels</strong>"
                    +str
                )
                .addTo(vueThis.map);

        })


    });

}

async function paintMarker(vueThis,markers){

    for (let i = 0; i < markers.length; i++) {
        markers[i].addTo(vueThis.map);
    }

}

async function loadMarkers(vueThis){

    // build url
    var url = vueThis.baseUrl+'/kstc/markers?'
        +'keywords='+vueThis.KSTC.query.keywords
        +'&lon='+vueThis.KSTC.query.location.longitude
        +'&lat='+vueThis.KSTC.query.location.latitude
        +'&k='+vueThis.KSTC.query.k
        +'&epsilon='+vueThis.KSTC.query.epsilon
        +'&minPts='+vueThis.KSTC.query.minPts
        +'&maxDist='+vueThis.KSTC.query.maxDist;

    let markers = [];

    let res = await requestMarkers(url);

    for (let i = 0; i < res.data.length; i++) {
        let mrk = res.data[i];
        let color = utils.getColor(i);
        let marker = utils.getDefaultMark(mrk.coordinate.longitude, mrk.coordinate.latitude, color);

        marker.setPopup(
            utils.getPopUp(
                "<strong>clusterId "+mrk.clusterId+"</strong>" +
                "<p>pointNum: "+mrk.pointNum+"</p>" +
                "<p>description: "+mrk.description+"</p>")
        );

        markers.push(marker);
    }
    return markers;

}

async function requestMarkers(url){
    return axios({
        method: 'get',
        url: url
    });
}

async function doLoad(vueThis,lon,lat,zoom){

    let markers = await loadMarkers(vueThis);

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        //style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [lon, lat],
        zoom: zoom
    });

    let marker = utils.getDefaultMark(lon, lat, utils.getColor(0,1));
    marker.setPopup(utils.getPopUp("当前位置"));
    marker.addTo(vueThis.map);

    await paintPoints(vueThis,markers.length);

    await paintMarker(vueThis,markers);

}


async function loadExample01(vueThis){
    vueThis.KSTC.query.keywords='Water';
    vueThis.KSTC.query.location.longitude=-75.1;
    vueThis.KSTC.query.location.latitude=39.9;
    vueThis.KSTC.query.k=20;
    vueThis.KSTC.query.epsilon=10000;
    vueThis.KSTC.query.minPts=3;

    await doLoad(
        vueThis,
        -75.1,
        39.9,
        10
    );


}
async function loadExample02(vueThis){

    vueThis.KSTC.query.keywords='Restaurants';
    vueThis.KSTC.query.location.longitude=-75.1;
    vueThis.KSTC.query.location.latitude=39.9;
    vueThis.KSTC.query.k=20;
    vueThis.KSTC.query.epsilon=100;
    vueThis.KSTC.query.minPts=10;

    await doLoad(
        vueThis,
        -75.1,
        39.9,
        10
    );

}
async function loadExample03(vueThis){
    vueThis.KSTC.query.keywords='Drugstores';
    vueThis.KSTC.query.location.longitude=-75.1;
    vueThis.KSTC.query.location.latitude=39.9;
    vueThis.KSTC.query.k=60;
    vueThis.KSTC.query.epsilon=1000;
    vueThis.KSTC.query.minPts=4;
    await doLoad(
        vueThis,
        -75.1,
        39.9,
        10
    );
}
async function loadExample04(vueThis){
    vueThis.KSTC.query.keywords='Food';
    vueThis.KSTC.query.location.longitude=-75.1;
    vueThis.KSTC.query.location.latitude=39.9;
    vueThis.KSTC.query.k=20;
    vueThis.KSTC.query.epsilon=100;
    vueThis.KSTC.query.minPts=5;
    await doLoad(
        vueThis,
        -75.1,
        39.9,
        10
    );
}

async function popupTest(vueThis){

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        //style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [-77.04, 38.907],
        zoom: 11.15
    });




}

export default {
    loadKSTC,
    loadExample01,
    loadExample02,
    loadExample03,
    loadExample04,
    popupTest
}