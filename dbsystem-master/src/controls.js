import React, { Component } from 'react';
import { methodPicker,mapStylePicker, layerControl } from './style';
import { IoMdMenu } from "react-icons/io";
import { BsCheckLg, BsChevronRight, BsCalendar3 } from "react-icons/bs";
import { IoLayers, IoClose } from "react-icons/io5";
import { GoSettings } from "react-icons/go";
import { AiOutlineInfo } from "react-icons/ai";
import { MdZoomOutMap } from "react-icons/md";
import { DateRange } from 'react-date-range';
import 'react-date-range/dist/styles.css'; // main style file
import 'react-date-range/dist/theme/default.css'; // theme css file
import moment from 'moment';
import {st_date,max_date} from './date_range.js'
import { addDays } from 'date-fns';
import IconButton from '@mui/material/IconButton';
import HtmlTooltip  from '@mui/material/Tooltip';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import Typography from '@mui/material/Typography';
import intl from 'react-intl-universal';

const FIRSTDATE = new Date(st_date*1000)

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
const locales = {
  "en": require('./locales/en.json'),
  "zh": require('./locales/zh.json')
};

intl.init({
  currentLocale: 'en',
  locales
})
export const fuck = intl.get('S-HM')

export const SCATTERPLOT_CONTROLS = {
  showHeatMap: {
    displayName: 'S-HM',
    type: 'boolean',
    value: true
  },

  // showRedzone: {
  //   displayName: 'Show Restricted Area',
  //   type: 'boolean',
  //   value: true
  // },

  

  runFlag:{
    displayName: 'RTS',
    type: 'boolean',
    value: false,
  },

    
  stKdv:{
    displayName: 'TKDV',
    type: 'boolean',
    value: false,
  },

  Resolution:{
    displayName: 'RL',
    type: 'range',
    value: isPC()?3:2,
    step: 1,
    min: 1,
    max: 5
  },




  bandwidth:{
    displayName: 'IR',
    type: 'range',

    value: 5,
    step: 0.1,
    min: 1,
    max: 10
  },

  bandwidth_t:{
    displayName: 'ID',
    type: 'range',
    value: 3,
    step: 1,
    min: 1,
    max: 7
  },
    opacity:{
      displayName: 'Op',
    type: 'range',
    value: 0.5,
    step: 0.01,
    min: 0,
    max: 1
  },


  // date:{
  //   displayName: 'Date',
  //   type: 'range',
  //   value: 41,
  //   min: 0,
  //   max: 41
  // },
};

const DEFAULT_MAPSTYLES = [
  { label: 'Light', value: 'https://api.maptiler.com/maps/7885a61f-e4cb-466c-a736-4dbf060c2270/style.json?key=zCrGI4RKkWAugUfCSlE1' },
  { label: 'Dark', value: 'https://api.maptiler.com/maps/12fae550-8b4a-4d4f-88c8-8f91ff3e94ed/style.json?key=zCrGI4RKkWAugUfCSlE1' },
  { label: 'Satellite', value: 'https://api.maptiler.com/maps/hybrid/style.json?key=zCrGI4RKkWAugUfCSlE1' },
  { label: 'Basic', value: 'https://api.maptiler.com/maps/basic-v2/style.json?key=zCrGI4RKkWAugUfCSlE1' },
  { label: 'Open Street Map', value: 'https://api.maptiler.com/maps/openstreetmap/style.json?key=zCrGI4RKkWAugUfCSlE1' },
];


const METHOD = [
  { label: 'Gaussian', value: '0'},
  { label: 'Triangular', value: '1'},
  { label: 'Cosine', value: '2'},
  { label: 'Exponential', value: '3'},
  { label: 'Quartic', value: '4'},
  { label: 'Epanechnikov', value: '5'},
];

const DATASET = [
  { label: 'Covid-19', value: 'COVID-19'},
  { label: 'Atlanta', value: 'Atlanta'},
  { label: 'New York Traffic Accident', value: 'NewYork'}
];



