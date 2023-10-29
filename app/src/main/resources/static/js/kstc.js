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

async function loadKSTC(vueThis){

    var kstc = vueThis.KSTC;

    prepareParams(kstc);

    let res = await getKstcCluster(kstc.query);

    vueThis.KSTC.clusters=res.data;
    vueThis.KSTC.maxClusterNums=res.data.length;
    vueThis.KSTC.clusterNums=Math.round(res.data.length/2);


    // load points

    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        // style: 'mapbox://styles/mapbox/light-v11',
        // style: 'mapbox://styles/mapbox/streets-v12',
        style: 'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey=' + vueThis.API_TOKEN,
        center: [kstc.query.location.longitude, kstc.query.location.latitude],
        zoom: 13
    });

    vueThis.map.on('load', function () {

        vueThis.map.addSource('points-source', {
            type: 'geojson',
            data: vueThis.baseUrl+'/kstc/geojson'
        });

        for (let i = 0; i < vueThis.KSTC.clusterNums; ++i) {
            vueThis.map.addLayer({
                id: 'layer' + i,
                type: 'circle',
                source: 'points-source',
                filter: ['==', 'clusterId', "" + i],
                paint: {
                    'circle-radius': 3.5,
                    'circle-color': utils.getColor(i, vueThis.KSTC.clusterNums),
                    'circle-opacity': 0.7,
                },
            });
        }
        vueThis.KSTC.layerLoaded = vueThis.KSTC.clusterNums;
    });


    // vueThis.map.setCenter([kstc.query.location.longitude,kstc.query.location.latitude]);

    // let markers = [];
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