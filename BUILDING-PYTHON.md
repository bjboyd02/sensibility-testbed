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
 * There is an un`strip`ped `.so` available too. It includes debug symbols,
   but is much larger: `~/.local/share/python-for-android/dists/unnamed_dist_1/python-install/lib/libpython2.7.so`.
   (`readelf -a` and look at the section headers for details.)
* Move the contents of the local build subdirectory,
  `~/.local/share/python-for-android/dists/unnamed_dist_1/private/lib`,
  to a temporary directory named `python/lib`.
* `zip` up that directory into `python_lib.zip`.
* Move `python_lib.zip` to the `sensibility-testbed/app/src/main/res/raw` folder.
* Move the header files in `~/.local/share/python-for-android/dists/unnamed_dist_1/python-install/include/python2.7/`
  over to `sensibility-testbed/distribution/python27/include`.



-----

Once you have all the toolchains installed, building python-for-android takes
around five minutes, including downloading the dependencies. Shell log:
```
lukas@seb-desktop:~$ p4a apk --arch=armeabi-v7a --private=/home/lukas/kivy-app --package=org.example.myapp --name "My app" --version 0.2 --requirements=python2,libffi --sdk_dir=$HOME/Android/Sdk --ndk_dir=$HOME/android-ndk-r13b --android_api=21 --ndk_ver=r13b
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
[INFO]:    No existing dists meet the given requirements!
[INFO]:    No dist exists that meets your requirements, so one will be built.
[INFO]:    Loaded recipe libffi (depends on [])
[INFO]:    Loaded recipe python2 (depends on ['hostpython2'], conflicts ['python3crystax', 'python3'])
[INFO]:    Loaded recipe hostpython2 (depends on [], conflicts ['hostpython3'])
[INFO]:    Found a single valid recipe set (this is good)
[INFO]:    Trying to find a bootstrap that matches the given recipes.
[INFO]:    Found 4 acceptable bootstraps: ['sdl2', 'pygame', 'plain', 'webview']
[INFO]:    Using the first of these: sdl2
[INFO]:    sdl2 bootstrap appears compatible with the required recipes.
[INFO]:    Checking this...
[INFO]:    Loaded recipe sdl2 (depends on [('python2', 'python3crystax'), 'sdl2_image', 'sdl2_mixer', 'sdl2_ttf'], conflicts ['sdl', 'pygame', 'pygame_bootstrap_components'])
[INFO]:    Loaded recipe sdl2_image (depends on [])
[INFO]:    Loaded recipe sdl2_mixer (depends on [])
[INFO]:    Loaded recipe sdl2_ttf (depends on [])
[INFO]:    Loaded recipe sdl2 (depends on [('python2', 'python3crystax'), 'sdl2_image', 'sdl2_mixer', 'sdl2_ttf'], conflicts ['sdl', 'pygame', 'pygame_bootstrap_components'])
[INFO]:    Loaded recipe sdl2_ttf (depends on [])
[INFO]:    Loaded recipe hostpython2 (depends on [], conflicts ['hostpython3'])
[INFO]:    Loaded recipe sdl2_mixer (depends on [])
[INFO]:    Loaded recipe sdl2_image (depends on [])
[INFO]:    Loaded recipe libffi (depends on [])
[INFO]:    Loaded recipe python2 (depends on ['hostpython2'], conflicts ['python3crystax', 'python3'])
[INFO]:    Found a single valid recipe set (this is good)
[INFO]:    The selected bootstrap is sdl2
[INFO]:    # Creating dist with sdl2 bootstrap
[INFO]:    Dist will have name unnamed_dist_1 and recipes (python2, libffi)
[INFO]:    -> running cp -r /usr/local/lib/python2.7...(and 125 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/bootstrap_builds/sdl2
[INFO]:    <- directory context /home/lukas
[INFO]:    Recipe build order is ['hostpython2', u'libffi', 'sdl2_image', 'sdl2_mixer', 'sdl2_ttf', 'python2', 'sdl2']
[INFO]:    # Downloading recipes 
[INFO]:    Downloading hostpython2
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 42 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/hostpython2
[INFO]:    -> running basename http://python.org/ftp/...(and 33 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading hostpython2 from http://python.org/ftp/python/2.7.2/Python-2.7.2.tar.bz2
[INFO]:    -> running rm -f .mark-Python-2.7.2.tar.bz2
[INFO]:    Downloading hostpython2 from http://python.org/ftp/python/2.7.2/Python-2.7.2.tar.bz2
[INFO]:    -> running touch .mark-Python-2.7.2.tar.bz2
[INFO]:    <- directory context /home/lukas
[INFO]:    Downloading libffi
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 37 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/libffi
[INFO]:    -> running basename https://github.com/atg...(and 30 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading libffi from https://github.com/atgreen/libffi/archive/v3.2.1.zip
[INFO]:    -> running rm -f .mark-v3.2.1.zip
[INFO]:    Downloading libffi from https://github.com/atgreen/libffi/archive/v3.2.1.zip
[INFO]:    -> running touch .mark-v3.2.1.zip
[INFO]:    <- directory context /home/lukas
[INFO]:    Downloading sdl2_image
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 41 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/sdl2_image
[INFO]:    -> running basename https://www.libsdl.or...(and 52 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading sdl2_image from https://www.libsdl.org/projects/SDL_image/release/SDL2_image-2.0.1.tar.gz
[INFO]:    -> running rm -f .mark-SDL2_image-2.0.1.tar.gz
[INFO]:    Downloading sdl2_image from https://www.libsdl.org/projects/SDL_image/release/SDL2_image-2.0.1.tar.gz
[INFO]:    -> running touch .mark-SDL2_image-2.0.1.tar.gz
[INFO]:    <- directory context /home/lukas
[INFO]:    Downloading sdl2_mixer
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 41 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/sdl2_mixer
[INFO]:    -> running basename https://www.libsdl.or...(and 52 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading sdl2_mixer from https://www.libsdl.org/projects/SDL_mixer/release/SDL2_mixer-2.0.1.tar.gz
[INFO]:    -> running rm -f .mark-SDL2_mixer-2.0.1.tar.gz
[INFO]:    Downloading sdl2_mixer from https://www.libsdl.org/projects/SDL_mixer/release/SDL2_mixer-2.0.1.tar.gz
[INFO]:    -> running touch .mark-SDL2_mixer-2.0.1.tar.gz
[INFO]:    <- directory context /home/lukas
[INFO]:    Downloading sdl2_ttf
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 39 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/sdl2_ttf
[INFO]:    -> running basename https://www.libsdl.org...(and 48 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading sdl2_ttf from https://www.libsdl.org/projects/SDL_ttf/release/SDL2_ttf-2.0.14.tar.gz
[INFO]:    -> running rm -f .mark-SDL2_ttf-2.0.14.tar.gz
[INFO]:    Downloading sdl2_ttf from https://www.libsdl.org/projects/SDL_ttf/release/SDL2_ttf-2.0.14.tar.gz
[INFO]:    -> running touch .mark-SDL2_ttf-2.0.14.tar.gz
[INFO]:    <- directory context /home/lukas
[INFO]:    Downloading python2
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 38 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/python2
[INFO]:    -> running basename http://python.org/ftp/...(and 33 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading python2 from http://python.org/ftp/python/2.7.2/Python-2.7.2.tar.bz2
[INFO]:    -> running rm -f .mark-Python-2.7.2.tar.bz2
[INFO]:    Downloading python2 from http://python.org/ftp/python/2.7.2/Python-2.7.2.tar.bz2
[INFO]:    -> running touch .mark-Python-2.7.2.tar.bz2
[INFO]:    <- directory context /home/lukas
[INFO]:    Downloading sdl2
[INFO]:    -> running mkdir -p /home/lukas/.local/sha...(and 35 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/packages/sdl2
[INFO]:    -> running basename https://www.libsdl.org...(and 26 more)
[WARNING]: Should check headers here! Skipping for now.                        
Downloading sdl2 from https://www.libsdl.org/release/SDL2-2.0.4.tar.gz
[INFO]:    -> running rm -f .mark-SDL2-2.0.4.tar.gz
[INFO]:    Downloading sdl2 from https://www.libsdl.org/release/SDL2-2.0.4.tar.gz
[INFO]:    -> running touch .mark-SDL2-2.0.4.tar.gz
[INFO]:    <- directory context /home/lukas
[INFO]:    # Building all recipes for arch armeabi-v7a
[INFO]:    # Unpacking recipes
[INFO]:    Unpacking hostpython2 for armeabi-v7a
[INFO]:    -> running basename http://python.org/ftp/...(and 33 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/hostpython2/desktop
[INFO]:    -> running tar tf /home/lukas/.local/shar...(and 62 more)
[INFO]:    -> running mv Python-2.7.2 /home/lukas/.l...(and 80 more)           
[INFO]:    <- directory context /home/lukas
[INFO]:    Unpacking libffi for armeabi-v7a
[INFO]:    -> running basename https://github.com/atg...(and 30 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/libffi/armeabi-v7a
[INFO]:    -> running mv libffi-3.2.1 /home/lukas/.l...(and 74 more)
[INFO]:    <- directory context /home/lukas
[INFO]:    Unpacking sdl2_image for armeabi-v7a
[INFO]:    -> running basename https://www.libsdl.or...(and 52 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/bootstrap_builds/sdl2/jni
[INFO]:    -> running tar tf /home/lukas/.local/shar...(and 64 more)
[INFO]:    -> running mv SDL2_image-2.0.1 /home/luka...(and 76 more)           
[INFO]:    <- directory context /home/lukas
[INFO]:    Unpacking sdl2_mixer for armeabi-v7a
[INFO]:    -> running basename https://www.libsdl.or...(and 52 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/bootstrap_builds/sdl2/jni
[INFO]:    -> running tar tf /home/lukas/.local/shar...(and 64 more)
[INFO]:    -> running mv SDL2_mixer-2.0.1 /home/luka...(and 76 more)           
[INFO]:    <- directory context /home/lukas
[INFO]:    Unpacking sdl2_ttf for armeabi-v7a
[INFO]:    -> running basename https://www.libsdl.org...(and 48 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/bootstrap_builds/sdl2/jni
[INFO]:    -> running tar tf /home/lukas/.local/shar...(and 61 more)
[INFO]:    -> running mv SDL2_ttf-2.0.14 /home/lukas...(and 73 more)           
[INFO]:    <- directory context /home/lukas
[INFO]:    Unpacking python2 for armeabi-v7a
[INFO]:    -> running basename http://python.org/ftp/...(and 33 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/python2/armeabi-v7a
[INFO]:    -> running tar tf /home/lukas/.local/shar...(and 58 more)
[INFO]:    -> running mv Python-2.7.2 /home/lukas/.l...(and 76 more)           
[INFO]:    <- directory context /home/lukas
[INFO]:    Unpacking sdl2 for armeabi-v7a
[INFO]:    -> running basename https://www.libsdl.org...(and 26 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/bootstrap_builds/sdl2/jni
[INFO]:    -> running tar tf /home/lukas/.local/shar...(and 52 more)
[INFO]:    -> running mv SDL2-2.0.4 /home/lukas/.loc...(and 63 more)           
[INFO]:    <- directory context /home/lukas
[INFO]:    # Prebuilding recipes
[INFO]:    Prebuilding hostpython2 for armeabi-v7a
[INFO]:    -> running cp /usr/local/lib/python2.7/di...(and 163 more)
[INFO]:    Prebuilding libffi for armeabi-v7a
[INFO]:    libffi has no prebuild_armeabi_v7a, skipping
[INFO]:    Applying patches for libffi[armeabi-v7a]
[INFO]:    Applying patch remove-version-info.patch
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 174 more)
[INFO]:    -> running touch /home/lukas/.local/share...(and 73 more)           
[INFO]:    Prebuilding sdl2_image for armeabi-v7a
[INFO]:    sdl2_image has no prebuild_armeabi_v7a, skipping
[INFO]:    Applying patches for sdl2_image[armeabi-v7a]
[INFO]:    Applying patch toggle_jpg_png_webp.patch
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 176 more)
[INFO]:    Applying patch extra_cflags.patch                                   
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 169 more)
[INFO]:    -> running touch /home/lukas/.local/share...(and 71 more)           
[INFO]:    Prebuilding sdl2_mixer for armeabi-v7a
[INFO]:    sdl2_mixer has no prebuild_armeabi_v7a, skipping
[INFO]:    Applying patches for sdl2_mixer[armeabi-v7a]
[INFO]:    Applying patch toggle_modplug_mikmod_smpeg_ogg.patch
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 188 more)
[INFO]:    -> running touch /home/lukas/.local/share...(and 71 more)           
[INFO]:    Prebuilding sdl2_ttf for armeabi-v7a
[INFO]:    sdl2_ttf has no prebuild_armeabi_v7a, skipping
[INFO]:    Prebuilding python2 for armeabi-v7a
[INFO]:    python2 has no prebuild_armeabi_v7a, skipping
[INFO]:    Applying patches for python2[armeabi-v7a]
[INFO]:    Applying patch patches/Python-2.7.2-xcompile.patch
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 187 more)
[INFO]:    Applying patch patches/Python-2.7.2-ctypes-disable-wchar.patch      
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 199 more)
[INFO]:    Applying patch patches/disable-modules.patch                        
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 181 more)
[INFO]:    Applying patch patches/fix-locale.patch                             
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 176 more)
[INFO]:    Applying patch patches/fix-gethostbyaddr.patch                      
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 183 more)
[INFO]:    Applying patch patches/fix-setup-flags.patch                        
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 181 more)
[INFO]:    Applying patch patches/fix-filesystemdefaultencoding.patch          
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 195 more)
[INFO]:    Applying patch patches/fix-termios.patch                            
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 177 more)
[INFO]:    Applying patch patches/custom-loader.patch                          
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 179 more)
[INFO]:    Applying patch patches/verbose-compilation.patch                    
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 185 more)
[INFO]:    Applying patch patches/fix-remove-corefoundation.patch              
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 191 more)
[INFO]:    Applying patch patches/fix-dynamic-lookup.patch                     
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 184 more)
[INFO]:    Applying patch patches/fix-dlfcn.patch                              
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 175 more)
[INFO]:    Applying patch patches/parsetuple.patch                             
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 176 more)
[INFO]:    Applying patch patches/ctypes-find-library-updated.patch            
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 193 more)
[INFO]:    Applying patch patches/fix-ftime-removal.patch                      
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 183 more)
[INFO]:    -> running touch /home/lukas/.local/share...(and 75 more)           
[INFO]:    Prebuilding sdl2 for armeabi-v7a
[INFO]:    sdl2 has no prebuild_armeabi_v7a, skipping
[INFO]:    Applying patches for sdl2[armeabi-v7a]
[INFO]:    Applying patch add_nativeSetEnv.patch
[INFO]:    -> running patch -t -d /home/lukas/.local...(and 160 more)
[INFO]:    -> running touch /home/lukas/.local/share...(and 64 more)           
[INFO]:    # Building recipes
[INFO]:    Building hostpython2 for armeabi-v7a
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/hostpython2/desktop/hostpython2
[INFO]:    -> running configure
[INFO]:    -> running make -j5                                                 
[INFO]:    -> running mv Parser/pgen hostpgen                                  
[INFO]:    -> running mv python hostpython
[INFO]:    <- directory context /home/lukas
[INFO]:    Building libffi for armeabi-v7a
ccache found, will optimize builds
('path is', '/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/:/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/:/home/lukas/android-ndk-r13b:/home/lukas/Android/Sdk/tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-7-oracle/bin:/usr/lib/jvm/java-7-oracle/db/bin:/usr/lib/jvm/java-7-oracle/jre/bin:/home/lukas/android-ndk-r12b')
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/libffi/armeabi-v7a/libffi
[INFO]:    -> running autogen.sh
[INFO]:    -> running autoreconf -vif                                          
[INFO]:    -> running configure --host=arm-linux-and...(and 114 more)          
[INFO]:    -> running make -j5 libffi.la                                       
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/libffi/armeabi-v7a/libffi
[INFO]:    <- directory context /home/lukas/.local/share/python-for-android/build/other_builds/libffi/armeabi-v7a/libffi
[INFO]:    -> running cp -t /home/lukas/.local/share...(and 115 more)
[INFO]:    <- directory context /home/lukas
[INFO]:    Building sdl2_image for armeabi-v7a
[INFO]:    Building sdl2_mixer for armeabi-v7a
[INFO]:    Building sdl2_ttf for armeabi-v7a
[INFO]:    Building python2 for armeabi-v7a
[INFO]:    -> running cp /home/lukas/.local/share/py...(and 169 more)
[INFO]:    -> running cp /home/lukas/.local/share/py...(and 167 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/other_builds/python2/armeabi-v7a/python2
[INFO]:    -> running cp /usr/local/lib/python2.7/di...(and 62 more)
ccache found, will optimize builds
('path is', '/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/:/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/:/home/lukas/android-ndk-r13b:/home/lukas/Android/Sdk/tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-7-oracle/bin:/usr/lib/jvm/java-7-oracle/db/bin:/usr/lib/jvm/java-7-oracle/jre/bin:/home/lukas/android-ndk-r12b')
[INFO]:    -> running gcc -dumpmachine
[INFO]:    ############-lm -L/home/lukas/.local/share/python-for-android/build/libs_collections/unnamed_dist_1/armeabi-v7a -Wl,-soname=libpython2.7.so
[INFO]:    -> running configure --host=arm-eabi --bu...(and 194 more)
First install (expected to fail...                                             
[INFO]:    -> running make -j5 install HOSTPYTHON=/h...(and 262 more)
Second install (expected to work)                                              
[INFO]:    -> running touch python.exe python
[INFO]:    -> running make -j5 install HOSTPYTHON=/h...(and 262 more)
[INFO]:    -> running rm -rf python-install/lib/python2.7/test                 
[INFO]:    -> running rm -rf python-install/lib/python2.7/json/tests
[INFO]:    -> running rm -rf python-install/lib/python2.7/lib-tk
[INFO]:    -> running rm -rf python-install/lib/python2.7/sqlite3/test
[INFO]:    -> running rm -rf python-install/lib/pytho...(and 19 more)
[INFO]:    -> running rm -rf python-install/lib/pytho...(and 18 more)
[INFO]:    -> running rm -rf python-install/lib/python2.7/bsddb/tests
[INFO]:    -> running rm -rf python-install/lib/pytho...(and 20 more)
[INFO]:    -> running rm -rf python-install/lib/python2.7/email/test
[INFO]:    -> running rm -rf python-install/lib/python2.7/curses
[INFO]:    <- directory context /home/lukas
[INFO]:    -> running cp -a /home/lukas/.local/share...(and 162 more)
[INFO]:    Copying hostpython binary to targetpython folder
[INFO]:    -> running cp /home/lukas/.local/share/py...(and 175 more)
[INFO]:    -> running cp /home/lukas/.local/share/py...(and 173 more)
[INFO]:    Building sdl2 for armeabi-v7a
ccache found, will optimize builds
('path is', '/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/:/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/:/home/lukas/android-ndk-r13b:/home/lukas/Android/Sdk/tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-7-oracle/bin:/usr/lib/jvm/java-7-oracle/db/bin:/usr/lib/jvm/java-7-oracle/jre/bin:/home/lukas/android-ndk-r12b')
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/build/bootstrap_builds/sdl2/jni
[INFO]:    -> running ndk-build V=1
[INFO]:    <- directory context /home/lukas                                    
[INFO]:    # Biglinking object files
[INFO]:    Collating object files from each recipe
[INFO]:    hostpython2 recipe has no biglinkable files dir, skipping
[INFO]:    libffi recipe has no biglinkable files dir, skipping
[INFO]:    sdl2_image recipe has no biglinkable files dir, skipping
[INFO]:    sdl2_mixer recipe has no biglinkable files dir, skipping
[INFO]:    sdl2_ttf recipe has no biglinkable files dir, skipping
[INFO]:    python2 recipe has no biglinkable files dir, skipping
[INFO]:    sdl2 recipe has no biglinkable files dir, skipping
ccache found, will optimize builds
('path is', '/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/:/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/:/home/lukas/android-ndk-r13b:/home/lukas/Android/Sdk/tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-7-oracle/bin:/usr/lib/jvm/java-7-oracle/db/bin:/usr/lib/jvm/java-7-oracle/jre/bin:/home/lukas/android-ndk-r12b')
[INFO]:    There seem to be no libraries to biglink, skipping.
[INFO]:    # Postbuilding recipes
[INFO]:    Postbuilding hostpython2 for armeabi-v7a
[INFO]:    Postbuilding libffi for armeabi-v7a
[INFO]:    Postbuilding sdl2_image for armeabi-v7a
[INFO]:    Postbuilding sdl2_mixer for armeabi-v7a
[INFO]:    Postbuilding sdl2_ttf for armeabi-v7a
[INFO]:    Postbuilding python2 for armeabi-v7a
[INFO]:    Postbuilding sdl2 for armeabi-v7a
[INFO]:    # Installing pure Python modules
[INFO]:    There are no Python modules to install, skipping
[INFO]:    # Creating Android project from build and sdl2 bootstrap
[INFO]:    This currently just copies the SDL2 build stuff straight from the build dir.
[INFO]:    -> running rm -rf /home/lukas/.local/share...(and 40 more)
[INFO]:    -> running cp -r /home/lukas/.local/share...(and 112 more)
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    <- directory context /home/lukas
[INFO]:    Bootstrap running with arch armeabi-v7a
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    Copying python distribution
[INFO]:    -> running mkdir private
[INFO]:    -> running mkdir assets
[INFO]:    -> running python.host -OO -m compileall ...(and 80 more)
[INFO]:    -> running cp -a /home/lukas/.local/share...(and 73 more)           
[INFO]:    Copying libs
[INFO]:    -> running cp -a /home/lukas/.local/share...(and 102 more)
[INFO]:    -> running cp -a /home/lukas/.local/share...(and 96 more)
[INFO]:    Unpacking aars
[INFO]:    Copying java files
[INFO]:    -> running cp -a /home/lukas/.local/share...(and 56 more)
[INFO]:    Filling private directory
[INFO]:    private/lib does not exist, making
[INFO]:    -> running cp -a python-install/lib private
[INFO]:    -> running mkdir -p private/include/python2.7
[INFO]:    -> running cp python-install/include/pytho...(and 42 more)
[INFO]:    Removing some unwanted files
[INFO]:    -> running rm -f private/lib/libpython2.7.so
[INFO]:    -> running rm -rf private/lib/pkgconfig
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/private/lib/python2.7
[INFO]:    -> running rm -f urlparse.py threading....(and 19101 more)
[INFO]:    Deleting some other stuff not used on android
[INFO]:    -> running rm -rf lib2to3
[INFO]:    -> running rm -rf idlelib
[INFO]:    -> running rm -f config/libpython2.7.a
[INFO]:    -> running rm -rf config/python.o
[INFO]:    <- directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    <- directory context /home/lukas
[INFO]:    Stripping libraries
ccache found, will optimize builds
('path is', '/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/:/home/lukas/android-ndk-r13b/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/:/home/lukas/android-ndk-r13b:/home/lukas/Android/Sdk/tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-7-oracle/bin:/usr/lib/jvm/java-7-oracle/db/bin:/usr/lib/jvm/java-7-oracle/jre/bin:/home/lukas/android-ndk-r12b')
[INFO]:    -> running find /home/lukas/.local/share/...(and 129 more)
[INFO]:    Stripping libraries in private dir                                  
[INFO]:    Frying eggs in /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/private/lib/python2.7/site-packages
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    Saving distribution info
[INFO]:    <- directory context /home/lukas
[INFO]:    # Your distribution was created successfully, exiting.
[INFO]:    Dist can be found at (for now) /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    Of the existing distributions, the following meet the given requirements:
[INFO]:    	unnamed_dist_1: includes recipes (hostpython2, libffi, sdl2_image, sdl2_mixer, sdl2_ttf, python2, sdl2), built for archs (armeabi-v7a)
[INFO]:    unnamed_dist_1 has compatible recipes, using this one
[INFO]:    -> directory context /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1
[INFO]:    -> running ant debug
[INFO]:    <- directory context /home/lukas                                    
[INFO]:    # Copying APK to current directory
[INFO]:    # Found APK file: /home/lukas/.local/share/python-for-android/dists/unnamed_dist_1/bin/Myapp-0.2-debug.apk
[INFO]:    -> running cp /home/lukas/.local/share/py...(and 64 more)
lukas@seb-desktop:~$ 
  ```
