function testLine(vueThis){
    vueThis.map = new mapboxgl.Map({
        container: 'map', // container id
        style: 'mapbox://styles/mapbox/light-v11',
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
                'line-width': 5,
                'line-color': '#ed6498'
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
        for(let i = 1;i<=1;++i){
            vueThis.map.addLayer({
                'id': 'poly' + i,
                'type': 'fill',
                'source': 'polygons',
                filter: ['==', 'index', i],
                'layout': {},
                'paint': {
                    'fill-color': '#ff3149',
                    'fill-opacity': 0.5
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
                    'line-color': '#0a1011',
                    'line-width': 2,
                    'line-opacity':0.5
                }
            });
        }
        console.log(vueThis.map.getSource('polygons'));
    });

}

export default {
    testLine,
    testPolygon
}