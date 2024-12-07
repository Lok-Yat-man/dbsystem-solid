import util from "./utils.js";

function loadHeatMap(vueThis) {
  vueThis.map = new mapboxgl.Map({
    container: "map", // container id
    style: vueThis.mapStyle,
    center: [
      (vueThis.kdv.x_U + vueThis.kdv.x_L) / 2,
      (vueThis.kdv.y_L + vueThis.kdv.y_U) / 2,
    ],
    zoom: 10.9,
  });
  vueThis.map.on("load", function () {
    getBounds(vueThis);
    let request = compute(vueThis.kdv);
    axios
      .post(vueThis.baseUrl + "/kdv/geojson", request)
      .then(function (response) {
        //console.log(response.data);
        // 添加 GeoJSON 数据源
        vueThis.map.addSource("points-source", {
          type: "geojson",
          data: response.data,
        });
        //console.log(response.data);
        buildHeatmap(vueThis);
      });
  });
  vueThis.map.on("zoomend", function () {
    vueThis.map.removeLayer("trees-heat");
    getBounds(vueThis);
    let request = compute(vueThis.kdv);
    axios
      .post(vueThis.baseUrl + "/kdv/geojson", request)
      .then(function (response) {
        console.log(response.data);
        // 添加 GeoJSON 数据源
        vueThis.map.removeSource("points-source");
        vueThis.map.addSource("points-source", {
          type: "geojson",
          data: response.data,
        });
        console.log(response.data);
        buildHeatmap(vueThis);
      });
  });
}

function getBounds(vueThis) {
  let bounds = vueThis.map.getBounds(); // 获取当前地图的边界
  if (bounds) {
    let sw = bounds.getSouthWest(); // 西南角经纬度
    let ne = bounds.getNorthEast(); // 东北角经纬度
    vueThis.kdv.x_L = sw.lng;
    vueThis.kdv.x_U = ne.lng;
    vueThis.kdv.y_L = sw.lat;
    vueThis.kdv.y_U = ne.lat;
  }
}

function buildHeatmap(vueThis) {
  vueThis.map.addLayer({
    id: "trees-heat",
    type: "heatmap",
    source: "points-source",
    maxzoom: 24,
    minzoom: 3,
    paint: {
      // increase weight as diameter breast height increases
      "heatmap-weight": {
        property: "dph",
        type: "exponential",
        stops: [
          [0, 0],
          [1, 0.25],
        ],
      },
      // increase intensity as zoom level increases
      "heatmap-intensity": 1,
      // assign color values be applied to points depending on their density
      "heatmap-color": [
        /**
         * interpolate： 对颜色进行线性插值处理
         * step：对颜色进行跳跃处理
         */
        "step",
        // ["linear"],
        ["heatmap-density"],
        "rgba(0, 0, 0, 0)", // 默认颜色
        0,
        "rgba(0, 0, 0, 0)",
        0.05,
        "rgb(37, 52, 148)",
        0.1,
        "rgb(8, 69, 148)",
        0.2,
        "rgb(7, 107, 158)",
        0.3,
        "rgb(0, 150, 169)",
        0.4,
        "rgb(0, 188, 160)",
        0.5,
        "rgb(95, 205, 125)",
        0.6,
        "rgb(199, 233, 109)",
        0.7,
        "rgb(255, 241, 118)",
        0.8,
        "rgb(255, 160, 64)",
        0.85,
        "rgb(230, 150, 64)",
        0.9,
        "rgb(230, 116, 40)",
        0.95,
        "rgb(218, 95, 42)",
        0.97,
        "rgb(216, 135, 34)",
        0.98,
        "rgb(200, 93, 37)",
        0.985,
        "rgb(199, 109, 65)",
        0.99,
        "rgb(191, 86, 34)",
        0.995,
        "rgb(191, 86, 34)",
        0.998,
        "rgb(191, 86, 34)",
        1.0,
        "rgb(240, 28, 28)",
      ],
      // increase radius as zoom increases
      "heatmap-radius": {
        stops: [
          [11.5, 25],
          [24, 40],
        ],
      },
      // decrease opacity to transition into the circle layer
      "heatmap-opacity": 1,
    },
  });
}

function compute(kdv) {
  // Module.onRuntimeInitialized = function() {
  // };
  // return Module.compute(kdv.kdv_type, kdv.num_threads, kdv.x_L, kdv.x_U,
  //     kdv.y_L,kdv.y_U,kdv.row_pixels,kdv.col_pixels,kdv.kernel_s_type, kdv.bandwidth_s,
  //     kdv.t_L,kdv.t_U,kdv.kernel_s_type,kdv.bandwidth_t,kdv.cur_time);

  // dbsystem
  Module.load_data();
  return Module.compute(
    1,
    1,
    0.01,
    250,
    250,
    1904,
    12169,
    kdv.x_L,
    kdv.x_U,
    kdv.y_L,
    kdv.y_U,
    kdv.t_L,
    kdv.t_U,
    kdv.cur_time,
    kdv.bandwidth_t
  );
  // return Module.compute(1, 1, kdv.bandwidth_s, kdv.row_pixels, kdv.col_pixels,
  //     st, ed, kdv.x_L, kdv.x_U, kdv.y_L, kdv.y_U, kdv.t_L, kdv.t_U, kdv.cur_time, kdv.bandwidth_t);
}

export default {
  loadHeatMap,
};