export function Header({ fullScreen ,changeLanguage}) {
  return (
	<div className="header-container" style = {{animation: fullScreen ? 'header-slide-up 1000ms forwards' : 'header-slide-down 1000ms forwards' }}>
      <div className="header" style={{  animation: fullScreen ? 'header-slide-up 1000ms forwards' : 'header-slide-down 1000ms forwards' }}>                      
        <div className="pc-header-container">
          <div className="header-left">
            {/* <a href="https://www.um.edu.mo/">
              <img className="header-img" src={ "./img/umac-black.png"} width="284" height="96" alt="University of Macau Logo"/>      
            </a>
            <div className="header-seperator"></div> */}
            <div className="pc-title-container">
              <div className="title-container">
                <a href="https://covid19.comp.hkbu.edu.hk/" className="header-text header-title1"> {intl.get('header-title1')}  </a>
                <a href="https://github.com/libkdv/libkdv" className="header-text header-title2">({intl.get('header-title2')})</a>
              </div>

            </div>
          </div>
          <div className="header-right">
            <a href="https://www.um.edu.mo/">
              <img className="header-img2" src={ "./img/umac-black.png"} />
            </a>
            <a href="https://www.hku.hk/">
              <img className="header-img3" src={ "./img/hku.png"} />
            </a>
            <a href="https://hkbu.edu.hk/">
              <img className="header-img4" src={ "./img/hkbu-logo.svg"} />
            </a>

            <div className="header-lang">
              <button onClick={changeLanguage} className="switch_button">
                {intl.get('lang')}
              </button>
            </div>
          </div>


        </div>

        <div className="mobile-header-container">
          <div className="mobile-images-container">
            {/* <a href="https://www.um.edu.mo/">
              <img className="header-img" src={ "./img/mobile-umac-black.jpeg"} width="284" height="96" alt="University of Macau Logo"/>      
            </a> */}
	    {/* <div className="header-seperator mobile-seperator"></div> */}
            <a href="https://www.um.edu.mo/">
              <img className="header-img2" src={ "./img/mobile-umac-black.jpeg"} />
            </a>
            {/* <div className="header-seperator mobile-seperator"></div> */}
            <a href="https://www.hku.hk/">
              <img className="header-img3" src={ "./img/hku.png"} />
            </a>
            {/* <div className="header-seperator mobile-seperator"></div> */}
            <a href="https://hkbu.edu.hk/">
              <img className="header-img4" src={ "./img/hkbu-logo.svg"} />
            </a>

          </div>

          <div className="mobile-header-lang">
            <button onClick={changeLanguage} className="switch_button">
              {intl.get('lang')}
            </button>
          </div>

          <div className="mobile-title-container">
            
            <a href="http://degroup.cis.um.edu.mo/covid-19/" className="header-text header-title1">{intl.get('header-title1')}</a>
            <div style={{ lineHeight: 0.6 }}><a href="https://github.com/libkdv/libkdv" className="header-text header-title2"> {intl.get('header-title2')} </a></div>
            
              
          </div>
        </div>
      </div>
    </div>
  );
}



export function LatestUpdate({ fullScreen }) {
  return (
	<div className="update-container">
      <div className="pc-update-container" style={{  animation: fullScreen ? 'update-slide-down 1000ms forwards' : 'update-slide-up 1000ms forwards' }}>
        <div className="update-text"> {intl.get('update-text1')}<a href="https://chp-dashboard.geodata.gov.hk/covid-19/zh.html" className="update-text">{intl.get('update-text2')}</a></div>
      </div>

    </div>
  );
}
export function Footer({ fullScreen }) {
  return (
	<div className="footer" style={{  animation: fullScreen ? 'footer-slide-down 1000ms forwards' : 'footer-slide-up 1000ms forwards'}}>
      <div className="footer-container">
        <div className="footer-left">
          <div className="footer-title" style={{ display: 'none'}}>{intl.get('footer-title')}</div>
          <div className="footer-text">{intl.get('footer-text')}</div>
        </div>
      </div>
    </div>
  );
}

export function ColorBar({fullScreen}){
  
}


export function MethodPicker({ currentMethod, onMethodChange }) {
  return (
    <select
      className="method-picker"
      style={methodPicker}
      value={currentMethod}
      onChange={e => onMethodChange(e.target.value)}
    >
      {METHOD.map(style => (
        <option key={style.value} value={style.value}>
          {style.label}
        </option>
      ))}
    </select>
  );
}

export function Abouts({ updateShowAbouts }) {
  return (
    <div className="abouts-container">
      <div className="menu-title-container">
        <div className="abouts-title">{intl.get('abouts-title')}</div>
        <button onClick={ updateShowAbouts }><IoClose className="menu-close" /></button>
      </div>
      <div style={{ padding: '4px 0'}}>
        <div className="abouts-text">{intl.get('abouts-text1')}
        
        {intl.get('abouts-text2')} <a href="https://chp-dashboard.geodata.gov.hk/covid-19/zh.html">{intl.get('abouts-text3')} </a>  
        </div>
        {/* <div className="abouts-sub-title">Members</div>
        <div className="abouts-text">This system is a technical demo for LIBKDV (the state-of-the-art algorithm, https://github.com/libkdv/libkdv) for heatmap computation. Given the privacy of the data, the visulization result is not a reliable indicator of COVID-19 cases.</div>
        <div className="abouts-sub-title">Acknowledgements</div>
        <div className="abouts-text">This system is a technical demo for LIBKDV (the state-of-the-art algorithm, https://github.com/libkdv/libkdv) for heatmap computation. Given the privacy of the data, the visulization result is not a reliable indicator of COVID-19 cases.</div>
        Member1, Member2 ??
        Ack??*/}
      </div>
    </div>
  );
}

