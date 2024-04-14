/* global window */
import React, { Component} from 'react';
import {Map, PositionOptions} from 'react-map-gl';
import maplibregl from 'maplibre-gl';
import { LayerControls, Abouts, Menu, Header, Footer, LatestUpdate,ModalControls, MapStylePicker,SCATTERPLOT_CONTROLS } from './controls';
import { tooltipStyle,chartStyle,colourStyle } from './style';
import DeckGL from 'deck.gl';
import { renderLayers } from './deckgl-layers';
import {WebMercatorViewport} from '@deck.gl/core';
import {load,parse} from '@loaders.gl/core';
import {CSVLoader,OBJLoader} from '@loaders.gl/csv';
import { addDays } from 'date-fns';
import RangeInput from './range-input';
import './app.css'
import 'mapbox-gl/dist/mapbox-gl.css';
import {date_to_index,max_date,st_date} from './date_range.js'
import Legend from './legend'
//import { fs } from 'memfs';

import intl from 'react-intl-universal';
import {emit} from "./emit.js"


const locales = {
    "en": require('./locales/en.json'),
    "zh": require('./locales/zh.json')
 };
intl.init({
     currentLocale: 'en',
     locales
})




const API_TOKEN = "c721d12c7b7f41d2bfc7d46a796b1d50";
const FIRSTDATE = new Date(st_date*1000)

const hk_bound = [113.8321,114.4501,22.1416,22.5760]

const bound = hk_bound

function isPC() {
  var userAgentInfo = navigator.userAgent;
  var Agents = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPod"];
  var flag = true;
  for (var i = 0; i < Agents.length; i++) {
      if (userAgentInfo.indexOf(Agents[i]) > 0) {
          flag = false;
          break;
      }
  }
  return flag;
};
function formatLabel(t) {

  const d = addDays(FIRSTDATE,t+1)
  const year = d.getUTCFullYear().toString()
  const date = (d.getUTCDate()).toString()
  const month = (d.getUTCMonth()+1).toString()

  return `${date+'/'+month+'/'+year}`;
}

const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const lon = urlParams.get('lon')?parseFloat(urlParams.get('lon')):114.147835
const lat = urlParams.get('lat')?parseFloat(urlParams.get('lat')):22.33410819
const zoom = urlParams.get('zoom')?parseFloat(urlParams.get('zoom')):null
const marker = urlParams.get('marker')==1?true:false




const HK_VIEW_STATE = {
  longitude: lon,
  latitude: lat,
  zoom: zoom?zoom:11,
  minZoom: 8,
  maxZoom: 50,
  pitch: 0,
  bearing: 0,
};



const resoluiton_candidate = isPC()?[300,500,1000,1500,2000]:[100,200,300,400,500]

const INITIAL_VIEW_STATE = HK_VIEW_STATE

function setCookie(cname,cvalue,exdays)
{
  var d = new Date();
  d.setTime(d.getTime()+7*(exdays*24*60*60*1000));
  var expires = "expires="+d.toGMTString();
  document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname)
{
  var name = cname + "=";
  var ca = document.cookie.split(';');
  for(var i=0; i<ca.length; i++) 
  {
    var c = ca[i].trim();
    if (c.indexOf(name)==0) return c.substring(name.length,c.length);
  }
  return "";
}


