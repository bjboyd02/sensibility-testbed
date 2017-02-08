# Building Python

This document briefly explains how to build Python 2.7 for Android using
@kivy's `python-for-android` project, https://github.com/kivy/python-for-android


## Why do we need this?

This repository, `sensibility-testbed`, does not build the required Python
2.7 itself at the moment. Instead, [as indicated here](https://github.com/aaaaalbert/sensibility-testbed/commit/9322af3bc947bb10d8c79ef27536ed7663792b77),
the build process relies on an *externally built* `libpython2.7.so` and
Python header files into the `distribution` directory. Also, the Python
libraries are needed as resources in the `app/src/main/res/raw` folder.


## What build outcomes are used where?

The header files are required for linking our JNI code (which `#include`s
the Python headers). The shared object contains the actual binary code
that will be called for CPython functions used in our JNI code. The Python
libraries contain modules that are imported throughout our Python code.


## How does building work?

In a nutshell,

* Build the Kivy sample app: `p4a apk --arch=armeabi-v7a --private=/home/lukas/kivy-app --package=org.example.myapp --name "My app" --version 0.1 --requirements=python2,libffi --sdk_dir=$HOME/Android/Sdk --ndk_dir=$HOME/android-ndk-r13b --android_api=21 --ndk_ver=r13b`
 * Obviously, YMMV depending on your SDK and NDK versions and path, target arch, etc.
 * See also the [Kivy quick-start guide](https://python-for-android.readthedocs.io/en/latest/quickstart/).
* Copy `~/.local/share/python-for-android/dists/unnamed_dist_1/libs/armeabi-v7a/libpython2.7.so` over
  to `sensibility-testbed/distribution/python27/lib/armeabi-v7a`.
 * The paths below `lib/` vary depending on the build architecture!
* Move the contents of the local build subdirectory,
  `~/.local/share/python-for-android/dists/unnamed_dist_1/private/lib`,
  to a temporary directory named `python/lib`.
* `zip` up that directory into `python_lib.zip`.
* Move `python_lib.zip` to the `sensibility-testbed/app/src/main/res/raw` folder.
* Move the header files in `~/.local/share/python-for-android/dists/unnamed_dist_1/python-install/include/python2.7/`
  over to `sensibility-testbed/distribution/python27/include`.



-----

Build shell log:
```
lukas@seb-desktop:~$ p4a apk --arch=armeabi-v7a --private=/home/lukas/kivy-app --package=org.example.myapp --name "My app" --version 0.1 --requirements=python2,libffi --sdk_dir=$HOME/Android/Sdk --ndk_dir=$HOME/android-ndk-r13b --android_api=21 --ndk_ver=r13b
[INFO]:    This python-for-android revamp is an experimental alpha release!
[INFO]:    It should work (mostly), but you may experience missing features or bugs.
[INFO]:    Will compile for the following archs: armeabi-v7a
[INFO]:    Getting Android API version from user argument
[INFO]:    Available Android APIs are (21, 22, 23, 24)
[INFO]:    Requested API target 21 is available, continuing.
[INFO]:    Getting NDK dir from from user argument
[INFO]:    Got NDK version from from user argument
[INFO]:    Using Google NDK r13b
[INFO]:    Found virtualenv at /usr/local/bin/virtualenv
[INFO]:    Found the following toolchain versions: ['4.9']
[INFO]:    Picking the latest gcc toolchain, here 4.9
[INFO]:    Of the existing distributions, the following meet the given requirements:
[INFO]:    	unnamed_dist_1: includes recipes (hostpython2, libffi, sdl2_image, sdl2_mixer, sdl2_ttf, python2, sdl2), built for archs (armeabi-v7a)
[INFO]:    unnamed_dist_1 has compatible recipes, using this one
[INFO]:    Of the existing distributions, the following meet the given requirements:
[INFO]:    	unnamed_dist_1: includes recipes (hostpython2, libffi, sdl2_image, sdl2_mixer, sdl2_ttf, python2, sdl2), built for archs (armeabi-v7a)
[INFO]:    unnamed_dist_1 has compatible recipes, using this one
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    -> running ant debug
[INFO]:    <- directory context /home/lukas                                    
[INFO]:    # Copying APK to current directory
[INFO]:    # Found APK file: /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/bin/Myapp-0.1-debug.apk
[INFO]:    -> running cp /home/lukas/.local/share/py...(and 64 more)
lukas@seb-desktop:~$ ls -l /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/bin/Myapp-0.1-debug.apk
-rw-rw-r-- 1 lukas lukas 5262585 Feb  8 06:35 /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/bin/Myapp-0.1-debug.apk
lukas@seb-desktop:~$ aapt
No command 'aapt' found, did you mean:
 Command 'apt' from package 'apt' (main)
 Command 'xapt' from package 'xapt' (universe)
aapt: command not found
lukas@seb-desktop:~$ cp /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/bin/Myapp-0.1-debug.apk /tmp
lukas@seb-desktop:~$ cd /tmp
lukas@seb-desktop:/tmp$ unzip Myapp-0.1-debug.apk 
Archive:  Myapp-0.1-debug.apk
  inflating: AndroidManifest.xml     
 extracting: assets/private.mp3      
 extracting: res/drawable-hdpi-v4/ic_launcher.png  
 extracting: res/drawable-mdpi-v4/ic_launcher.png  
 extracting: res/drawable-xhdpi-v4/ic_launcher.png  
 extracting: res/drawable-xxhdpi-v4/ic_launcher.png  
 extracting: res/drawable/icon.png   
 extracting: res/drawable/presplash.jpg  
  inflating: res/layout/main.xml     
 extracting: resources.arsc          
  inflating: classes.dex             
  inflating: lib/armeabi-v7a/libSDL2_ttf.so  
  inflating: lib/armeabi-v7a/libmain.so  
  inflating: lib/armeabi-v7a/libpython2.7.so  
  inflating: lib/armeabi-v7a/libSDL2.so  
  inflating: lib/armeabi-v7a/libSDL2_image.so  
  inflating: lib/armeabi-v7a/libffi.so  
  inflating: lib/armeabi-v7a/libSDL2_mixer.so  
  inflating: META-INF/MANIFEST.MF    
  inflating: META-INF/CERT.SF        
  inflating: META-INF/CERT.RSA       
  ```
