cd ..
emcc -lembind -o target/kdvCpp.js alg_visual.cpp baseline.cpp bucket.cpp EDWIN_multiple.cpp EDWIN_otf.cpp init_visual.cpp main.cpp SLAM.cpp SWS.cpp -sEXIT_RUNTIME --preload-file data/cases.csv