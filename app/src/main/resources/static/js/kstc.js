import utils from "./utils.js";

async function loadKSTC(vueThis){
    vueThis.KSTC.loading=true;
    vueThis.KSTC.timeout=false;
    doLoad(
        vueThis,
        vueThis.KSTC.query.location.longitude,
        vueThis.KSTC.query.location.latitude,
        10
    );
    setTimeout(()=>{

        if(vueThis.KSTC.loading){
            vueThis.KSTC.loading=false;
            vueThis.KSTC.timeout=true;
            alert("30 second Time Out! Please try again later or modify parameters!")
        }

    },30000)

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
                    'circle-radius': 5,
                    'circle-color': utils.getColor(i, size),
                    'circle-opacity': 0.7,
                },
            });
            layerPopup(i,vueThis);
        }
    });

}

function layerPopup(i, vueThis){
    vueThis.map.on('click', 'layer' + i, function (e) {
        let coordinates = e.features[0].geometry.coordinates.slice();
        let labels = JSON.parse(e.features[0].properties.labels);
        var strings = vueThis.KSTC.query.keywords.split(",").map(str=>str.toLowerCase());

        let str = "<ul>";
        for (let j = 0; j < labels.length; j++) {
            if(strings.includes(labels[j].toLowerCase())){
                str+="<li><font size='2' color='red'>"+labels[j]+"</font></li>";
            }else{
                str+="<li><font size='2' color='black'>"+labels[j]+"</font></li>";
            }
        }
        str+="</ul>"
        utils.getPopUp(
            "<strong><font size='4' color='black'>Labels: </font></strong>"
            +str,
            false
        ).
        setLngLat(coordinates)
            .addTo(vueThis.map);

    });
    vueThis.map.on('mouseenter', 'layer' + i, () => {
        vueThis.map.getCanvas().style.cursor = 'pointer';
    });
    vueThis.map.on('mouseleave', 'layer' + i, () => {
        vueThis.map.getCanvas().style.cursor = '';
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

    if(vueThis.KSTC.timeout){
        return;
    }

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

    await paintPoints(vueThis,markers.length);

    await paintMarker(vueThis,markers);

    vueThis.KSTC.loading=false;
}

export default {
    loadKSTC
}