import utils from "./utils.js";

function prepareParams(kstc){

    kstc.query.keywords=kstc.message.split(",");
}

async function getKstcCluster(query){
    return axios({
        method: 'post',
        url: '/kstc/geojson',
        data:{...query}
    });
}


async function loadKSTC(vueThis,eg){




    // build url
    var url = vueThis.baseUrl+'/kstc/geojson?'
        +'keywords='+vueThis.KSTC.query.keywords
        +'&lon='+vueThis.KSTC.query.location.longitude
        +'&lat='+vueThis.KSTC.query.location.latitude
        +'&k='+vueThis.KSTC.query.k
        +'&epsilon='+vueThis.KSTC.query.epsilon
        +'&minPts='+vueThis.KSTC.query.minPts;


    var  res = await axios({
        method: 'get',
        'url': url
    })
    vueThis.KSTC.clusters=res.data;
    vueThis.KSTC.maxClusterNums=res.data.length;

    // load points

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        // style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [-119.71074676513672, 34.42033386230469],
        zoom: 13
    });


    vueThis.map.on('load', function () {

        vueThis.map.addSource('points-source', {
            type: 'geojson',
            data: url
        });

        for (let i = 0; i < vueThis.KSTC.maxClusterNums; ++i) {
            vueThis.map.addLayer({
                id: 'layer' + i,
                type: 'circle',
                source: 'points-source',
                filter: ['==', 'clusterId', "" + i],
                paint: {
                    'circle-radius': 3.5,
                    'circle-color': utils.getColor(i, vueThis.KSTC.maxClusterNums),
                    'circle-opacity': 0.7,
                },
            });
        }
        vueThis.KSTC.layerLoaded = vueThis.KSTC.clusterNums;
    });

    // let marker = utils.getDefaultMark(kstc.query.location.longitude, kstc.query.location.latitude, color);
    // markers.push(marker);
    //
    // marker.addTo(vueThis.map);
    //
    // vueThis.map.setCenter([kstc.query.location.longitude,kstc.query.location.latitude]);

    // let markers = [];
    //
    // for(let i=0;i<vueThis.KSTC.maxClusterNums;++i){
    //     let clusterId = vueThis.KSTC.clusters[i].clusterId;
    //     let color = utils.getColor(clusterId, vueThis.KSTC.maxClusterNums);
    //     let locations = vueThis.KSTC.clusters[i].members;
    //     let checkIn = locations[0].coordinate;
    //     let marker = utils.getDefaultMark(checkIn.longitude, checkIn.latitude, color);
    //     markers.push(marker);
    //
    //     marker.addTo(vueThis.map);
    //
    // }



}




export default {
    loadKSTC
}