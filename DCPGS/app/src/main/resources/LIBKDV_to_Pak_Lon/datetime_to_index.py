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
    max_index = max(df['index'])
    first_date_ed = 0
    last_date_st = 0

    for i in range(len(df['index'])):
        if df['index'][i]==1:
            first_date_ed=i
            break

    for i in reversed(range(len(df['index']))):
        if df['index'][i]!=max_index:
            last_date_st=i+1
            break


    tmp = 0
    index = [0]

    for i in range(len(df['index'])):
        if df['index'][i] != tmp:
            for j in range (df['index'][i]-tmp-1):
                index.append(index[-1])
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


    repeated_first_date = df.iloc[:first_date_ed,].copy()
    repeated_end_date = df.iloc[last_date_st:,].copy()
    repeated_end_date['index'] += 7+1
    for i in range (6):
        tmp = df.iloc[:first_date_ed,].copy()
        tmp['index'] += i+1
        repeated_first_date = pd.concat([repeated_first_date,tmp],ignore_index=True)

        tmp = df.iloc[last_date_st:,].copy()
        tmp['index'] += i+1+7+1
        repeated_end_date = pd.concat([repeated_end_date,tmp],ignore_index=True)
    df['index']+=7
    df = pd.concat([repeated_first_date,df,repeated_end_date],ignore_index=True)


    df = df[['lon','lat','index']]
    df.to_csv(out_file,index=False)

if __name__ == '__main__':
    main(sys.argv)
