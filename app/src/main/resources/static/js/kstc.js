import utils from "./utils.js";

async function loadKSTC(vueThis){
    vueThis.mapLoading=false;
    doLoad(
        vueThis,
        vueThis.KSTC.query.location.longitude,
        vueThis.KSTC.query.location.latitude
    );
    setTimeout(()=>{

        vueThis.mapLoading=false;

    },5000)

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
        +'&maxDist='+vueThis.KSTC.query.maxDist
        +'&command='+vueThis.KSTC.query.command;

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
                    'circle-color': utils.getNewColor(i),
                    'circle-opacity': 1,
                },
            });
            layerPopup(i,vueThis);
        }
    });

}

function layerPopup(i, vueThis){
    let color = utils.getNewColor(i);
    vueThis.map.on('click', 'layer' + i, function (e) {
        let coordinates = e.features[0].geometry.coordinates.slice();
        let labels = JSON.parse(e.features[0].properties.labels);
        var strings = vueThis.KSTC.lastKeywords;

        let str = "";
        for (let j = 0; j < labels.length; j++) {
            var flag=false;
            var label = labels[j].toLowerCase();
            for (let k = 0; k < strings.length; k++) {
                var string = strings[k].toLowerCase();
                if(label===string || label.indexOf(string)>=0){
                    flag=true;
                    break;
                }
            }
            if(flag){
                str+='<div class="popup-message" ><font color=#FFB458>'+labels[j]+'</font></div>';
            }else{
                str+='<div class="popup-message">'+labels[j]+'</div>';
            }
        }

        utils.getNewPopUp(
            "<strong>"+e.features[0].properties.name+"</strong>",
            str,
            false
        ).
        setLngLat(coordinates)
            .addTo(vueThis.map);
    });

    vueThis.map.on('mouseenter', 'layer' + i, () => {
        vueThis.map.removeLayer('layer' + i);
        vueThis.map.addLayer({
            id: 'layer' + i,
            type: 'circle',
            source: 'points-source',
            filter: ['==', 'clusterId', "" + i],
            paint: {
                'circle-radius': 7,
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
            filter: ['==', 'clusterId', "" + i],
            paint: {
                'circle-radius': 5,
                'circle-color': color,
                'circle-opacity': 0.5,
            },
        });
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
        let color = utils.getColor(i,res.data.length);
        let marker = utils.getCustomMark(mrk.coordinate.longitude, mrk.coordinate.latitude, i);

        marker.setPopup(
            utils.getNewPopUp(
                "<strong>clusterId "+mrk.clusterId+"</strong>" +
                "<p>Result: "+mrk.pointNum+"</p>" +
                "<p>description: "+mrk.description+"</p>","",false)
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


async function requestZoom(url){
    return axios({
        method: 'get',
        url: url
    });
}
async function getZoom(vueThis){

    // build url
    var url = vueThis.baseUrl+'/kstc/dataCoordinateRange?'
        +'keywords='+vueThis.KSTC.query.keywords
        +'&lon='+vueThis.KSTC.query.location.longitude
        +'&lat='+vueThis.KSTC.query.location.latitude
        +'&k='+vueThis.KSTC.query.k
        +'&epsilon='+vueThis.KSTC.query.epsilon
        +'&minPts='+vueThis.KSTC.query.minPts
        +'&maxDist='+vueThis.KSTC.query.maxDist
        +'&command='+vueThis.KSTC.query.command;


    let res = await requestZoom(url);
    console.log(res);
    if(res.data.code !== 2000){
        alert(res.data.message);
        return {succeed: false};
    }

    // 2274.5034  14
    // 165.5796 17
    // k * 2274.5034 + c = 14
    // k * 165.5796 + c = 18
    var k = (18-15)/(165.5796-2274.5034);
    var c = 14 - k * 2274.5034 + 0.5;
    var zoom = Math.ceil(k * res.data.data.hypotenuseLength + c);
    if(zoom < 11){
        zoom = 11;
    }
    if(zoom > 18){
        zoom = 18;
    }
    console.log("number: "+zoom);
    return {succeed:true,zoom: zoom ,clusterSize:res.data.data.clusterNumber,lons:res.data.data.midLongitude,lats:res.data.data.midLatitude};

}

async function doLoad(vueThis,lon,lat){

    let zoomData = await getZoom(vueThis);
    if(!zoomData.succeed){
        return;
    }


    vueThis.KSTC.lastKeywords=vueThis.KSTC.query.keywords.split(";");
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: vueThis.mapStyle,
        center: [zoomData.lons, zoomData.lats],
        zoom: zoomData.zoom
    });
    vueThis.map.doubleClickZoom.disable();
    console.log("current position: "+lon+","+lat);
    let marker = utils.currentPosition(lon, lat);
    marker.setPopup(utils.getNewPopUp("Query",false));
    marker.addTo(vueThis.map);
    vueThis.KSTC.curMarker=marker;

    vueThis.map.on('dblclick',(e) => {
        console.log(`A click event has occurred at ${e.lngLat}`);
        if(vueThis.KSTC.curMarker != null){
            vueThis.KSTC.curMarker.remove();
        }
        vueThis.KSTC.query.location.longitude=e.lngLat.lng;
        vueThis.KSTC.query.location.latitude=e.lngLat.lat;

        let marker = utils.currentPosition(e.lngLat.lng, e.lngLat.lat);
        marker.setPopup(utils.getPopUp("Query",false));
        vueThis.KSTC.curMarker=marker;
        marker.addTo(vueThis.map);
    });
    await paintPoints(vueThis,zoomData.clusterSize);

}

async function searchKeywords(vueThis){
    // build url
    var url = vueThis.baseUrl+'/kstc/keywords?'
        +'keywords='+vueThis.KSTC.query.keywords;

    let  res = await axios({
        method: 'get',
        url: url
    });
    console.log(res.data);
    return res;
}

export default {
    loadKSTC,
    searchKeywords
}
