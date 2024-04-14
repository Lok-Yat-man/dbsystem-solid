#! usr/bin/env python
from sys import argv
import os from os.path import exists
import json
import pandas as pd
import numpy as np
import copy
import time
import datetime
from pyproj import Transformer
import urllib3

path = os.path.dirname(os.path.abspath(__file__))

#input_file=json.load(open(path+"/queryhk2.json", "r", encoding="utf-8"))

http = urllib3.PoolManager()
response = http.request('GET', 'https://services8.arcgis.com/PXQv9PaDJHzt8rp0/arcgis/rest/services/StayBuildings_LIM_0827_View/FeatureServer/0/query?f=geojson&returnGeometry=true&spatialRel=esriSpatialRelIntersects&geometry=%7B%22xmin%22%3A12676928.26703362%2C%22ymin%22%3A2501631.06171958%2C%22xmax%22%3A12761314.746260433%2C%22ymax%22%3A2586017.540946392%2C%22spatialReference%22%3A%7B%22wkid%22%3A102100%7D%7D&geometryType=esriGeometryEnvelope&inSR=102100&outFields=*&returnCentroid=false&returnExceededLimitFeatures=false&maxRecordCountFactor=3&outSR=102100&resultType=tile&quantizationParameters=%7B%22mode%22%3A%22view%22%2C%22originPosition%22%3A%22upperLeft%22%2C%22tolerance%22%3A152.87405657031263%2C%22extent%22%3A%7B%22xmin%22%3A12679985.748165026%2C%22ymin%22%3A2504688.542850986%2C%22xmax%22%3A12758257.265129026%2C%22ymax%22%3A2582960.0598149863%2C%22spatialReference%22%3A%7B%22wkid%22%3A102100%7D%7D%7D')

if response.status==200 and len(response.data)>1200000:
    input_file=json.loads(response.data)

    #output_file=open(path+"/data/(0716)geodata_hk.json", "w", encoding="utf-8")
    case_file=path+"/data/cases.csv"

    entry = {
        "name":"",
        "geometry": {
            "type":"MultiPolygon",
            "coordinates": [],
        },
        "type":"Feature",
        "properties":{},
        # "properties":d,
    }

    geojs={
        "type": "FeatureCollection",
        "features":[
        ]  
    }

    transformer = Transformer.from_crs(3857, 4326)

    df = pd.DataFrame(columns=['lat','lon','timestamp'])

    i=0
    for (d) in input_file["features"]:
        i=i+1

        time_string=d["properties"]["LastReportedDate"]
        time_int=time.mktime(datetime.datetime.strptime(time_string, "%d/%m/%Y").timetuple())

        coor1=d["geometry"]["coordinates"]
        easting, northing = coor1[1], coor1[0]
        latitude, longitude = transformer.transform(northing, easting)
        d["geometry"]["coordinates"][0]=copy.deepcopy(longitude)
        d["geometry"]["coordinates"][1]=copy.deepcopy(latitude)

        dict = {'lon':[longitude], 'lat': [latitude], 'timestamp': [time_int]}
        df2=pd.DataFrame(dict)
        df=pd.concat([df, df2], ignore_index=True)

        # entry["properties"]["name"]=d["attributes"]["cname"]
        # entry["properties"]["caddr"]=d["attributes"]["caddr"]
        # entry["properties"]["infected_count"]=d["attributes"]["infected_count"]
        # entry["properties"]["restriction"]=d["attributes"]["restriction"]
        # entry["properties"]["lastupdate"]=d["attributes"]["lastupdate"]
        geojs["features"].append(copy.deepcopy(d))

    #print(geojs)

    df.sort_values(by=['timestamp'], inplace=True)
    df.to_csv(case_file,index=False)

    #json.dump(geojs, output_file)
    #output_file.close()

exit()
