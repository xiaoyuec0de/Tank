//
// Created by moon on 2023/1/9.
//

#ifndef TANK_LOG_H
#define TANK_LOG_H

#include <android/log.h>

#define LOG(...) do{__android_log_print(ANDROID_LOG_INFO,"TANK",__VA_ARGS__);}while(0)

#endif //TANK_LOG_H
