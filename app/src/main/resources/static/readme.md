## 新功能增加
- index.html需要在paramsIn中添加对应参数框 和 sideBar中添加对应类型和onclick事件
- onclick的函数在app.js中定义,以load开头，注意参数框的底边距也在这定义

## 问题
- app.js中env和baseUrl的意义不明，baseURL没有必要，直接`/data/test.geojson`绝对路径拿就好了数据
- 很多变化都是直接修改data，没有使用vue3的响应式，所以数据变化没有触发视图更新，尤其是各算法组件直接使用vueThis来访问数据，很奇怪
- index.html各组件内容高度重合，建议使用框架重构，paramsIn和sideBar写成组件
- css文件很多重复部分复制粘贴，建议改成less或者sass增加复用
- **Spring Boot的意义是什么？**前端部分完全可以单独跑，怀疑只是用下SB的Tomcat托管web服务

## ToDoList
- 去掉env和baseUrl，使用绝对路径
- **使用Vite重构**
