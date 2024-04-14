// import { ScatterplotLayer } from 'deck.gl';
import {PathLayer,GeoJsonLayer,ScatterplotLayer} from '@deck.gl/layers';
import {RectangleLayer} from './RectangleLayer'
import {DataFilterExtension} from '@deck.gl/extensions';
import {CSVLoader} from '@loaders.gl/csv';
import {IconLayer} from '@deck.gl/layers';


const PICKUP_COLOR = [114, 19, 108];
const DROPOFF_COLOR = [243, 185, 72];

const cAlpha = 180

const HEATMAP_COLORS = [
/*
  [255,195,0],
  [241,146,14],
  [227,97,28],
  [199,0,57],
  [144,12,63],
  [90,24,46],
*/
/*
  [213, 62, 79],
  [252, 141, 89],
  [254, 224, 139],
  [230, 245, 152],
  [153, 213, 148],
  [50, 136, 189]
  */
  
 [158,1,66,],
 [213,62,79,],
 [244,109,67,],
 [253,174,97,],
 [254,224,139,],
 [230,245,152,],
 [171,221,164,],
 [102,194,165,],
 [50,136,189,],
 [94,79,162,],
 
];




function getColor(d,max) {
  const z = d.v;
  return(
  z>max * 0.95? HEATMAP_COLORS[0]:
  z>max *0.85? HEATMAP_COLORS[1]:
  z>max *0.75? HEATMAP_COLORS[2]:
  z>max *0.65? HEATMAP_COLORS[3]:
  z>max *0.55? HEATMAP_COLORS[4]:
  z>max * 0.45? HEATMAP_COLORS[5]:
  z>max *0.35? HEATMAP_COLORS[6]:
  z>max *0.25? HEATMAP_COLORS[7]:
  z>max *0.15? HEATMAP_COLORS[8]:
  z>max*0.06? HEATMAP_COLORS[9]:
  [0,0,0,0])
}

const dataFilter = new DataFilterExtension({
  filterSize: 1,
  // Enable for higher precision, e.g. 1 second granularity
  // See DataFilterExtension documentation for how to pick precision
  fp64: false
});

// function getFill(code){
//   if (code == "R") return [255,150,150,60]
//   else if (code == "Y") return [255,255,0,60]
//   else return [150,150,150,60]
// }

// function getLine(code){
//   if (code == "R") return [255,0,0]
//   else if (code == "Y") return [255,255,150]
//   else return [255,255,255]
// }

function getFill(code){
  if (code == "R") return [213,0,85]
  else if (code == "Y") return [255,255,0]
  else return [220,220,220]
}

function getLine(code){
  if (code == "R") return [120,120,120]
  else if (code == "Y") return [120,120,120]
  else return [120,120,120]
}


function getFillMap(code){
  if (code == "R") return [213,0,85]
  else if (code == "Y") return [255,255,0]
  else return [220,220,220]
}


const ICON_MAPPING = {
  marker: {x: 0, y: 0, width: 128, height: 128, mask: true}
};



export function renderLayers(props) {
  const { data, settings, radius,filter,stKdv,dates,marker,init_lon,init_lat} = props
  return [

    // new PathLayer({
    //   id: 'path',
    //   data: 'trajectory500.json',
    //   getPath: d => d.path,
    // }),
    settings.showHeatMap&&new RectangleLayer({
        visible:settings.showHeatMap,
        id: 'scatterplot',
        data,
        

        loaders: [CSVLoader],
        loadOptions: {
          csv: {
            dynamicTyping: true,
            skipEmptyLines: true
          }
        },
        getPosition: d => [d.lon,d.lat],
        getColor: d => getColor(d,100),
        getRadius: d => 5,
        pickable: false,
        radiusMinPixels: 0.25,
        radiusMaxPixels: 100,
        lineWidthMinPixels: 3,
        radiusScale:radius,
        filterEnabled:stKdv,
        extensions: [dataFilter],
        getFilterValue: d => d.t,
        filterRange: [filter-0.125,filter+0.124],
        ...settings,
      }),
      marker&&new IconLayer({
        id: 'IconLayer',
        data: 'https://raw.githubusercontent.com/visgl/deck.gl-data/master/website/bart-stations.json',
        getColor: d => [255, 72, 0],
        getIcon: d => 'marker',
        getPosition: d => [init_lon,init_lat],
        getSize:  8,
        iconAtlas: 'https://raw.githubusercontent.com/visgl/deck.gl-data/master/website/icon-atlas.png',
        iconMapping: {
          marker: {
            x: 0,
            y: 0,
            width: 128,
            height: 128,
            anchorY: 128,
            mask: true
          }
        },
        sizeScale: 8,
        pickable: true,
      }),



      
    ]
}