export function Menu({ updateMenu, updateShowLayers, updateShowSettings, updateShowAbouts, updateFullScreen }) {
  return (
    <div className="menu-container"> 
      <>
        <nav className="menu">
          <input
            type="checkbox"
            href="#"
            className="menu-open"
            name="menu-open"
            id="menu-open"
          />
          <label className="menu-open-button" htmlFor="menu-open" onClick={ updateMenu }>
            <IoMdMenu className="menuIcon active" />
          </label>
          <a href="#" className="menu-item" onClick={ updateShowLayers }>
            {" "}
            <IoLayers className="menuIcon" />
          </a>
          <a href="#" className="menu-item" onClick={ updateShowSettings }>
            {" "}
            <GoSettings className="menuIcon" />
          </a>
          <a href="#" className="menu-item" onClick={ updateShowAbouts }>
            {" "}
            <AiOutlineInfo className="menuIcon" />
          </a>
          <a href="#" className="menu-item" onClick={ updateFullScreen }>
            {" "}
            <MdZoomOutMap className="menuIcon" />
          </a>
        </nav>
      </>
    </div>
  );

}


export function MapStylePicker({ currentStyle, onStyleChange, changeLayer, buttonClick, updateShowLayers }) {
  return (
    <div className="layers-container">
      <div className="menu-title-container">
        <div className="layer-title">{intl.get('layer-title')}</div>
        <button onClick={ updateShowLayers }><IoClose className="menu-close" /></button>
      </div>
      <div style={{ padding: '4px 0'}}>
      {DEFAULT_MAPSTYLES.map(style => (
        <button key={style.value} className={`pc-button ${changeLayer === style.label ? "active" : ""}`}  type="button" onClick={e => {onStyleChange(e.target.value); buttonClick(style.label); }} value={ style.value }>{intl.get(style.label)}</button>
      ))}
      </div>
    </div>
  );
}

// export function MapStylePicker({ currentStyle, onStyleChange }) {
//   return (
//     <div>
//       <select
//         className="map-style-picker"
//         style={mapStylePicker}
//         value={currentStyle}
//         onChange={e => onStyleChange(e.target.value)}
//       >
//         {MAPBOX_DEFAULT_MAPSTYLES.map(style => (
//           <option key={style.value} value={style.value}>
//             {style.label}
//           </option>
//         ))}
//       </select>
//     </div>
//   );
// }

export class LayerControls extends Component {
  _onValueChange(settingName, newValue) {
    const { settings } = this.props;
    // Only update if we have a confirmed change
    if (settings[settingName] !== newValue) {
      // Create a new object so that shallow-equal detects a change
      const newSettings = {
        ...this.props.settings,
        [settingName]: newValue
      };

      this.props.onChange(newSettings);
    }
  }

  calendarScroll() {
    var objDiv = document.getElementById("layerControls");
    //objDiv.scrollTop = objDiv.scrollHeight;
  }
  
  componentDidUpdate() {
    this.calendarScroll();
  }

  render() {
    
    const { title, settings, propTypes, updateShowSettings, selectionRange, updateSelectionRange, openCalendar, updateOpenCalendar = {} } = this.props;
    return (
        <div id="layerControls" className="layer-controls" style={layerControl}>
          <div className="menu-title-container" style={{position: 'sticky', top: 0, background: '#FFFFFF', paddingTop: '20px', zIndex: 200}}>
            <div className="setting-title">{intl.get('setting-title')}</div>
            <button onClick={ updateShowSettings }><IoClose className="menu-close" /></button>
          </div>
          {title && <h4>{title}</h4>}

          <Calendar
            selectionRange={selectionRange}
            updateSelectionRange={updateSelectionRange}
            openCalendar={openCalendar}
            updateOpenCalendar={updateOpenCalendar} 
            calendarScroll={this.calendarScroll}
          />
          {Object.keys(settings).map(key => (

            
            <div key={key} className="setting-row">
              

              <div>
              <label className="setting-item">{intl.get(propTypes[key].displayName)}</label>
                {
                propTypes[key].displayName=="TKDV" &&
                <HtmlTooltip 
                title={
                  <React.Fragment>
                  
                  {intl.get('tool-tip1')}
                  <br></br>
                  {intl.get('tool-tip2')}
                  
                    

                
                  </React.Fragment>
                }
              >
                  <InfoOutlinedIcon color='action'/>
              </HtmlTooltip>
                }
              </div>


              <div className="item-num">
                {settings[key]}
              </div>

              
              <Setting
                settingName={key}
                value={settings[key]}
                propType={propTypes[key]}
                onChange={this._onValueChange.bind(this)}
              />
            </div>
          ))}

        </div>
    );
  }
}


