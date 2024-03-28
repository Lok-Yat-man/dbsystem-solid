import util from './utils.js'

let targetTrackingCalls = 0;
let targetTrackingCalls1 = 0;
let targetTrackingCalls2 = 0;
let targetTrackingCalls3 = 0;
let targetTrackingCalls4 = 0;
let targetTrackingCalls5 = 0;
let targetTrackingCalls6 = 0;
var previous = null;
var current = null;
async function LoadtopK_yago(vueThis, lon, la, key, k) {
  vueThis.map = new mapboxgl.Map({
    container: 'map', // container id
    style: vueThis.mapStyle,
    center: [lon, la],
    zoom: 8
  });
  // const marker1 = new mapboxgl.Marker({ scale: 0.5}) /**使用draggable可以拖动顶点**/
  //     .setLngLat([12.554729, 55.70651])
  //     .addTo(vueThis.map);
  // const marker2 = new mapboxgl.Marker({ color: 'pink', scale: 0.5 })
  //     .setLngLat([12.65147, 55.608166])
  //     .addTo(vueThis.map);

  const sourceID = "trace" + ++targetTrackingCalls;
  const el = document.createElement('div');
  el.id = 'top_k_marker';
  const marker1 = new mapboxgl.Marker(el,{scale: 1, draggable: true, backgroundImage: "img//point/point_blue1.png"})
      .setLngLat([lon, la])
      .addTo(vueThis.map)

  function onDragEnd() {
    const lngLat = marker1.getLngLat();
    console.log("Marker: Lng: " + lngLat.lng + " Lat: " + lngLat.lat);
    vueThis.topk_yago.query.longitude_yago_topk = lngLat.lng.toFixed(2);
    vueThis.topk_yago.query.latitude_yago_topk = lngLat.lat.toFixed(2);
  }

  marker1.on('dragend', onDragEnd);

  vueThis.map.on('load', e => {
    vueThis.map.addSource(sourceID, {
      'type': 'geojson',
      'data': 'data//geojson/yago.json'
    });
    vueThis.map.addLayer({
      'id': sourceID,
      'type': 'circle',
      'source': sourceID,
      'paint': {
        'circle-radius': 2.5,
        'circle-color': '#be6fe3',
        'circle-opacity': 0.7,
      }
    });
  });
}

//初始加载
async function StarLoadtopK_yago(vueThis, lon, la, key, k) {
  const sourceS = "traceS" + ++targetTrackingCalls3;
  const sourceSK = "traceSK" + ++targetTrackingCalls4;
  const sourceP = "traceP" + ++targetTrackingCalls5;
  const sourceL = "traceL" + ++targetTrackingCalls6;
  /***添加文字描述到每个点上面***/

}

export default {
    LoadtopK_yago,
    StarLoadtopK_yago,
}