export default class App extends Component {
  constructor(props) {
    super(props);
  this.state = {
    hover:{
      x:0,
      y:0,
      hoveredObject: null
    },
    points: [],
    settings: Object.keys(SCATTERPLOT_CONTROLS).reduce(
      (accu, key) => ({
        ...accu,
        [key]: SCATTERPLOT_CONTROLS[key].value
      }),
      {}
    ),

    filter:Math.floor((addDays(FIRSTDATE,0)-FIRSTDATE)/(1000*24*60*60)),
    viewState: INITIAL_VIEW_STATE,
    bound:[0,180,0,180],
    currentStyle: 'meth_1',
    method:'0',
    max: 100,
    bandwidth:1,
    radius:1,
    showLayers: false,
    showSettings: false,
    showAbouts: false,
    fullScreen: false,
    compute:0,
    marker:marker,
    init_lon:lon,
    init_lat:lat,
    lang:'en',
    busy:false,
    style:'https://maps.geoapify.com/v1/styles/positron/style.json?apiKey='+API_TOKEN,

    
    changeLayer: "Light",
    selectionRange : {
      startDate:  addDays(FIRSTDATE,0),
      endDate: addDays(FIRSTDATE, max_date-1),
      color: 'rgba(0,44,85,0.87)',
      autoFocus: true,
      key: 'selection',
      showDateDisplay: false,
    },
    openCalendar: false,
    showModal: false,
    modalTitles: ['Macau COVID-19 Case HotSpot Anaylsis', 'The Hotspots', 'The Settings'],
    modalContents : [
      'Welcome to use our system “Macau COVID-19 Case HotSpot Anaylsis”. This is an interactive map for hotspot analysis based on the living and working locations of positive cases. Hotspot analysis is used to analyze data relative distance and relative density. The hotspots are computed by our world fastest ever hotspot computation library, LibKDV (https://github.com/libkdv/libkdv).',
      'According to the hotspot in the figure, red zone is the densest area (in this region) and orange zone is the second. As a comparison, purple zone is a relatively safe region as it is relatively far from the densest area.',
      'User can select data interval, hotspot resolution (higher resolution, higher CPU loading), and hotspot bandwidth (the distance range for hotspot analysis).'
    ],
    modalIndex: 0,
  
  };

  //this._onViewStateChange = this._onViewStateChange.bind(this);
  this._onHover = this._onHover.bind(this);
  this.getCursor = this.getCursor.bind(this);


  const visited=getCookie("visited");
  if (visited==""){
    //this.state.showModal = true
    setCookie("visited",1,1);
  }


  const time = new Date().getHours()
  this.state.style = 'https://api.maptiler.com/maps/7885a61f-e4cb-466c-a736-4dbf060c2270/style.json?key=zCrGI4RKkWAugUfCSlE1'
  //this.setState({style})
  this.state.settings.runFlag = true;


  // var factory = require('./kdv.js');
  // //this.setState({kdv: 2})
  // factory().then(instance => {
    
  //   let compute = instance.compute
  //   instance.load_data()
  //   let MemFS = instance.FS
  //   this.state.compute = compute
  //   this.state.MemFS = MemFS
  //   this.state.settings.runFlag = true;
  // })

  this.worker = new Worker("./js/kdv_worker.js");
  this.worker.onmessage = (msg) => {
    if (typeof(msg.data) == 'number'){
    this.updateRadius(msg.data)
    }
    else{
      // fs.writeFile('hello.csv',msg.data)
      // fs.readFile('')
      parse(msg.data,CSVLoader).then((response)=>{
        this.setData(response)
      })
    }
  
    this.setState({busy:false})
    
    // load(msg.data, CSVLoader).then((response)=>{
    //   console.log(response)
    //   this.setData(response)
    //   this.updateRadius(Math.round((long_L-long_U)*-10000)/resolution*1.15)
    // })
  };



}


