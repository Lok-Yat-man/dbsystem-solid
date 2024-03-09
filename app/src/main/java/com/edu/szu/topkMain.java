package com.edu.szu;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

class Triple<M1,M2,M3> {
  public M1 gap1;
  public M2 gap2;
  public M3 gap3;
  public Triple(M1 gap1, M2 gap2, M3 gap3) {
    this.gap1 = gap1;
    this.gap2 = gap2;
    this.gap3 = gap3;
  }
}

//存储顶点信息
class Point {
  /*记录顶点是否存在*/
  public boolean isNull = true;
  public int id;
  /**加**/
  public String name;
  public String city; //uk没有city
  public double longitude;
  public double latitude;
  public boolean[] hasKey; //记录含有的输入关键字

  /*存放顶点的message*/
  public Vector<String> message = new Vector<String>();

  public Vector<Triple<Integer, Integer, Integer>> recordK = new Vector<>(); //记录每个根节点搜索每个关键字所在顶点及最短路径<关键字，结点，最短路径>
  public int Lp;
  public double Fp;
}

//存储关系关键字
class Relationship {
  public int relationNum;
  public String [] relationKey;
}

class Tuple<T1, T2> {
  public T1 item1;
  public T2 item2;
  public Tuple(T1 item1, T2 item2) {
    this.item1 = item1;
    this.item2 = item2;
  }
}

//存储图信息
class Graph {
  public int pointNum;
  public boolean[] pointIsNull;
  public boolean[] isRoot; //记录是否根结点，默认为false
  public int [][] neighbors;
  public int [] tag; //用于记录邻居的个数
  public int [] inDegree; //记录每个顶点的入度
  public Vector<Tuple<Integer, Double>> Sp = new Vector<>();
  public Vector<Tuple<Integer, Double>> FindPoint = new Vector<>(); //记录top-k结果
}

@RestController
public class topkMain {
  Double QLongitude, QLatitude;
  String inputKeyword;
  int Qk;

