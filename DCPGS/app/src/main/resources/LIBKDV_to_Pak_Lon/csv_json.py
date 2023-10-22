import pandas as pd

df = pd.read_csv('result.csv')

df.to_json('csvjson.json',orient = "records")