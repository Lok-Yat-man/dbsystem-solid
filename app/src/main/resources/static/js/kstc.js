import utils from "./utils.js";

async function loadKSTC(vueThis){

    var kstc = vueThis.KSTC;
    kstc.query.keyword=kstc.message.split(",");

    console.log(kstc.query);

    await axios({

    })

}




export default {
    loadKSTC
}