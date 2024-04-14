
import React, { useRef, useEffect } from 'react'


function hslToRgb(h, s, l){
    var r, g, b;
    if(s == 0){
        r = g = b = l; // achromatic
    }else{
        var hue2rgb = function hue2rgb(p, q, t){
            if(t < 0.0) t += 1.0;
            if(t > 1.0) t -= 1.0;
            if(t < 1.0/6.0) return p + (q - p) * 6.0 * t;
            if(t < 1.0/2.0) return q;
            if(t < 2.0/3.0) return p + (q - p) * (2.0/3.0 - t) * 6.0;
            return p;
        }
  
        var q = l < 0.5 ? l * (1.0 + s) : l + s - l * s;
        var p = 2.0 * l - q;
        r = hue2rgb(p, q, h + 1.0/3.0);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1.0/3.0);
    }
    r = Math.min(Math.floor(r*256),255)
    g = Math.min(Math.floor(g*256),255)
    b = Math.min(Math.floor(b*256),255)

    return '#'+ r.toString(16).padStart(2,0)+g.toString(16).padStart(2,0)+b.toString(16).padStart(2,0)

  }

const Legend = props => {
    
  const canvasRef = useRef(null)
  const draw = ctx => {

    props.fullScreen
    var grd = ctx.createLinearGradient(0, 0, props.width, 0);
    ctx.lineWidth=0.5;
    const colour_range = 50

    for(var i=0;i<colour_range;i++){
        var h = (1.0 - Math.min(1,Math.max(0,(i/colour_range))))*2.0/3.0
        grd.addColorStop((i)/colour_range, hslToRgb(h,1,0.5));
    }
    ctx.fillStyle = grd;


    ctx.fillRect(0, 0, props.width, props.height*0.4);

    ctx.lineWidth=0.5
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(0,props.height*0.5)
    ctx.lineTo(props.width, props.height*0.5)   
    ctx.closePath()
    ctx.stroke()
    ctx.fillStyle='#000000'
    

    const fontSize = 20
    const fontFamily= 'Times New Roman'
    ctx.font= fontSize + 'px ' + fontFamily;

    ctx.fillRect(0, props.height*0.52, 5, 5);
    ctx.fillText(0,0,props.height)

    for(var i=1;i<5;i++){
      ctx.fillRect(props.width/5*i, props.height*0.52, 5, 5);
      ctx.fillText(i*20,props.width/5*i-10,props.height)
    }
  

    ctx.fillRect(props.width-5, props.height*0.52, 5, 5);
    ctx.fillText(100,props.width-35,props.height)




  }
  
  useEffect(() => {
    
    const canvas = canvasRef.current
    const context = canvas.getContext('2d')
    draw(context)
  }, [draw])
  
  return (

  <canvas ref={canvasRef} {...props}/>
  )
}

export default Legend