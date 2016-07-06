## Native Sensor Extensions

This is the source repo for SensibilityTestbed (Android app).

See https://sensibilitytestbed.com/ for details

-----
We are currently moving away from SL4A towards an embedded Python 
interpreter with native-looking sensor interfaces (i.e. Python 
functions that you can call from Python code in order to get 
sensor values).

How this works approximately:
* The required sensor event listening stuff is implemented in Java
* The Python interpreter is [embedded](https://docs.python.org/2/extending/embedding.html) 
  into C code, where we use
 * the Java Native Interface (JNI) to call into Java, and 
 * the proper Python bindings to make these JNI calls available 
  to Python code.

Scroll down for a [detailed System Overview](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/README.md#detailed-system-overview)

## Installation and Usage

* Clone the project
* Use `ndk-build` in `sensibility-testbed/app/src/main/jni` to build the 
  native module, `snakei`.
* Copy the shared objects for Snakei and Python from 
  `sensibility-testbed/app/src/main/libs/armeabi` into `sensibility-testbed/app/jniLibs` 
  where [the Gradle config currently expects them](https://github.com/aaaaalbert/sensibility-testbed/blob/use-android-studio/app/build.gradle#L39).
* Import the `sensibility-testbed` project into Android Studio.
* Android Studio should (TM) download all of the Gradle etc. stuff it 
  needs to build the APK.
* Add Python scripts - `*.py` - to the [assets directory](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/assets) and modify [interpreter.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/interpreter.c#L55-L73) to execute them.
* Build the APK
* `adb install` it on your device
* Look at `adb logcat` to see interesting debug messages scroll by.


## Detailed System Overview

![Sensibility Implementation Overview](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/docs/sensibility_overview.png "Sensibility Implementation Overview")

### JVM
 - **Activity** - `com.sensibility_testbed.ScriptActivity`
   - `onCreate` is the entry point to the app
   - sets up UI and handles user interaction 
   - starts `PythonInterpreterService`
   - *parts to install Seattle are currently commented out*
 - **Python Interpreter Service** - `com.snakei.PythonInterpreterService` 
   - runs in background
   - loads native libraries (Python) and native modules (Snakei)
   - starts new thread (Android `Service`s don't run in their own thread a priori)
   - calls its native method defined in `interpreter.c` 
 - **Application** - `com.sensibility_testbed.SensibilityApplication`
   - Workaround to provide a static reference of the application context to Sensor Service Facades
 - **Android Manifest** - `AndroidManifest.xml` 
   - defines above components and required permissions

 - **Sensor Service Facades** -  `com.snakei.SensorService`, `com.snakei.LocationService`, `com.snakei.MediaService`,  `com.snakei.MiscInfoService`, `com.snakei.OutputService`
   - Facades for Android resource access, i.e. sensors, location providers, media components and miscellaneous device information
   - made available to native code via JNI calls to functions that return either simple data types or String serialized JSON Objects
   - *currently access the application context using a static call to `SensibilityApplication`*
   - *These are not Android Services, they do not implement `android.app.Service`


### Native Code
 - **Snakei** - `snakei.c`
   - receives and caches a reference to JVM when loaded via `System.loadLibrary()`
   - included by all extensions to access the JVM pointer
 - **Python Interpreter** - `interpreter.c`
   - defines native function, declared in `PythonInterpreterService.java`, from where it gets called
   - currently used to initialize Python modules - `init*()`, acquire resources in Java - `*_start_*()`, run Python tests scripts that use the new modules, and eventually release the resources - *_stop_*()
 - **Sensor Init/Deint Function /  Python Extensions** - `sensors.c`, `location.c`, `media.c`, `miscinfo.c`, `outputs.c`
   - initialize Python module
   - acquire Android resources
   - Python extensions to access Android resources
   - release Android resources
 - **JNI glue** - `jnihelper.c`
   - Helper functions that can be used by C-Python extension to call into the Android Java Virtual Machine (JVM) using the Java Native Interface (JNI)
   - Some of the functions convert Java's return values to Python objects
 - **CJSON** - `cjson.c`
   - We use [Python CJSON](https://pypi.python.org/pypi/python-cjson) to decode String serialized JSON objects received from Java and convert them directly to Python objects


## Accelerometer Sensor Extension Example

![Sensibility Sequence Diagram for Acceleration Extension](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/docs/sensibility_sequence.png "Sequence Diagram for Acceleration Extension")

1. The native method `Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter()` is declared in [PythonInterpreterService.java](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/java/com/snakei/PythonInterpreterService.java) and defined in [interpreter.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/interpreter.c) and gets called from the JVM
1. This method calls [sensor.c's](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/sensors.c#L340)`initsensor()` which
 - initializes the sensor extensions in a Python module (not shown)
 - and uses the custom JNI glue - [jnihelper.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/jnihelper.c) - to lookup the required Java class and methods defined in [SensorService.java](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/java/com/snakei/SensorService.java) and caches them for later use.
1. The interpreter then calls sensor.c's `sensor_start_sensing()`, which passes the previously cached Java class and methods to the the JNI glue, in order to
 - retrieve a Singleton instance of `SensorService` and
 - call `start_sensing()` on the instance which registers a listener on the main thread (not the thread the method was called from). The listener receives regular updates from the device's Accelerometer
1. The interpreter then runs a Python script which simply imports the previously initialized Python module `sensor` and calls its `get_acceleration()` function. The script is executed using a custom C wrapper defined in [pyhelper.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/pyhelper.c)
 - the Python function `get_acceleration()` is defined as C Extension in `sensors.c` where it again uses `jnihelper.c` to get a Singleton instance of `SensorService.java` and call `getAcceleartion()` on that instance, which
    - returns the last Sensor update, received in the previously registered Listener, as String serialized JSON Object
    - `jnihelper.c` uses [cjson.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/cjson.c) to decode the returned String as Python Object which gets returned to the calling Python script
1. Eventually, the interpreter calls `sensor_stop_sensing()` to unregister the listener in Java


## Extension Modules
The following modules are available in Python. Detailed information about each method can be found in the source code comments. 

### sensor
`sensors.c`

 - get_sensor_list()
 - get_acceleration()
 - get_ambient_temperature()
 - get_game_rotation_vector()
 - get_geomagnetic_rotation_vector()
 - get_gravity()
 - get_gyroscope()
 - get_gyroscope_uncalibrated()
 - get_heart_rate()
 - get_light()
 - get_linear_acceleration()
 - get_magnetic_field()
 - get_magnetic_field_uncalibrated()
 - get_pressure()
 - get_proximity()
 - get_relative_humidity()
 - get_rotation_vector()
 - get_step_counter()
### location
`location.c`

 - get_location()
 - get_lastknown_location()
 - get_geolocation(latitude, longitude, max_results)
### media
`media.c`

 - tts_speak(message)
 - microphone_record(file_name, duration)
 - is_media_playing()
 - is_tts_speaking()
### miscinfo
`miscinfo.c`

 - get_bluetooth_info()
 - get_bluetooth_scan_info()
 - is_wifi_enabled()
 - get_wifi_state()
 - get_wifi_connection_info()
 - get_wifi_scan_info()
 - get_network_info()
 - get_cellular_provider_info()
 - get_cell_info()
 - get_sim_info()
 - get_phone_info()
 - get_mode_settings()
 - get_display_info()
 - get_volume_info()
 - get_battery_info()
### androidlog
`outputs.c`

 - log(message)

** Notes on Java Native Interface
In general the JNI can be used to call native methods from Java and vice versa. This application uses the JNI in both ways. It starts a Python interpreter - a native method that runs Python scripts - from Java and it calls into the JVM from native code, to access Android resources and make them available to Python as C-Python extensions. Most of the JNI specific calls are hidden by `jnihelper.c`. Furthermore, complex data, e.g. nested lists or maps containing values of different data types are returned from Java to the native code as String serialized JSON Objects and subsequently converted to Python Objects. This avoids reaching back into the JVM repeatedly. E.g. given an array of 

*Helpful resources*
 - [Android JNI Tips](https://developer.android.com/training/articles/perf-jni.html)
 - [JNI Specification](http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/jniTOC.html)
 - [JNI best practices](http://www.ibm.com/developerworks/java/library/j-jni/index.html)



-----
## Todo
 - Write Todo