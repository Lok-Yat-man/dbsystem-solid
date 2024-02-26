let db_sidebar = Vue.extend({
    template: `
     <el-row class="tac sideOut" id="sideBar" style="overflow:hidden;" >
        <el-col :span="12">
            <!--              <h3 style="text-align: center">Menu</h3>-->

            <el-menu
                    class="el-menu-vertical-demo"
                    default-active="1-1-1"
                    unique-opened
                    v-loading="mapLoading"
            >
                <el-submenu index="1">
                    <template slot="title">
                        <span>Clustering</span>
                    </template>
                    <el-submenu index="1-1">
                        <template slot="title">Geo-social network</template>
                        <el-menu-item index="1-1-1" @click="loadDSPGS('StockholmSweden', 13)" >Stockholm Sweden</el-menu-item>
                        <el-menu-item index="1-1-2" @click="loadDSPGS('MalmoSweden', 13)" >Malmo Sweden</el-menu-item>
                        <el-menu-item index="1-1-3" @click="loadDSPGS('GothenburgSweden', 13)" >Gothenburg Sweden</el-menu-item>
                        <el-menu-item index="1-1-4" @click="loadDSPGS('AustinUS', 13)" >Austin US</el-menu-item>
                        <el-menu-item index="1-1-5" @click="loadDSPGS('LondonUK', 13)" >London UK</el-menu-item>
                        <el-menu-item index="1-1-6" @click="loadDSPGS('NewcastleUponTyneUk', 13.5)" >Newcastle Upon Tyne UK</el-menu-item>
                        <el-menu-item index="1-1-7" @click="loadDSPGS('OsloNorway', 13)" >Oslo Norway</el-menu-item>
                        <el-menu-item index="1-1-8" @click="loadDSPGS('ZurichSwitzerland', 13)" >Zurich Switzerland</el-menu-item>
                    </el-submenu>
                    <el-submenu index="1-2">
                        <template slot="title">Spatial textual clustering</template>
                        <el-menu-item index="1-2-3" @click="loadKStc('')">Simple KSTC</el-menu-item>

                    </el-submenu>
                    <el-submenu index="1-3">
                        <template slot="title">Textually spatial skylines</template>
                        <el-menu-item index="1-3-1"  @click="paramsSwitch('spatial_skylines')">1-3-1</el-menu-item>
                    </el-submenu>

                    <el-submenu index="1-4">
                        <template slot="title">Mapbox test ground</template>
                        <el-menu-item index="1-4-1"  @click="loadTest()">1-4-1</el-menu-item>
                    </el-submenu>
                </el-submenu>
                <el-submenu index="2">
                    <template slot="title">
                        <span slot="title">Visualization</span>
                    </template>
                    <el-submenu index="2-1">
                        <template slot="title">KDV</template>
                        <el-menu-item index="2-1-1" @click="loadKDV">kdv</el-menu-item>
                    </el-submenu>

                    <el-submenu index="2-2">
                        <template slot="title">Temporal KDV</template>
                        <el-menu-item index="2-2-1">2-2-1</el-menu-item>
                    </el-submenu>

                    <el-submenu index="2-3">
                        <template slot="title">Line KDV</template>
                        <el-menu-item index="2-3-1">2-3-1</el-menu-item>
                    </el-submenu>
                </el-submenu>
            </el-menu>
        </el-col>
    </el-row>
    `,
    data(){
        return {
        }
    }
})

Vue.component('db-sidebar', db_sidebar);