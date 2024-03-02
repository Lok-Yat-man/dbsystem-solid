import util from './utils.js'

let targetTrackingCalls = 0;
let targetTrackingCalls1 = 0;
let targetTrackingCalls2 = 0;
let targetTrackingCalls3 = 0;
var previous = null;
var current = null;
async function LoadtopK(vueThis, lon, la) {
  vueThis.map = new mapboxgl.Map({
    container: 'map', // container id
    style: 'mapbox://styles/mapbox/dark-v11',
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
  var marker1 = new mapboxgl.Marker({scale: 1})
    .setLngLat([lon, la])
    .addTo(vueThis.map)
  vueThis.map.on('load', () => {
    vueThis.map.addSource(sourceID, {
      'type': 'geojson',
      'data': 'data//geojson/uk.json'
    });
    vueThis.map.addLayer({
      'id': sourceID,
      'type': 'circle',
      'source': sourceID,
      'paint': {
        'circle-radius': 2.5,

        'circle-color': [
          'match',
          ['get', 'id'],
          '1',
          '#032885',
          '2',
          '#fbb03b',
          '3',
          '#223b53',
          '4',
          '#e55e5e',
          '#032885'
        ],
        'circle-opacity': 0.7,
      }
    });
  });

}

async function LoadResult(vueThis, id, lon, la, finds, key) {

    let inputKeywordAsString = key.split(",");
    let array = [];
    let point_key= [];
    //遍历所有有关键字的点，将点上的关键字聚合起来，不重复显示结点
    for(let i=0;i<finds.length;i++) {
      //关键字在根节点上，将关键字添加到array里
      if(finds[i] === id) {
        array.push(inputKeywordAsString[i]);
      } else {
        let temp = [];
        let isPut = false;
        //找到相同的点则添加到该点的关键字里
        for(let j=0;j<point_key.length;j++) {
          if(point_key.at(j).s_id === finds[i]) {
            point_key.at(j).s_key.push(inputKeywordAsString[i]);
            isPut = true;
            break;
          }
        }
        //没有找到则新添加该点和关键字
        if(isPut === false) {
          temp.push(inputKeywordAsString[i]);
          point_key.push({s_id: finds[i], s_key: temp});
        }

      }
    }

    let mhtml;
    if(array.length > 0) {
      mhtml = '<div class="pointClick_topk_hasKey">' + "id: " + id + '<br>' + "keyword: " + array +'</div>';
    } else {
      mhtml = '<div class="pointClick_topk_noKey">' + "id: " + id + '</div>';
    }

    let popup = new mapboxgl.Popup({ closeButton: false})
      .setHTML(mhtml)
    const marker1 = new mapboxgl.Marker({ scale: 0.8, color: '#e55e5e'}) /**使用draggable可以拖动顶点**/
      .setLngLat([lon, la])
      .setPopup(popup)
      .addTo(vueThis.map)


    //绘制除根节点外其他带关键字的顶点，悬浮在根节点附近，沿圆分布
    let x = -0.0015;
    let y = 0.0;
    let turn = true;
    let sourceD = [];
    let sourceDK = [];
    for(let i=0;i<point_key.length;i++) {
      if(turn === true)
        y = 1.0 * Math.sqrt(0.003 * 0.003 - x * x).toFixed(10);
      else
        y = -1.0 * Math.sqrt(0.003 * 0.003 - x * x).toFixed(10);
      const x1 = lon + x;
      const y1 = la + y;
      let mhtml1 = '<div class="pointClick_topk_hasKey">' + "id: " + point_key.at(i).s_id + '<br>' + "keyword: " + point_key.at(i).s_key +'</div>';
      let popup1 = new mapboxgl.Popup({ closeButton: false})
        .setHTML(mhtml1)
      const marker2 = new mapboxgl.Marker({ scale: 0.5, type: 'circle', color: '#fbb03b'})
        .setLngLat([x1, y1])
        .setPopup(popup1)
        .addTo(vueThis.map)

      //画线
      sourceD.push("traced" + ++targetTrackingCalls1)
      sourceDK.push("tracek" + ++targetTrackingCalls2);
      vueThis.map.on('load', () => {
        vueThis.map.addSource(sourceD.at(i), {
          type: 'geojson',
          data: {
            type: "FeatureCollection",
            features: [{
              type: "Feature",
              geometry: {
                type: 'LineString',
                coordinates: [
                  [lon, la],
                  [x1, y1]
                ],
              }
            }]
          }
        })
        // 增加线条
        vueThis.map.addLayer({
          id: sourceDK.at(i),
          type: "line",
          source: sourceD.at(i),
          paint: {
          "line-width": 3, // 线条宽度
           "line-opacity": 1, // 线条透明度
            "line-color": '#fbb03b', // 线条颜色
        }
      });

      })

      //修改xy值
      if(turn === true)
        x += 0.0008;
      else
        x -= 0.0008;
      if(x >= 0.003)
      {
        x -= 0.003;
        if(turn === true)
          turn = false;
        else
          turn = true;
      } else if(x <= -0.003) {
        x += 0.003;
        if(turn === true)
          turn = false;
        else
          turn = true;
      }
    }

}

async function PostTopK(vueThis, lon, la, key, k) {
  //判断输入合法性
  if(isNaN(lon) || isNaN(la) || key.length < 1 || isNaN(k) ) {
    alert("Please input correct parameters!");
    return;
  }

   axios.get('/hello', {
   params: {
     lon_topk: lon,
     la_topk: la,
     key_topk: key,
     k_topk: k
   }
  })
    .then(result => {
      LoadtopK(vueThis, lon, la); //显示查询坐标点和地图
      let array = [];
      for(let i=0;i<result.data.length;i++) {
        let temp = {return_id: result.data[i].id, return_lon: result.data[i].lon, return_la: result.data[i].la, return_finds: result.data[i].finds};
        array.push(temp);
        if(array.at(i).return_id !== -1) {
          LoadResult(vueThis, array.at(i).return_id, array.at(i).return_lon, array.at(i).return_la, array.at(i).return_finds, key);
        } else {
          alert("The input k is too large, the actual size of k is: " + array.at(i).return_finds);
        }
      }

      console.log(JSON.stringify(array));
    })
    .catch(error => {
      console.log('oooooo');
    })
}

export default {
    LoadtopK,
    PostTopK
}
