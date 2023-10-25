import pandas as pd
from datetime import datetime
import sys


def main(argv):
    in_file = argv[1]
    out_file = argv[2]
    df = pd.read_csv(in_file)
    df.sort_values(by='timestamp')

    df['timestamp'] = df['timestamp'].astype(int)

    st = df['timestamp'][0]
    df['index'] = ((df['timestamp']-st) //(24*60*60)).astype(int)


    tmp = 0
    index = [0]
    for i in range(len(df['index'])):
        if df['index'][i] != tmp:
            index.append(i)
            tmp = df['index'][i]

    index.append(len(df['index'])-1)

    with open('date_range.js', 'w') as f:
    #Date index
        f.write('export const date_to_index = %s\n'%str(index))
    #Start Date
        f.write('export const st_date = %d\n'%(st))
    #Max Date
        f.write('export const max_date = %d'%(len(index)-1))

    df = df[['lon','lat','index']]
    df.to_csv(out_file,index=False)

if __name__ == '__main__':
    main(sys.argv)
