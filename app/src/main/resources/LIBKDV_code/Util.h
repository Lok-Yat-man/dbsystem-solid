#pragma once
#ifndef UTIL_H
#define UTIL_H
#include "init_visual.h"
double x_mid,y_mid;
void GPS_to_xy(double longitude, double latitude,double &x,double &y,statistics& stat);
void xy_to_GPS(double x,double y,double& longitude,double& latitude,statistics& stat);

#endif