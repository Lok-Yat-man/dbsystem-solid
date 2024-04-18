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
async function LoadtopK(vueThis, lon, la, key, k) {
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
    vueThis.topk.query.longitude_topk = lngLat.lng.toFixed(2);
    vueThis.topk.query.latitude_topk = lngLat.lat.toFixed(2);
  }

  marker1.on('dragend', onDragEnd);

  //不用画可以查询的点
  // vueThis.map.on('load', e => {
  //   vueThis.map.addSource(sourceID, {
  //     'type': 'geojson',
  //     'data': 'data//geojson/uk.json'
  //   });
  //   vueThis.map.addLayer({
  //     'id': sourceID,
  //     'type': 'circle',
  //     'source': sourceID,
  //     'paint': {
  //       'circle-radius': 2.5,
  //       'circle-color': '#be6fe3',
  //       'circle-opacity': 0.7,
  //     }
  //   });
  // });
}

//初始加载
async function StarLoadtopK(vueThis, lon, la, key, k) {
  const sourceS = "traceS" + ++targetTrackingCalls3;
  const sourceSK = "traceSK" + ++targetTrackingCalls4;
  const sourceP = "traceP" + ++targetTrackingCalls5;
  const sourceL = "traceL" + ++targetTrackingCalls6;
  /***添加文字描述到每个点上面***/
  vueThis.map.on('load', () => {
    vueThis.map.loadImage(
      'img//point/point_2.png',
      (error, image) => {
        if (error) throw error;
        vueThis.map.addImage('custom-marker', image);
        //根节点
        vueThis.map.addSource(sourceS, {
          'type': 'geojson',
          'data': 'data//geojson/topk.json'
        });

        //画根节点
        vueThis.map.addLayer({
          'id': sourceS,
          'type': 'symbol',
          'source': sourceS,
          'layout': {
            'icon-image': 'custom-marker'
          }
        });
      }
    )

    //显示根节点的信息
    vueThis.map.on('click', sourceS, (e) => {
      const description = e.features[0].properties.description;
      const coordinates = e.features[0].geometry.coordinates.slice();
      // while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
      //   coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
      // }
      new mapboxgl.Popup()
        .setLngLat(coordinates)
        .setHTML(description)
        .addTo(vueThis.map)
    })

    vueThis.map.loadImage(
      'img//point/point_7.png',
      (error, image1) => {
        if (error) throw error;
        vueThis.map.addImage('custom-marker1', image1);
        //叶子节点
        vueThis.map.addSource(sourceP, {
          'type': 'geojson',
          'data': 'data//geojson/topk1.json'
        });

        //画叶子节点
        vueThis.map.addLayer({
          'id': sourceP,
          'type': 'symbol',
          'source': sourceP,
          'layout': {
            'icon-image': 'custom-marker1'
          }
        });
      }
    )

    //显示叶子节点的信息
    vueThis.map.on('click', sourceP, (e) => {
      const description = e.features[0].properties.description;
      const coordinates = e.features[0].geometry.coordinates.slice();
      // while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
      //   coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
      // }
      new mapboxgl.Popup()
        .setLngLat(coordinates)
        .setHTML(description)
        .addTo(vueThis.map)
    })

    //画线
    vueThis.map.addSource(sourceL, {
      type: 'geojson',
      data: {
        type: "FeatureCollection",
        features: [
          { type: "Feature",
            geometry: {
            type: 'LineString',
            coordinates: [[-3.3095230000000004, 53.117003000000004], [-3.609, 53.317]]
            }
          },
          { type: "Feature",
            geometry: {
              type: 'LineString',
              coordinates: [[-3.3095230000000004, 53.117003000000004], [-3.009, 53.317]]
            }
          },
          { type: "Feature",
            geometry: {
              type: 'LineString',
              coordinates: [[-3.054, 52.86], [-3.354, 52.66]]
            }
          },
          { type: "Feature",
            geometry: {
              type: 'LineString',
              coordinates: [[-3.054, 52.86], [-2.754, 52.66]]
            }
          },
          { type: "Feature",
            geometry: {
              type: 'LineString',
              coordinates: [[-2.9986, 53.0474], [-2.6986, 53.1974]]
            }
          },
          { type: "Feature",
            geometry: {
              type: 'LineString',
              coordinates: [[-2.9986, 53.0474], [-2.6986, 52.8974]]
            }
          }
        ]
      }
    })
    //画线
    vueThis.map.addLayer({
      'id': sourceL,
      'type': 'line',
      'source': sourceL,
      'paint': {
        "line-color": '#00FDFF', // 线条颜色
        "line-width": 1, // 线条宽度
        "line-opacity": 0.5, // 线条透明度
      }

      });



  })
}