const Setting = props => {
  const { propType } = props;
  if (propType && propType.type) {
    switch (propType.type) {
      case 'range':
        return <Slider {...props} />;
      case 'empty':
        return <a/>;
        // return null;
      case 'boolean':
        return <Checkbox {...props} />;
    }
  }
};



const Checkbox = ({ settingName, value, onChange }) => {
  return (
    <div key={settingName} className="container">
      <div className="input-group">
        <div>{value}</div>
      {/* <input type="checkbox" checked="checked"> */}
      {/* <span class="checkmark"></span> */}
        <input
          type="checkbox"
          id={settingName}
          checked={value}
          onChange={e => onChange(settingName, e.target.checked)}
        />
        <span className="checkmark">
          <BsCheckLg className="checkmark-icon"/>
        </span>
      </div>
    </div>
  );
};



const Slider = ({ settingName, value, propType, onChange }) => {
  const { max = 100,min =0,step =1} = propType;
  return (
    <div key={settingName} style={{ flexBasis: '100%', flexGrow: '1'}}>
      <div className="input-group">
        <div>
          <input
            style={{ width: '100%' }}
            type="range"
            id={settingName}
            min={min}
            max={max}
            step={step}
            value={value}
            onChange={e => onChange(settingName, Number(e.target.value))}
          />
        </div>
      </div>
    </div>
  );
};

const Calendar = ({ selectionRange, updateSelectionRange, openCalendar, updateOpenCalendar, calendarScroll }) => {
  return (
    <div>
      <div className="setting-item">{intl.get('date-filter')}</div>
      <div className="calendar-container" style={ openCalendar ? {padding: 0} : {paddingBottom: ''}}>
        <div className="calendarDate-container">
          <span className="calendar-sub-title">{intl.get('from')}</span>
          <div className="calendar-left">
            <span className="calendar-date">{moment(selectionRange.startDate).format("DD")}</span>
            <div className="calendar-right">
              <span className="calendar-month">{moment(selectionRange.startDate).format("MMM YYYY")}</span>
              <span className="calendar-weekdays">{moment(selectionRange.startDate).format('MMM').toUpperCase()}</span>
            </div>
          </div>
        </div>
        <BsChevronRight className="calendar-arrow"/>
        
        <div className="calendarDate-container">
          <span className="calendar-sub-title">{intl.get('from')}</span>
          <div className="calendar-left">
            <span className="calendar-date">{moment(selectionRange.endDate).format("DD")}</span>
            <div className="calendar-right">
              <span className="calendar-month">{moment(selectionRange.endDate).format("MMM YYYY")}</span>
              <span className="calendar-weekdays">{moment(selectionRange.endDate).format('MMM').toUpperCase()}</span>
            </div>
          </div>
        </div>
        <button className="calendar-btn" onClick={() => {updateOpenCalendar(), calendarScroll()}}><BsCalendar3 className="calendar-icon"/></button>
      </div>

    {/* <div>{JSON.stringify(selectionRange)}</div> */}
    { openCalendar ?
      <div className="customDatePickerWidth">
       <DateRange
          // editableDateInputs={true}
          moveRangeOnFirstSelection={false}
          minDate={FIRSTDATE}
          maxDate={addDays(FIRSTDATE,max_date-1)}
          ranges={[selectionRange]}         
          months={1}
          scroll={{ enabled: true }}  
          onChange={item => updateSelectionRange(item.selection)  }
          // fixedHeight={true}
        /> 
      </div>
      : null}
    </div>
  );
}


export function ModalControls({ updateShowModal, modalTitles, modalContents, modalIndex, updateModalIndex  }) {
  return (
    <div className="modal-container">
      <img className="modal-img" src={"./img/modal_img_" + modalIndex + ".png"} />
      <div className="modal-content">
        <div className="modal-title">{modalTitles[modalIndex]}</div>
        <div className="modal-text">{modalContents[modalIndex]}</div>
        <div className="modal-control">
          <div className="skip-container"><button className="modal-skip" onClick={updateShowModal}>Skip</button></div>
          <div className="modal-dots">
          
            <div className={`modal-index ${modalIndex === 0 ? "active" : ""}`}></div>
            <div className={`modal-index ${modalIndex === 1 ? "active" : ""}`}></div>
            <div className={`modal-index ${modalIndex === 2 ? "active" : ""}`}></div>
          </div>
          <button className="modal-btn" onClick={modalIndex != 2 ? updateModalIndex : updateShowModal}>{modalIndex === 2 ? 'Finish' : 'Next'}</button>
        </div>
      </div>
    </div>
  );
};
