#include "Util.h"
void GPS_to_xy(double longitude,double latitude,double &x,double &y,statistics& stat)
{
    double mid_lat;
    longitude = longitude * pi / 180;
	latitude=latitude*pi/180;
    mid_lat = stat.middle_lat * pi/ 180;
    x = earth_radius * longitude * cos( mid_lat * 2 * pi / 360);
	y=earth_radius*latitude;
}

void xy_to_GPS(double x,double y,double& longitude,double& latitude,statistics& stat)
{
	double mid_lat;
	mid_lat=stat.middle_lat*pi/180;
	longitude =(x/(earth_radius*cos(mid_lat*2*pi/360)))*(180/pi);
	latitude=(y/earth_radius)*(180/pi);
}