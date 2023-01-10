cmake_minimum_required(VERSION 3.4.1)

project(sample)

add_library( sample SHARED sample.c test_jni.c)

#链接目标库
target_link_libraries( sample)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
