@echo off
"C:\\Users\\karnv\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HD:\\SSD Data Backup\\Desktop\\TLC App Extrapolate\\TLC-Analyzer\\openCV\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=21" ^
  "-DANDROID_PLATFORM=android-21" ^
  "-DANDROID_ABI=x86_64" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86_64" ^
  "-DANDROID_NDK=C:\\Users\\karnv\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\karnv\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\karnv\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\karnv\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=D:\\SSD Data Backup\\Desktop\\TLC App Extrapolate\\TLC-Analyzer\\openCV\\build\\intermediates\\cxx\\Debug\\2h115o65\\obj\\x86_64" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=D:\\SSD Data Backup\\Desktop\\TLC App Extrapolate\\TLC-Analyzer\\openCV\\build\\intermediates\\cxx\\Debug\\2h115o65\\obj\\x86_64" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BD:\\SSD Data Backup\\Desktop\\TLC App Extrapolate\\TLC-Analyzer\\openCV\\.cxx\\Debug\\2h115o65\\x86_64" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"