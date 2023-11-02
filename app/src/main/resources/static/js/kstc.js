import utils from "./utils.js";

async function loadKSTC(vueThis){


    // build url
    var url = vueThis.baseUrl+'/kstc/geojson?'
        +'keywords='+vueThis.KSTC.query.keywords
        +'&lon='+vueThis.KSTC.query.location.longitude
        +'&lat='+vueThis.KSTC.query.location.latitude
        +'&k='+vueThis.KSTC.query.k
        +'&epsilon='+vueThis.KSTC.query.epsilon
        +'&minPts='+vueThis.KSTC.query.minPts;

    doLoad(
        vueThis,
        url,
        vueThis.KSTC.query.location.longitude,
        vueThis.KSTC.query.location.latitude,
        10
    );

}

async function paintPoints(vueThis,url,size){

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
    });

}

async function paintMarker(vueThis,markers){

    for (let i = 0; i < markers.length; i++) {
        markers[i].addTo(vueThis.map);
    }

}

function loadMarkers(vueThis){
    const clusters = vueThis.KSTC.clusters;
    var map = new Map();
    for (let i = 0; i < clusters.length; ++i) {
        const clusterId = clusters[i].properties.clusterId;
        if(map.has(clusterId)){
            map.get(clusterId).push(clusters[i]);
        }else{
            map.set(clusterId,[clusters[i]]);
        }

    }
    let markers = [];
    var idx = 0;
    map.forEach(
        (cls,v)=>{
            let i = cls.length>>1;
            let cl = cls[i];
            let color = utils.getColor(idx);
            let location = cl.geometry.coordinates;
            let marker = utils.getDefaultMark(location[0], location[1], color);
            marker.setPopup(utils.getPopUp("cluster " + cl.properties.clusterId),false);
            markers.push(marker);
            idx++;
        }
    )
    return markers;

}

async function getCluster(url){
    return axios({
        method: 'get',
        url: url
    });
}

async function doLoad(vueThis,url,lon,lat,zoom){

    let res = await getCluster(url);
    vueThis.KSTC.clusters = res.data.features;

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        //style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [lon, lat],
        zoom: zoom
    });
    let marker = utils.getDefaultMark(lon, lat, utils.getColor(0,1));
    marker.setPopup(utils.getPopUp("当前位置",false));
    marker.addTo(vueThis.map);

    let markers = loadMarkers(vueThis);

    await paintPoints(vueThis,url,markers.length);

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
        vueThis.baseUrl+'/kstc/example01',
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
        vueThis.baseUrl+'/kstc/example02',
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
        vueThis.baseUrl+'/kstc/example03',
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
        vueThis.baseUrl+'/kstc/example04',
        -75.1,
        39.9,
        10
    );
}



export default {
    loadKSTC,
    loadExample01,
    loadExample02,
    loadExample03,
    loadExample04
}