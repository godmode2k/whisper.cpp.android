# whisper.cpp.android with Translation and TTS


Summary
----------
> whisper.cpp.android with CLBlast(OpenCL), Translation (Google ML-Kit) and TTS </br>
> </br>
> WORK IN-PROGRESS


Environment
----------
> build all and tested on GNU/Linux

    GNU/Linux: Ubuntu 20.04_x64 LTS
    Android Studio: android-studio-2023.2.1.24


Test device
----------
> Samsung Galaxy A32 (with CLBlast(OpenCL))


Build
----------
```sh

// Working Path: $HOME/work
$ mkdir $HOME/work && cd $HOME/work

// Install Android Studio, SDK, ...
// Path: /work/android
//
// ADB: /work/android/sdk/platform-tools/adb


// Clone
$ git clone https://github.com/godmode2k/whisper.cpp.android


// Clone whisper.cpp
$ git clone https://github.com/ggerganov/whisper.cpp.git


// Rename origin dir (whisper.cpp/examples/whisper.android)
$ mv ./whisper.cpp/examples/whisper.android ./whisper.cpp/examples/whisper.android.old

// Copy whisper.cpp.android <whisper.cpp path>/examples/
$ cp -a -r ./whisper.cpp.android/whisper.android ./whisper.cpp/examples/whisper.android


---------------------------------
pull libGLES_mali.so file from device
---------------------------------
Note: lib path is for Samsung Galaxy A32


// Android path: /work/android


// SEE: https://github.com/ggerganov/whisper.cpp/tree/master/examples/whisper.android

$ /work/android/sdk/platform-tools/adb pull /system/vendor/lib64/egl/libGLES_mali.so

// rename
$ cp libGLES_mali.so libOpenCL.so

$ mkdir -p ./whisper.cpp/examples/whisper.android/lib/src/main/jniLibs/arm64-v8a
$ cp libOpenCL.so ./whisper.cpp/examples/whisper.android/lib/src/main/jniLibs/arm64-v8a


---------------------------------
OpenCL-Headers, library
---------------------------------
$ git clone https://github.com/KhronosGroup/OpenCL-Headers.git
$ cd OpenCL-Headers

//$ cmake -S . -B build -DCMAKE_INSTALL_PREFIX=/chosen/install/prefix
//$ cmake --build build --target install

$ cmake -S . -B build
$ cmake --build build

// result: USE CL/*.h

$ cd ..

// renamed above (libGLES_mali.so == libOpenCL.so)
$ cp libOpenCL.so ./OpenCL-Headers/


---------------------------------
CBlast
---------------------------------
$ git clone https://github.com/CNugteren/CLBlast.git
$ cd CLBlast
$ mkdir build && cd build

$ /work/android/sdk/cmake/3.22.1/bin/cmake .. \
    -DCMAKE_SYSTEM_NAME=Android \
    -DCMAKE_SYSTEM_VERSION=33 \
    -DCMAKE_ANDROID_ARCH_ABI=arm64-v8a \
    -DCMAKE_ANDROID_NDK=/work/android/sdk/ndk/25.2.9519653 \
    -DCMAKE_ANDROID_STL_TYPE=c++_static \
    -DOPENCL_ROOT=/work/OpenCL-Headers \
    -DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH \
    -DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH \
    -DOPENCL_LIB=/work/libOpenCL.so \
&& make -j4

$ cd ../..


---------------------------------
GGML
---------------------------------
$ git clone https://github.com/ggerganov/ggml.git
$ cd ggml
$ mkdir build && cd build

$ /work/android/sdk/cmake/3.22.1/bin/cmake .. \
    -DGGML_CLBLAST=ON \
    -DCMAKE_SYSTEM_NAME=Android \
    -DCMAKE_SYSTEM_VERSION=33 \
    -DCMAKE_ANDROID_ARCH_ABI=arm64-v8a \
    -DCMAKE_ANDROID_NDK=/work/android/sdk/ndk/25.2.9519653 \
    -DCMAKE_ANDROID_STL_TYPE=c++_shared \
    -DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH \
    -DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH \
    -DCLBLAST_HOME=/work/CLBlast \
    -DOPENCL_LIB=/work/libOpenCL.so \
&& make -j4

$ cd ../..


---------------------------------
whisper.cpp
---------------------------------
$ git clone https://github.com/ggerganov/whisper.cpp.git
$ cd whisper.cpp
$ make

$ cd ..


---------------------------------
copy assets
---------------------------------
// whisper.cpp Models
https://huggingface.co/ggerganov/whisper.cpp

// Download Model: Tiny
// https://huggingface.co/ggerganov/whisper.cpp/blob/main/ggml-tiny.bin
$ mkdir models
$ wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin -P models/


// whisper.cpp models
$ mkdir -p ./whisper.cpp/examples/whisper.android/app/src/main/assets/models
$ cp ./models/ggml-tiny.bin ./whisper.cpp/examples/whisper.android/app/src/main/assets/models

// whisper.cpp samples
$ mkdir -p ./whisper.cpp/examples/whisper.android/app/src/main/assets/samples
$ cp ./whisper.cpp/samples/jfk.wav ./whisper.cpp/examples/whisper.android/app/src/main/assets/samples


// libs (.so)
// NOTE: libs (.so) below is for Samsung Galaxy A32
//
// You should find dependencies (.so).
//
//
// [Option #1]
//
// e.g., 'libGLES_mali.so' first
//
// $ adb pull /system/vendor/lib64/egl/libGLES_mali.so
// $ readelf -d libGLES_mali.so
// Dynamic section at offset 0x2537748 contains 42 entries:
//   Tag        Type                         Name/Value
//  0x0000000000000001 (NEEDED)             Shared library: [liblog.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libz.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libnativewindow.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libged.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libgpu_aux.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libgpud.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libgralloc_extra.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libcutils.so]
//  0x0000000000000001 (NEEDED)             Shared library: [android.hardware.graphics.mapper@4.0.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libgralloctypes.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libhidlbase.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libutils.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libm.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libc.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libdl.so]
//  0x0000000000000001 (NEEDED)             Shared library: [libc++.so]
//  0x000000000000000e (SONAME)             Library soname: [libGLES_mali.so]
//  ...
//
// then copy (pull) the (NEEDED) .so files.
// $ mkdir libs
// $ adb pull <path>/.so ./libs
// You have to run 'readelf' for these (NEEDED) .so files again and copy (pull) it.
//
// or
//
//
// [Option #2]
// Easy, but size is too large. I cannot recommend this approach.
//
// copy all libs (*.so)
//
// (Android device)/system/vendor/lib64/*.so
// (Android device)/system/vendor/lib64/egl/libGLES_mali.so
// (Android device)/system/lib64/*.so
//
//
//
// Below instruction is for [Option #1]
//
// NOTE: libs (.so) below is for Samsung Galaxy A32
//
$ mkdir libs
$ adb pull /system/vendor/lib64/egl/libGLES_mali.so ./libs
$ adb pull /system/vendor/lib64/libged.so ./libs
$ adb pull /system/vendor/lib64/libgpu_aux.so ./libs
$ adb pull /system/vendor/lib64/libgpud.so ./libs
$ adb pull /system/vendor/lib64/libgralloc_extra.so ./libs
$ adb pull /system/vendor/lib64/libdpframework.so ./libs
$ adb pull /system/vendor/lib64/vendor.mediatek.hardware.mms@1.5.so ./libs
$ adb pull /system/vendor/lib64/libion_mtk.so ./libs
$ adb pull /system/vendor/lib64/libpq_prot.so ./libs
$ adb pull /system/vendor/lib64/vendor.mediatek.hardware.mms@1.0.so ./libs
$ adb pull /system/vendor/lib64/vendor.mediatek.hardware.mms@1.1.so ./libs
$ adb pull /system/vendor/lib64/vendor.mediatek.hardware.mms@1.2.so ./libs
$ adb pull /system/vendor/lib64/vendor.mediatek.hardware.mms@1.3.so ./libs
$ adb pull /system/vendor/lib64/vendor.mediatek.hardware.mms@1.4.so ./libs
$ adb pull /system/vendor/lib64/libion_ulit.so ./libs
$ adb pull /system/vendor/lib64/libladder.so ./libs
//
$ adb pull /system/lib64/libcutils.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.mapper@4.0.so ./libs
$ adb pull /system/lib64/libgralloctypes.so ./libs
$ adb pull /system/lib64/libhidlbase.so ./libs
$ adb pull /system/lib64/libutils.so ./libs
$ adb pull /system/lib64/libc++.so ./libs
$ adb pull /system/lib64/libhardware.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.common@1.0.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.common@1.1.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.common@1.2.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.mapper@2.0.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.mapper@2.1.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.mapper@3.0.so ./libs
$ adb pull /system/lib64/libion.so ./libs
$ adb pull /system/lib64/libdmabufheap.so ./libs
$ adb pull /system/lib64/libutilscallstack.so ./libs
$ adb pull /system/lib64/libbase.so ./libs
$ adb pull /system/lib64/android.hardware.graphics.common-V3-ndk.so ./libs
$ adb pull /system/lib64/libvndksupport.so ./libs
$ adb pull /system/lib64/libhidlmemory.so ./libs
$ adb pull /system/lib64/libbacktrace.so ./libs
$ adb pull /system/lib64/android.hardware.common-V2-ndk.so ./libs
$ adb pull /system/lib64/libdl_android.so ./libs
$ adb pull /system/lib64/android.hidl.memory@1.0.so ./libs
$ adb pull /system/lib64/android.hidl.memory.token@1.0.so ./libs
$ adb pull /system/lib64/libunwindstack.so ./libs
$ adb pull /system/lib64/ld-android.so ./libs
$ adb pull /system/lib64/liblzma.so ./libs
//
$ cp ./libs/*.so ./whisper.cpp/examples/whisper.android/lib/src/main/jniLibs/arm64-v8a/


---------------------------------
build whisper.android
---------------------------------
// Android Studio: gradle.properties
//
GGML_HOME=/work/ggml
GGML_CLBLAST=ON
CLBLAST_HOME=/work/CLBlast
OPENCL_LIB=/work/libOpenCL.so
OPENCL_ROOT=/work/OpenCL-Headers


// Android Studio: build.gradle (:lib)
// Use only here: "arm64-v8a"
//
ndk {
    //abiFilters "arm64-v8a", "armeabi-v7a", "x86", "x86_64
    abiFilters "arm64-v8a"
}


// comment-out
// issues:
//  - ld: error: unknown argument "--copy-dt-needed-entries"
//
// (EDIT) /work/ggml/src/CMakeLists.txt:202
//
$ vim /work/ggml/src/CMakeLists.txt +202
(Before)
        link_libraries("-Wl,--copy-dt-needed-entries")
(After: comment-out)
#        link_libraries("-Wl,--copy-dt-needed-entries")


// Android Studio
clean & rebuild & run...


Run in Release mode is more faster.
```


Screenshots
----------

> UI #1: Debug mode </br>
<img src="https://github.com/godmode2k/whisper.cpp.android/raw/main/screenshots/screenshot1_debug_mode.jpg" width="50%" height="50%">


> UI #2: Release mode (more faster) </br>
<img src="https://github.com/godmode2k/whisper.cpp.android/raw/main/screenshots/screenshot2_release_mode.jpg" width="50%" height="50%">


> UI #3: Translation Supported Languages </br>
<img src="https://github.com/godmode2k/whisper.cpp.android/raw/main/screenshots/screenshot3_translation_supported_langs.jpg" width="50%" height="50%">



