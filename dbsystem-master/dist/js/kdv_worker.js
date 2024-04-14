async function compute_kdv(kdv_type,bandwidth,resolution,ratio,
  st,ed,long_L,long_U,lat_L,lat_U,t_L,t_U,t_pixels,bw_t){

  bandwidth = bandwidth * (long_U-long_L) /0.111

  const result = await this.lib.compute(1,kdv_type,bandwidth,resolution,resolution*ratio,
    st,ed,long_L,long_U,lat_L,lat_U,t_L,t_U,t_pixels,bw_t)

  const radius = (Math.round((long_L-long_U)*-10000)/resolution*1.15)
  //const result = lib.compute(1, 0.001, 256, 256, 2789, 2919, 113.52580648132354, 113.56786351867696, 22.17086086934504, 22.21735166312359)
  return [str2ab(result),radius]
}

function str2ab(str) {
  var buf = new ArrayBuffer(str.length*2); // 2 bytes for each char
  var bufView = new Uint8Array(buf);
  for (var i=0, strLen=str.length; i<strLen; i++) {
    bufView[i] = str.charCodeAt(i);
  }
  return buf;
}


self.importScripts("./kdv.js");
this.lib = 0
this.running_tasks = 0

self.onmessage = async (message) => {
  
  if (message.data[0]==3 || !this.lib) await kdv().then(instance=>{this.lib = instance;this.lib.load_data();this.ready=true});



  this.running_tasks += 1
  
  setTimeout(
    () => compute_kdv(...message.data).then(([result,radius]) =>{
      this.running_tasks+=-1
      postMessage(result,[result])
      postMessage(radius)
    }), 
    0
  );


  // if (this.ready == true){
  //   this.ready=false;
  //   console.log(...message.data)
  //   compute_kdv(...message.data).then(([result,radius]) =>{
  //     postMessage(result,[result])
  //     postMessage(radius)
  //   })
  // }
  //console.log(compute())
  
  // toArrayBuffer(data).then(arrayBuffer => {

  // postMessage(arrayBuffer,[arrayBuffer])
  // })
};