  getDateRange(){
    if (this.state.settings.stKdv==false)
    return [
      (date_to_index[1]-date_to_index[0])*(7)+
      date_to_index[
        Math.min(max_date,Math.max(0,Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))))
      ],
      (date_to_index[1]-date_to_index[0])*(7)+1+
      date_to_index[
        Math.min(max_date,Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))+1)
      ]
    ]
    else return [
      (date_to_index[1]-date_to_index[0])*(7-this.state.settings.bandwidth_t)+
      date_to_index[
        Math.min(max_date,Math.max(0,Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))))
      ],
      (date_to_index[date_to_index.length-1]-date_to_index[date_to_index.length-2])*this.state.settings.bandwidth_t+
      (date_to_index[1]-date_to_index[0])*(7)+
      date_to_index[
        Math.min(max_date,Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))+1)
      ]
    ]
  }
  
  getScreenRatio(viewState){
    const viewport =  new WebMercatorViewport(viewState);
    const nw = viewport.unproject([0,0]);
    const se = viewport.unproject([viewport.width,viewport.height]);
    return (nw[1]-se[1])/(se[0]-nw[0])

  }
  getBound(viewState){
    //return (this.state.viewState)
    const viewport =  new WebMercatorViewport(viewState);
    const nw = viewport.unproject([0,0]);
    const se = viewport.unproject([viewport.width,viewport.height]);
    return([Math.max(nw[0],bound[0]),Math.min(se[0],bound[1]),Math.max(se[1],bound[2]),Math.min(bound[3],nw[1])]);
  }

  getResolution(){
    return this.state.settings.Resolution
  }

  getRunFlag(){
    //return true;
    return this.state.settings.runFlag
  }

  getBandwidth(){
    return this.state.settings.bandwidth
  }
  getKernel(){
    return this.state.method
  }
  getMethod(){
    if (this.state.method=='5'||this.state.method=='0'){
        return '2'
    }
    else if(this.state.method='1')
    {
        return '1'
    }
    
    else if (this.state.method = '2'){
    return '0'
    }
    else{
    return '3'
    }
    
  }

  getDate(){
    return this.state.settings.date
  }
  setData(points){
    this.setState({
      points
    });
  };

  setMax(max){
    this.setState({
      max
    })
  }
  updateRadius = radius => {
    this.setState({radius})
  }

  updateShowLayers = () => {
    this.setState({
      showLayers: !this.state.showLayers,
      showSettings: false,
      showAbouts: false
    });
  }
  updateShowSettings = () => {
    this.setState({
      showSettings: !this.state.showSettings,
      showLayers: false,
      showAbouts: false
    });
  }
  updateShowAbouts = () => {
    this.setState({
      showAbouts: !this.state.showAbouts,
      showLayers: false,
      showSettings: false
    });
  }
  updateFullScreen = () => {
    this.setState({
      fullScreen: !this.state.fullScreen
    });
  }
  updateMenu = () => {
    this.setState({
      showAbouts: false,
      showLayers: false,
      showSettings: false
    });
  }
  updateChangeLayer = (layer) => {
    this.setState({
      changeLayer: layer
    });
  }

  updateOpenCalendar = () => {
    this.setState({
      openCalendar: !this.state.openCalendar,
    });
  }
  updateSelectionRange = (selection) => {
    this.setState({
      selectionRange: selection
    });
  }
  
  updateFilter = filter =>{
    this.setState({filter})
  };

  onStyleChange = style => {
    this.setState({ style });
  };
  onMethodChange = method => {
    this.setState({ method });
  };

  updateShowModal = () => {
    this.setState({
      showModal: false
    });
  }
  updateModalIndex = () => {
    if (this.state.modalIndex != 2) {
      this.setState({
        modalIndex: this.state.modalIndex + 1
      })
    } else {
      this.setState({
        modalIndex: 0
      })
    }
  
  }

  _updateLayerSettings(settings) {
    this.setState({ settings });
  };


  debounce(fn, ms) {
    let timer;
    return (...args) => {      
      clearTimeout(timer);
      timer = setTimeout(() => {
        timer = null;

        if (this.getRunFlag()==true){
          this.setState({viewState:args[0].viewState})
          fn.apply(this,args)
        }
      }, ms);
  }
  }

  componentDidMount(){
    emit.on('change_language',lang => this.loadLocales(lang));
    this.loadLocales();
  }  

  loadLocales = (lang = "en") =>{
    intl.init({
      currentLocale: lang,
      locales
    })
  }

  changeLanguage = () =>{
    let next_lang = this.state.lang=='en'?'zh':'en'
    emit.emit('change_language',next_lang)
    this.setState({lang:next_lang})
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.state.settings.stKdv !== prevState.settings.stKdv){
      if (this.state.settings.stKdv==true){
        var new_settings = this.state.settings
        new_settings.Resolution=1
        //this._updateLayerSettings(new_settings)
      }
      else{
        var new_settings = this.state.settings
        new_settings.Resolution=isPC()?3:2
      }
    }

    if ((this.state.settings.bandwidth !== prevState.settings.bandwidth) || 
      (this.state.settings.Resolution !== prevState.settings.Resolution) ||
      (this.state.settings.stKdv !== prevState.settings.stKdv) ||
      (this.state.selectionRange !== prevState.selectionRange) ||
      (this.state.settings.bandwidth_t !== prevState.settings.bandwidth_t)
      ) {
      this.compute_kdv({viewState:this.state.viewState})
    }





  }

  
  compute_kdv = ({viewState}) =>{
    if (this.state.busy) return
    const [st,ed] = this.getDateRange()

    const [long_L,long_U,lat_L,lat_U] = this.getBound(viewState);
    const ratio = this.getScreenRatio(viewState)

    const bandwidth = this.getBandwidth()/4000/5*20;
    
    const kdv_type = this.state.settings.stKdv? 3:1
    const resolution = resoluiton_candidate[this.getResolution()-1]
    

    try{this.worker.postMessage([kdv_type,bandwidth/2,resolution,ratio,
      st,ed,long_L,long_U,lat_L,lat_U,Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))+max_date,
      Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))+1+max_date,
      4*(Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))-Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))+1)
      
      ,this.state.settings.bandwidth_t])
    }
    catch(e){
    }
      this.setState({busy:true})
    
    // const buffer = Buffer.from(this.state.MemFS.readFile('./tmp.bin'))
    // var tik = performance.now()
    // load(buffer, CSVLoader).then((response)=>{
    //   this.setData(response)
    
    //   this.updateRadius(Math.round((long_L-long_U)*-10000)/resolution*1.15)
    // })
    //var tok = performance.now()    
    //load(result)


  }


  _onHover({x, y, object}) {


    const cstat = object?(
      object.properties.restriction == 'R'?
      '紅碼區'
      :object.properties.restriction == 'Y'?
      '黃碼區'
      :object.properties.restriction == 'U'?
      '解封'
      :'沒有封控')
      :null;


      const pstat = object?(
      object.properties.restriction == 'R'?
      'Lockdown Zone'
      :object.properties.restriction == 'Y'?
      'Precautionary Zone'
      :object.properties.restriction == 'U'?
      'Reopening Zone'
      :'Normal')
      :null;


    const cname = object?
    object.properties.cname
    :null;
    const caddr = object?
    object.properties.caddr
    :null;

    const count = object?
    object.properties.infected_count:null;

    const pname = object?
    object.properties.pname
    :null;
    const paddr = object?
    object.properties.pname
    :null;

    this.setState({hover: {x, y, hoveredObject: object,cname,caddr,count,pname,paddr,cstat,pstat}});
  }

  getCursor = (h) =>{
    if (this.state.busy) return 'wait'
    else if (h.isDragging)
    return 'grabbing'
    else{
      return 'grab'
    }

  }




  render() {
    const data = this.state.points;

    if (!data.length) {
      // return null;
    }
    const { hover,settings } = this.state;
    const _cname = '大廈名稱'
    const _caddr = '街名'
    const _count = '陽性案例'
    const _cstat = '封控狀態'
    return (
        <div>
          <Header
            changeLanguage = {this.changeLanguage}
            fullScreen={this.state.fullScreen}
          />

        {hover.hoveredObject && (
            <div
              style={{
                ...tooltipStyle,
                transform: `translate(${hover.x}px, ${hover.y}px)`
              }}
            >
              <div>{_cstat}: {hover.cstat}</div>
              <div>{_cname}: {hover.cname}</div>
              <div>{_caddr}: {hover.caddr}</div>
              <div>{_count}: {hover.count} </div>
              <br/>

              <div>Restriction: {hover.pstat} </div>
              <div>Building: {hover.pname}</div>
              <div>Street: {hover.paddr}</div>
              <div>Infection Counts: {hover.count} </div>

              <br/>
            </div>
          )}
          
          
          {/* <div style = {textStyle}>
          This system is a technical demo for
          <br />
          fast Kernel Density Estimation (KDE) heatmap.
          <br />
          Given the privacy of the raw data, the visulization
          <br />
          result is not a reliable indicator of COVID-19 cases.

          </div> */}


          {/* { this.state.showModal ? <ModalControls
            updateShowModal={this.updateShowModal}
            modalTitles={this.state.modalTitles}
            modalContents={this.state.modalContents}
            modalIndex={this.state.modalIndex}
            updateModalIndex={this.updateModalIndex}
          /> : null } */}

          
          { this.state.showLayers ? <MapStylePicker
            onStyleChange={this.onStyleChange}
            currentStyle={this.state.style}
            changeLayer={this.state.changeLayer}
            buttonClick={this.updateChangeLayer}
            updateShowLayers={this.updateShowLayers}
          /> : null }


          { this.state.showSettings ? <LayerControls
            settings={this.state.settings}
            propTypes={SCATTERPLOT_CONTROLS}
            onChange={settings => this._updateLayerSettings(settings)}
            updateShowSettings={this.updateShowSettings}
            updateSelectionRange={this.updateSelectionRange}
            selectionRange={this.state.selectionRange}
            openCalendar={this.state.openCalendar}
            updateOpenCalendar={this.updateOpenCalendar}
          /> : null }


          { this.state.showAbouts ? <Abouts
            updateShowAbouts={this.updateShowAbouts}
          /> : null }
          {/* <LayerControls
            settings={this.state.settings}
            propTypes={SCATTERPLOT_CONTROLS}
            onChange={settings => this._updateLayerSettings(settings)}
          /> */}
          
          {/* <Abouts/> */}
          {/* <div className="test"> */}
          {/* <DeckGL  style={{ top: '120px !important', width: '100%', height: 'calc(100vh - 210px)',  }} */}
          <DeckGL getCursor={this.getCursor}style={{ top: '120px !important', width: '100%'}}
          {...this.state.settings}

          _typedArrayManagerProps= {{overAlloc: 1, poolSize: 0}}
          
          onViewStateChange ={this.debounce(this.compute_kdv,20)}
          controller =  {{scrollZoom: {speed:0.003},smooth:true}}


          layers={renderLayers({
            init_lon:this.state.init_lon,
            init_lat:this.state.init_lat,
            marker: this.state.marker,
            radius: this.state.radius,
            data: this.state.points,
            settings: this.state.settings,
            filter:this.state.filter+max_date,
            stKdv:this.state.settings.stKdv,
            dates:Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))-Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))
          })}
        
          initialViewState={INITIAL_VIEW_STATE}
        >
          
          <Map 
          mapLib={maplibregl}
          mapStyle={this.state.style}

          />
        </DeckGL>
          {/* <Legend 
            width={window.innerWidth*0.4} height={50} style = {colourStyle}>
            fullScreen={this.state.fullScreen}
          </Legend> */}

          <Menu
            updateMenu={this.updateMenu}
            updateShowLayers={this.updateShowLayers}
            updateShowSettings={this.updateShowSettings}
            updateShowAbouts={this.updateShowAbouts}
            updateFullScreen={this.updateFullScreen}
          />

          <LatestUpdate 
            fullScreen={this.state.fullScreen}
          />

          <script
            type="module"
            src="color-legend-element/build/color-legend-element.js"
          ></script>

          <Footer 
            fullScreen={this.state.fullScreen}
          />
          {
          this.state.settings.stKdv && (
          <RangeInput
            min={Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))}
            max={Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))}
            value={this.state.filter}

            animationSpeed={0.5*1/24*(Math.floor((this.state.selectionRange.endDate-FIRSTDATE)/(1000*24*60*60))-Math.floor((this.state.selectionRange.startDate-FIRSTDATE)/(1000*24*60*60))+1)/7}
            formatLabel={formatLabel}
            onChange={(filter)=> this.updateFilter(filter)}
          />)
          }
          
        </div>

    );
  }

}

