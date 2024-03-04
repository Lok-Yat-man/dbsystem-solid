let db_header = Vue.extend({
    template: `
      <div id="header">
        <img class="header-image" id="top-image" src="./img/bg_top.png"/>
        <img class="header-title" id="title-image" src="./img/STARS.png"/>
        <div class="subheader">
            <div class="title1">
                <div class="ename">Spatial-Temporal Analytics with Rapid System</div>
            </div>
            <div class="title2">
                <div id="school">
                    <span class="cname">深圳大学</span>
                </div>
                <div id="college">
                    <span class="cname">深圳大学计算机与软件学院</span>
                </div>
                <div id="lab">
                    <span class="cname">大数据与应用数据研究所</span>
                </div>
            </div>
        </div>
    </div>
    `,
    data(){
        return {
        }
    }
})

Vue.component('db-header', db_header);