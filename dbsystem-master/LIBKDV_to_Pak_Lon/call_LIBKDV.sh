#Compile the code
g++ -c init_visual.cpp -w -o init_visual.o
g++ -c baseline.cpp -w -o baseline.o
g++ -c SLAM.cpp -w -o SLAM.o
g++ -c SWS.cpp -w -o SWS.o
g++ -c bucket.cpp -w -o bucket.o
g++ -c EDWIN_otf.cpp -w -o EDWIN_otf.o
g++ -c EDWIN_multiple.cpp -w -o EDWIN_multiple.o
g++ -c alg_visual.cpp -w -o alg_visual.o

g++ main.cpp -O3 -o main init_visual.o baseline.o SLAM.o SWS.o bucket.o EDWIN_otf.o EDWIN_multiple.o alg_visual.o

#Parameters
#stat.dataFileName_CSV = argv[1];
#stat.KDV_type = atoi(argv[2]);
#stat.num_threads = atoi(argv[3]);
#stat.x_L = atof(argv[4]);
#stat.x_U = atof(argv[5]);
#stat.y_L = atof(argv[6]);
#stat.y_U = atof(argv[7]);
#stat.row_pixels = atoi(argv[8]);
#stat.col_pixels = atoi(argv[9]);
#stat.kernel_s_type = atoi(argv[10]);
#stat.bandwidth_s = atof(argv[11]);

#For batch-based STKDV (stat.KDV_type = 3)
#Additional parameters
#stat.t_L = atof(argv[12]);
#stat.t_U = atof(argv[13]);
#stat.t_pixels = atoi(argv[14]);
#stat.kernel_t_type = atoi(argv[15]);
#stat.bandwidth_t = atof(argv[16]);

#For Exploratory-based STKDV (stat.KDV_type = 2) Needs to provide a single timestamp in the variable stat.cur_time
#stat.t_L = atof(argv[12]);
#stat.t_U = atof(argv[13]);
#stat.kernel_t_type = atoi(argv[14]);
#stat.bandwidth_t = atof(argv[15]);
#stat.cur_time = atof(argv[16]);

#./main ./Datasets/Macau.csv 1 1 22.13 22.2 113.541 113.562 256 256 1 0.8
#./main ./Datasets/cases.csv 3 1 113.5222 113.6022 22.1036 22.2195 256 256 1 0.0001 1 30 30 1 1