cmake_minimum_required(VERSION 3.4.1)

find_library(
        log-lib
        log)

include_directories(${CMAKE_SOURCE_DIR}/../../../includes)

add_library(tflite SHARED IMPORTED)
set_target_properties(tflite PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libs/libtensorflowlite.so)
add_library(tflite_gpu SHARED IMPORTED)
set_target_properties(tflite_gpu PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libs/libtensorflowlite_gpu_delegate.so)

add_library(libopencv_core SHARED IMPORTED)
set_target_properties(libopencv_core PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libs/libopencv_core.so)

add_library(libopencv_imgcodecs SHARED IMPORTED)
set_target_properties(libopencv_imgcodecs PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libs/libopencv_imgcodecs.so)

add_library(libopencv_imgproc SHARED IMPORTED)
set_target_properties(libopencv_imgproc PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libs/libopencv_imgproc.so)

add_library(libopencv INTERFACE)
target_link_libraries(libopencv INTERFACE libopencv_core libopencv_imgcodecs libopencv_imgproc)

add_library(autoexp SHARED jni_interface.cpp autoexp.cpp)

target_link_libraries(
    autoexp
    tflite
    tflite_gpu
    libopencv
    android
    ${log-lib}
    log
)