  @GetMapping("/hello")
  public JSONArray getLon(@RequestParam(value = "lon_topk") String lon,
                          @RequestParam(value = "la_topk") String la,
                          @RequestParam(value = "key_topk") String key,
                          @RequestParam(value =  "k_topk") String inputk) {
    System.out.println("\nlongitude: " + lon + "  latitude: " + la + "  keyword: " + key + "  k: " + inputk);
    QLongitude = Double.parseDouble(lon);
    QLatitude = Double.parseDouble(la);
    inputKeyword = key;
    Qk = Integer.parseInt(inputk);

    JSONArray jsonArray = new JSONArray();

    /*读取顶点的keyword信息*/
    /*把绝对路径改为相对路径*/
    File file1 = new File("app/src/main/resources/static/data/txt/uk/eid2keyword.txt");
    /*读取顶点的经纬度信息*/
    File file2 = new File("app/src/main/resources/static/data/txt/uk/eid2coor.txt");
    Point[] points = new Point[1000000];
    for(int k=0;k<1000000;k++) {
      points[k] = new Point();
    }
    int total = 34000; //记录点的id最大值
    try {
      BufferedReader reader1 = new BufferedReader(new FileReader(file1));
      String line;
      while((line = reader1.readLine()) != null) {
        String[] numberAsString = line.split(",");
        String[] temp = numberAsString[0].split(":");
        int ID = Integer.parseInt(temp[0]);
        if(temp.length > 1) {
          String firstMes = temp[1];
          points[ID].isNull = false;
          points[ID].id = ID;
          points[ID].message.add(firstMes);
          for(int i=1;i<numberAsString.length;i++) {
            points[ID].message.add(numberAsString[i]);
          }
        }

      }
      reader1.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      BufferedReader reader2 = new BufferedReader(new FileReader(file2));
      String line;
      while((line = reader2.readLine()) != null) {
        String[] numberAsString = line.split("\t");
        /*YAGO*/
//         int ID = Integer.parseInt(numberAsString[3]);
//         points[ID].isNull = false;
//         points[ID].city = numberAsString[0];
//         points[ID].longitude = Double.parseDouble(numberAsString[1]);
//         points[ID].latitude = Double.parseDouble(numberAsString[2]);
//
        /*uk*/
        int ID = Integer.parseInt(numberAsString[0]);
        points[ID].isNull = false;
        points[ID].longitude = Double.parseDouble(numberAsString[2]); //注意经纬度顺序
        points[ID].latitude = Double.parseDouble(numberAsString[1]);

      }
      reader2.close();
    } catch (IOException e) {
      e.printStackTrace();
    }


    //拆分relation2id文件中的关键字，提取最后一个单词作为关键字，并将信息存储到Relationship中
    /*uk*/

    File fileS = new  File("app/src/main/resources/static/data/txt/uk/relationship.txt");
    Relationship relationship = new Relationship();
    try {
      if(!fileS.exists()) {
        fileS.createNewFile();
      }
      BufferedWriter writeRelation = new BufferedWriter(new FileWriter(fileS));
      BufferedReader readRelation = new BufferedReader(new FileReader("app/src/main/resources/static/data/txt/uk/relation2id.txt"));
      String lineOfRelation;
      lineOfRelation = readRelation.readLine();
      String []numOfR = lineOfRelation.split("#");
      int numOfRelation = Integer.parseInt(numOfR[1]);
      relationship.relationNum = numOfRelation;
      relationship.relationKey = new String[numOfRelation];
      int tempN = 0;
      while((lineOfRelation = readRelation.readLine()) != null) {
        String []idString = lineOfRelation.split("\t");
        String temp;
        if(lineOfRelation.indexOf("#") > 0) {
          temp = lineOfRelation.substring(lineOfRelation.lastIndexOf("#") + 1, lineOfRelation.lastIndexOf(">"));
        }
        else {
          temp = lineOfRelation.substring(lineOfRelation.lastIndexOf("/") + 1, lineOfRelation.lastIndexOf(">"));
        }
        temp = temp.toLowerCase(); //全部转为小写字母
        relationship.relationKey[tempN] = temp;
        temp = idString[0] + ":" + temp + "\n";
        //System.out.println(relationship.relationKey[tempN]);
        tempN++;
        writeRelation.write(temp);
      }

      writeRelation.close();
      readRelation.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    /**加 读取Entity名称到顶点中**/
    /*uk*/
    try {
      BufferedReader readEntity = new BufferedReader(new FileReader("app/src/main/resources/static/data/txt/uk/Entity.txt"));
      String lineOfEntity;
      while((lineOfEntity = readEntity.readLine()) != null) {
        String[] numOfE = lineOfEntity.split(":");
        int Eid = Integer.parseInt(numOfE[0]);
        if ( numOfE.length > 1 && points[Eid].isNull == false) {
          points[Eid].name = numOfE[1];
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }


    /*uk*/
    //初始化图的数据，并将图中顶点关系中的关键字添加到顶点关键字中
    Graph graph = new Graph();
    graph.pointNum = total;
    graph.pointIsNull = new boolean[graph.pointNum];
    graph.tag = new int[graph.pointNum];
    /**把5000改为1000**/
    graph.neighbors = new int[graph.pointNum][1000];
    graph.inDegree = new int[graph.pointNum];
    graph.isRoot = new boolean[graph.pointNum];
    for(int zt=0;zt<graph.pointNum;zt++) {
      graph.pointIsNull[zt] = true;
      graph.isRoot[zt] = false;
      graph.tag[zt] = 0;
      graph.inDegree[zt] = 0;
      //graph.neighbors[zt] = new int[total];
    }
    try {
      BufferedReader readTest = new BufferedReader(new FileReader("app/src/main/resources/static/data/txt/uk/train2id.txt")); //测试数据
      String lineOfTest;
      lineOfTest = readTest.readLine();
      while((lineOfTest = readTest.readLine()) != null) {
        String []temp = lineOfTest.split(" ");
        int t1 = Integer.parseInt(temp[0]); //顶点
        int t2 = Integer.parseInt(temp[1]); //邻居
        int t3 = Integer.parseInt(temp[2]); //关系
        graph.pointIsNull[t1] = false;
        graph.neighbors[t1][graph.tag[t1]] = t2;
        graph.tag[t1]++; //邻居个数加1
        graph.inDegree[t2] ++; //邻居的入度加1

        //在邻居的关键字中添加关系关键字
        if(! (points[t2].message.contains(relationship.relationKey[t3]))) {
          points[t2].isNull = false;
          points[t2].id = t2;
          points[t2].message.add(relationship.relationKey[t3]);
          //System.out.println(t2);
        }
      }
      //System.out.println(total);
      readTest.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    /**关键字之间以空格分隔**/
    String[] inputKeywordAsString = inputKeyword.split(" ");
    //初始化
    for(int zt=0;zt<total;zt++) {
      points[zt].hasKey = new boolean[inputKeywordAsString.length];
      for(int z1=0;z1< inputKeywordAsString.length;z1++) {
        points[zt].hasKey[z1] = false;
      }
    }
    //遍历每个输入的关键词，每个点匹配对应的关键词
    for(int k1=0;k1<inputKeywordAsString.length;k1++) {
      for(int zt=0;zt<total;zt++) {
        if(points[zt].message.contains(inputKeywordAsString[k1])) {
          points[zt].hasKey[k1] = true;
        }
      }
    }
    //输出construct m结果
//    for(int zt=0;zt<total;zt++) {
//      if(points[zt].keepKey) {
//        System.out.println(zt + ": " + points[zt].hasKey[0] + " " + points[zt].hasKey[1]);
//      }
//    }


    //只有一个顶点也可能被搜索到
    for(int zt=0;zt<total;zt++) {
      if(points[zt].isNull == false) {
        if(points[zt].latitude == 0 && points[zt].longitude ==0) {

        } else{
          graph.pointIsNull[zt] = false;
        }
      }
    }

    //R树的根节点还有可能是单个地点顶点
    //没有经纬度的不作为R树的根结点
    for(int zt=0;zt<total;zt++) {
      if(graph.pointIsNull[zt] == false && graph.inDegree[zt] == 0) {
        if(points[zt].latitude == 0 && points[zt].longitude == 0 ) { //没有经纬度的点

        } else { //有经纬度的点，其经度或纬度其中一个可能为0
          graph.isRoot[zt] = true;
        }
      }
    }

    //TF-label可达性查询
    //把从根节点开始关键字不可达的根节点排除（不一定要根节点包含关键字，只要根节点往下探索有所有关键字就可以）
    //使用BFS遍历，找到所有可达结点，如果可达结点里面包含关键字序列里面的关键字就成功

    int testTotal = 0; //符合条件的点的个数
    for(int zt=0;zt<total;zt++) {
      if(graph.isRoot[zt]) {
        boolean[] visited = new boolean[total];
        boolean[] CanGet = new boolean[inputKeywordAsString.length];
        for(int zmt=0;zmt<total;zmt++) {
          visited[zmt] = false;
        }
        for(int g=0;g<inputKeywordAsString.length;g++) {
          CanGet[g] = false;
        }
        List<Integer> ContainVector = BFS(zt, visited, graph);

        for(int g=0;g<inputKeywordAsString.length;g++) {
          //时间复杂度较高，不采用
//          for(int k1=0;k1<total;k1++) {
//            if(points[k1].hasKey[g]) {
//              if(ContainVector.contains(k1)) {
//                CanGet[g] = true;
//                break;
//              }
//            }
//          }
          for(int k1=0;k1<ContainVector.size();k1++) {
            int temp = ContainVector.get(k1);
            if(points[temp].hasKey[g]) {
              CanGet[g] = true;
              break;
            }
          }
        }

        boolean flag = false;
        for(int g=0;g<inputKeywordAsString.length;g++) {
          if(CanGet[g] == false)
            flag = true;
        }
        if(flag == false) {
          //计算S(q,p)，可以不使用R树
          Double distance = Math.sqrt((points[zt].latitude - QLatitude) * (points[zt].latitude - QLatitude) + (points[zt].longitude - QLongitude) * (points[zt].longitude - QLongitude));
          Tuple<Integer, Double> tuple = new Tuple<Integer, Double>(zt, distance);

          graph.Sp.add(tuple);
          testTotal ++;
          //System.out.println(zt + " " +graph.tag[zt]);
        } else {
          graph.isRoot[zt] = false; //不包含关键字的顶点不能作为根结点
        }
      }
    }
    /**/
    System.out.println(testTotal);

    // 排序前的结果
//    for(int jt=0;jt<graph.Sp.size();jt++) {
//      Tuple<Integer, Double> ttest = graph.Sp.get(jt);
//      System.out.println(jt + " " + ttest.item1 + " " + ttest.item2);
//    }

    //将Sp根据大小升序排序
    Collections.sort(graph.Sp, new Comparator<Tuple<Integer, Double>>() {
      @Override
      public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2) {
        return o1.item2 > o2.item2 ? 1:-1;
      }
    });

    //排序后的结果
//    for(int jt=0;jt<graph.Sp.size();jt++) {
//      Tuple<Integer, Double> ttest = graph.Sp.get(jt);
//      System.out.println(jt + " " + ttest.item1 + " " + ttest.item2);
//    }


    //在已排序的Sp的结点中计算Lp
    //在对根节点使用BFS的过程中判断访问的顶点是否包含未消去的关键字，逐个消除关键字直到每个关键字
    //未使用剪枝规则2
//    for(int zt=0;zt<graph.Sp.size();zt++) {
//      PlusBFS(graph.Sp.get(zt).item1,graph,points);
//      points[graph.Sp.get(zt).item1].Lp = 0;
//      //System.out.println(graph.Sp.get(zt).item1 + " " + graph.Sp.get(zt).item2);
//      for(int zz=0;zz< inputKeywordAsString.length;zz++) {
//        String kkey = inputKeywordAsString[points[graph.Sp.get(zt).item1].recordK.get(zz).gap1];
//        //System.out.println(kkey + " " + points[graph.Sp.get(zt).item1].recordK.get(zz).gap2 + " " + points[graph.Sp.get(zt).item1].recordK.get(zz).gap3);
//        points[graph.Sp.get(zt).item1].Lp += points[graph.Sp.get(zt).item1].recordK.get(zz).gap3;
//      }
//      points[graph.Sp.get(zt).item1].Fp = points[graph.Sp.get(zt).item1].Lp * graph.Sp.get(zt).item2; //Fp = Lp * Sp;
//
//      Tuple<Integer, Double> tuple = new Tuple<>(graph.Sp.get(zt).item1,points[graph.Sp.get(zt).item1].Fp);
//      graph.FindPoint.add(tuple);
//    }
//    //升序排序
//    Collections.sort(graph.FindPoint, new Comparator<Tuple<Integer, Double>>() {
//      @Override
//      public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2) {
//        return o1.item2 > o2.item2 ? 1:-1;
//      }
//    });
//    if(testTotal <= Qk) {
//      /**加**/
//      JSONObject jsonObject = new JSONObject();
//      jsonObject.put("id", -1);
//      jsonObject.put("lon", 0);
//      jsonObject.put("la", 0);
//      jsonObject.put("finds", testTotal);
//      jsonArray.add(jsonObject);
//
//      System.out.println("The input k is too large, the actual size of k is: " + testTotal);
//    } else {
//      System.out.println("\nResult of the top-" + Qk + " relevant semantic place retrieval is: \n");
//      for(int i=0;i<Qk;i++) {
//        System.out.println("Point: " + graph.FindPoint.get(i).item1 + "\tLongitude: " + points[graph.FindPoint.get(i).item1].longitude + "\tLatitude: " + points[graph.FindPoint.get(i).item1].latitude);
//
//        /**加**/
//        JSONObject jsonObject = new JSONObject();
//        /**改 把id里面的内容变为对应id的name**/
//        jsonObject.put("id", points[graph.FindPoint.get(i).item1].name);
//        jsonObject.put("lon", points[graph.FindPoint.get(i).item1].longitude);
//        jsonObject.put("la", points[graph.FindPoint.get(i).item1].latitude);
//
//        String []finds = new String[inputKeywordAsString.length];
//        for(int j=0;j<inputKeywordAsString.length;j++) {
//          /**改 把finds的结点变为结点的name**/
//          finds[j] = points[points[graph.FindPoint.get(i).item1].recordK.get(j).gap2].name;
//        }
//
//        jsonObject.put("finds", finds);
//
//        jsonArray.add(jsonObject);
//
//      }
//    }

    // System.out.println(testTotal);
    //使用剪枝规则2
    if(testTotal < Qk) {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("id", -1);
      jsonObject.put("lon", 0);
      jsonObject.put("la", 0);
      jsonObject.put("finds", testTotal);
      jsonArray.add(jsonObject);
      /***后台输出结果***/
      System.out.println("The input k is too large, the actual size of k is: " + testTotal);
    } else {
      Vector<Tuple<Integer, Double>> Ks = new Vector<>(); //记录顶点id和Lw(Tp)
      for(int i=0;i<Qk;i++) {
        Ks.add(new Tuple<>(-1,1000000.0));
      }
      for(int zt=0;zt<graph.Sp.size();zt++) {

        boolean tk = PlusPlusBFS(graph.Sp.get(zt).item1,graph,points,Ks,zt);
        if(tk) { //没有被提前剪枝
          points[graph.Sp.get(zt).item1].Lp = 0;
          for(int zz=0;zz< inputKeywordAsString.length;zz++) {
            points[graph.Sp.get(zt).item1].Lp += points[graph.Sp.get(zt).item1].recordK.get(zz).gap3;
            //System.out.println(points[graph.Sp.get(zt).item1].recordK.size() + "!!!!");
          }
          points[graph.Sp.get(zt).item1].Fp = points[graph.Sp.get(zt).item1].Lp * graph.Sp.get(zt).item2; //Fp = Lp * Sp;

          Ks.add(new Tuple<>(graph.Sp.get(zt).item1, points[graph.Sp.get(zt).item1].Fp));
          Collections.sort(Ks, new Comparator<Tuple<Integer, Double>>() {
            @Override
            public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2) {
              return o1.item2 > o2.item2 ? 1:-1;
            }
          }); //加入一个元素后升序排序
          Ks.remove(Qk); //再把最大的删除
        }
      }

      System.out.println(testTotal);

      //输出结果，并将结果转为json形式传给前端
      /***后台输出结果***/
      System.out.println("\nResult of the top-" + Qk + " relevant semantic place retrieval is: \n");

      for(int i=0;i<Qk;i++) {
        //将关键字记录按关键字顺序升序排序
        Collections.sort(points[Ks.get(i).item1].recordK, new Comparator<Triple<Integer, Integer, Integer>>() {
          @Override
          public int compare(Triple<Integer, Integer, Integer> o1, Triple<Integer, Integer, Integer> o2) {
            return o1.gap1 > o2.gap1 ? 1:-1;
          }
        });

        /***后台输出结果***/
        System.out.println("Point: " + points[Ks.get(i).item1].name + "\tLongitude: " + points[Ks.get(i).item1].longitude + "\tLatitude: " + points[Ks.get(i).item1].latitude);

        JSONObject jsonObject = new JSONObject();
        /**改 把id里面的内容变为对应id的name**/
        jsonObject.put("id", points[Ks.get(i).item1].name);
        jsonObject.put("lon", points[Ks.get(i).item1].longitude);
        jsonObject.put("la", points[Ks.get(i).item1].latitude);

        String []finds = new String[inputKeywordAsString.length];
        for(int j=0;j<inputKeywordAsString.length;j++) {
          /**改 把finds的结点变为结点的name**/
          finds[j] = points[ points[Ks.get(i).item1] .recordK.get(j).gap2].name;
        }

        jsonObject.put("finds", finds);

        jsonArray.add(jsonObject);
      }


      //将检索到的结果写入topk.json文件
//      File js1 = new  File("/Users/heguohui/Desktop/spring-boot-tutorial/src/main/resources/static/data/geojson/topk.json");
//      try {
//        if (!js1.exists()) {
//          js1.createNewFile();
//        }
//        BufferedWriter writeMap = new BufferedWriter(new FileWriter(js1));
//        String line;
//        writeMap.write("{\"type\": \"FeatureCollection\", \"features\": [\n");
//        writeMap.flush();
//        for(int i=0;i<Qk;i++) {
//          String temp = "{\"type\": \"Feature\", \"properties\": {\"id\": \"" + Ks.get(i).item1 + "\"}, \"geometry\": {\"type\": \"Point\", \"coordinates\": [" + points[Ks.get(i).item1].longitude + ", " + points[Ks.get(i).item1].latitude + "]}}";
//          if(i != Qk -1) {
//            temp += ",\n";
//          } else {
//            temp += "\n";
//          }
//          writeMap.write(temp);
//          writeMap.flush();
//        }
//        writeMap.write("]}");
//        writeMap.flush();
//        writeMap.close();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }

      //
   }

    System.out.println("finished!");
    return jsonArray;
  }

  public static List<Integer> BFS(int src, boolean[] visited, Graph graph) {
    List<Integer> queue = new ArrayList<>();
    queue.add(src);
    visited[src] = true;
    List<Integer> reachableNodes = new ArrayList<>();
    while(!queue.isEmpty()) {
      int u = queue.remove(0);
      reachableNodes.add(u);
      for(int g=0;g<graph.tag[u];g++) {
        int tempG = graph.neighbors[u][g];
        if(visited[tempG] == false) {
          visited[tempG] = true;
          queue.add(tempG);
        }
      }
    }
    return reachableNodes;
  }

  //未使用剪枝规则2的BFS算法
  public static void PlusBFS(int src, Graph graph, Point []points) {
    boolean []keyVisit = new boolean[points[0].hasKey.length];
    for(int z=0;z<keyVisit.length;z++) //初始化根结点所有关键字都没有被访问过
      keyVisit[z] = false;
    List<Integer> queue = new ArrayList<>();
    queue.add(src);
    int []d = new int[graph.pointNum];
    for(int i=0;i<d.length;i++)
      d[i] = -1;
    d[src] = 1;
    while(!queue.isEmpty()) {
      int u = queue.remove(0);
      for (int z = 0; z < keyVisit.length; z++) {
        if (!keyVisit[z]) {
          if (points[u].hasKey[z]) {
            Triple<Integer, Integer, Integer> triple = new Triple<>(z, u, d[u]);
            points[src].recordK.add(triple);
            keyVisit[z] = true;
          }
        }
      }
      //所有关键字遍历完即可结束
      boolean flag = false;
      for (int z = 0; z < keyVisit.length; z++) {
        if (keyVisit[z] == false)
          flag = true;
      }
      if (!flag)
        return;

      for (int g = 0; g < graph.tag[u]; g++) {
        int temG = graph.neighbors[u][g];
        if (d[temG] == -1) { //如果拓展的顶点没有被搜索过就拓展
          d[temG] = d[u] + 1;
          queue.add(temG);
        }
      }

    }
  }

  //使用剪枝规则2的BFS算法
  public static boolean PlusPlusBFS(int src, Graph graph, Point []points, Vector<Tuple<Integer, Double>> Ks, int pos) {
    boolean []keyVisit = new boolean[points[0].hasKey.length];
    for(int z=0;z<keyVisit.length;z++) //初始化根结点所有关键字都没有被访问过
      keyVisit[z] = false;
    List<Integer> queue = new ArrayList<>();
    queue.add(src);
    int []d = new int[graph.pointNum];
    for(int i=0;i<d.length;i++)
      d[i] = -1;
    d[src] = 1;
    while(!queue.isEmpty()) {
      int u = queue.remove(0);
      //System.out.println(u);
      for (int z = 0; z < keyVisit.length; z++) {
        if (!keyVisit[z]) {
          if (points[u].hasKey[z]) {
            Triple<Integer, Integer, Integer> triple = new Triple<>(z, u, d[u]);
            points[src].recordK.add(triple);
            keyVisit[z] = true;
          }
        }
      }
      //所有关键字遍历完即可结束
      int KeyNum = 0;
      for (int z = 0; z < keyVisit.length; z++) {
        if (keyVisit[z] == false)
          KeyNum++;
      }
      if (KeyNum == 0)
        return true;

      //剪枝规则2
      int LB = d[u] + (d[u]-1) * KeyNum;
      double Lw = (Ks.get(Ks.size()-1).item2 * 1.0) / (graph.Sp.get(pos).item2 * 1.0);
      if (LB > Lw)
      {
        //System.out.println(LB + " " + Lw);
        return false;
      }

      for (int g = 0; g < graph.tag[u]; g++) {
        int temG = graph.neighbors[u][g];
        if (d[temG] == -1) { //如果拓展的顶点没有被搜索过就拓展
          d[temG] = d[u] + 1;
        }
        queue.add(temG);
      }

    }
    return true;
  }
}