async function LoadResult(vueThis, id, lon, la, finds, key) {

    /**关键字之间以空格分隔**/
    let inputKeywordAsString = key.split(" ");
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
      mhtml = '<div class="popup-window-topk-haskey"><div class=\"popup-title\">' + id + '<img src=\"./img/popup/bar-nameBar.png\" class=\"popup-img\"></div><br><p class=\"popup-message-topk\">' + "Keyword: " + array +'</p></div>';
    } else {
      mhtml = '<div class="popup-window-topk-nokey"><div class=\"popup-title\">' + id + '<img src=\"./img/popup/bar-nameBar.png\" class=\"popup-img\"></div></div>';
    }

    const el1 = document.createElement('div');
    el1.id = 'top_k_marker1';
    let popup = new mapboxgl.Popup({ closeButton: false})
      .setHTML(mhtml)
    const marker1 = new mapboxgl.Marker(el1, { scale: 0.8})
      .setLngLat([lon, la])
      .setPopup(popup)
      .addTo(vueThis.map)


    //绘制除根节点外其他带关键字的顶点，悬浮在根节点附近，沿圆分布
    let x = -0.15; //-0.0015
    let y = 0.0;  //0.0
    let turn = true;
    let sourceD = [];
    let sourceDK = [];
    for(let i=0;i<point_key.length;i++) {
      if(turn === true)
        y = 1.0 * Math.sqrt(0.2 * 0.2 - x * x).toFixed(10); //0.003，10
      else
        y = -1.0 * Math.sqrt(0.2 * 0.2 - x * x).toFixed(10);
      console.log(point_key.at(i).s_id);
      console.log(x);
      console.log(y);
      let x1 = lon + x;
      let y1 = la + y;
      let mhtml1 = '<div class="popup-window-topk-haskey"><div class=\"popup-title\">' + point_key.at(i).s_id + '<img src=\"./img/popup/bar-nameBar.png\" class=\"popup-img\"></div><br><p class=\"popup-message-topk\">' + point_key.at(i).s_key +'</p></div>';
      let popup1 = new mapboxgl.Popup({ closeButton: false})
        .setHTML(mhtml1)
      const el2 = document.createElement('div');
      el2.id = 'top_k_marker2';

      console.log(x1)
      console.log(y1);
      const marker2 = new mapboxgl.Marker(el2, { scale: 0.5, type: 'circle'})
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
          "line-width": 1, // 线条宽度
           "line-opacity": 0.5, // 线条透明度
            "line-color": '#00FDFF', // 线条颜色
        }
      });

      })

      //修改xy值
      if(turn === true)
        x += 0.32;//0.0008
      else
        x -= 0.32; //0.0008
      if(x >= 0.2) // 0.003
      {
        x -= 0.2; //0.003
        if(turn === true)
          turn = false;
        else
          turn = true;
      } else if(x <= -0.2) { //-0.003
        x += 0.2; //0.003
        if(turn === true)
          turn = false;
        else
          turn = true;
      }
    }

}

async function JudgeInput(lon, la, key, k) {
  //判断输入合法性函数
  if(isNaN(lon) || isNaN(la) || key.length < 1 || isNaN(k) ) {
    alert("Please input correct parameters!");
    return 1;
  }
  return 0;
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
      LoadtopK(vueThis, lon, la, key, k); //显示查询坐标点和地图
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
    StarLoadtopK,
    JudgeInput,
    PostTopK
}
