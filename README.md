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
 * the Java Native interface to call into Java, and 
 * the proper Python bindings to make these JNI calls available 
  to Python code.

Scroll for a [detailed System Overview](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/README.md#detailed-system-overview)

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


## Accelerometer Sensor Extension Example

![Sensibility Sequence Diagram for Acceleration Extension](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/docs/sensibility_sequence.png "Sequence Diagram for Acceleration Extension")

1. The native method `Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter()` is declared in [PythonInterpreterService.java](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/java/com/snakei/PythonInterpreterService.java) and defined in [interpreter.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/interpreter.c) and gets called from the JVM
1. This method calls [sensor.c's](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/sensors.c#L340)`initsensor()` which
 - initializes the sensor extensions in a Python module (not shown)
 - and uses the custom JNI glue - [jnihelper.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/jnihelper.c) - to lookup the required Java class and methods defined in [SensorService.java](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/java/com/snakei/SensorService.java) and caches them for later use.
1. The interpreter then calls `sensor.c`'s sensor_start_sensing() which passes the previously cached Java class and methods to the the JNI glue, in order to
 - retrieve a Singleton instance of `SensorService` and
 - call `start_sensing()` on the instance which registers a listener on the main thread (not the thread the method was called from). The listener receives regular updates from the device's Accelerometer
1. The interpreter then runs a Python script which simply imports the previously initialized Python module `sensor` and calls its `get_acceleration()` function. The script is executed using a custom C wrapper defined in [pyhelper.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/pyhelper.c)
 - the Python function `get_acceleration()` is defined as C Extension in `sensors.c` where it again uses `jnihelper.c` to get a Singleton instance of `SensorService.java` and call `getAcceleartion()` on that instance, which
 - returns the last Sensor update, received in the previously registered Listener, as String serialized JSON Object
 - `jnihelper.c` uses [cjson.c](https://github.com/aaaaalbert/sensibility-testbed/blob/native-sensors-jni/app/src/main/jni/cjson.c) to decode the returned String as Python Object which gets returned to the calling Python script
 1. Eventually, the interpreter calls `sensor_stop_sensing()` to unregister the listener in Java


## Extension Modules



-----
## Todo