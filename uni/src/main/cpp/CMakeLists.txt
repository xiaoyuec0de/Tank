cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_STANDARD 11)

project(uni C)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/unicorn)

include_directories(unicorn/include)


#添加动态库，包含源文件路径
add_library( unicorn_java SHARED unicorn_Unicorn.c)

#查找系统库
find_library(log-lib log)
#链接目标库
target_link_libraries( unicorn_java unicorn ${log-lib} )

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

