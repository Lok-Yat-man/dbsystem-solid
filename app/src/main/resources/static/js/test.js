import util from './utils.js'
function testTree(vueThis){
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: vueThis.mapStyle,
        center: [-97.7575966669, 30.2634181234],
        zoom: 5
    });
    vueThis.map.on('load', ()=> {
        vueThis.map.addSource('line', {
            'type': 'geojson',
            'data': 'data//geojson/lineStringExample.json'
        });
        vueThis.map.addLayer({
            'id': 'line-animation',
            'type': 'line',
            'source': 'line',
            'paint': {
                'line-width': 1,
                'line-color': '#a292f8'
            }
        });
        vueThis.map.addLayer({
            'id': 'line-point',
            'type': 'circle',
            'source': 'line',
            'paint': {
                'circle-radius': 4,
                'circle-color': '#032885',
                'circle-opacity': 0.7,
            }
        });
    });
}

function testPolygon(vueThis){
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: 'mapbox://styles/mapbox/light-v11',
        // center: [-68.137343, 45.137451],
        center: [113.54415036, 22.20735254],
        zoom: 9
    });
    vueThis.map.on('load', ()=> {
        vueThis.map.addSource('polygons', {
            'type': 'geojson',
            'data': 'js/kdv/kdvPolygon.json'
        });
        for(let i = 1;i<=3;++i){
            vueThis.map.addLayer({
                'id': 'poly' + i,
                'type': 'fill',
                'source': 'polygons',
                filter: ['==', 'index', i],
                'layout': {},
                'paint': {
                    'fill-color': util.getColor(i,20),
                    'fill-opacity': 0.1
                }
            });
            // 添加轮廓
            vueThis.map.addLayer({
                'id': 'outline' + i,
                'type': 'line',
                'source': 'polygons',
                filter: ['==', 'index', i],
                'layout': {},
                'paint': {
                    'line-color': util.getColor(i,20),
                    'line-width': 2,
                    'line-opacity':0.5
                }
            });
        }
        console.log(vueThis.map.getSource('polygons'));
    });

}

export default {
    testTree,
    testPolygon
}