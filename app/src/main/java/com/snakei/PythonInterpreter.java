package com.snakei;

/**
 * Created by
 * lukas.puehringer@nyu.edu
 * on 7/25/16.
 *
 * Provides a static Python Interpreter that defines
 * a native method which sets Python environment variables and executes
 * the passed Python script.
 *
 *
 *
 */
public class PythonInterpreter {

    // Todo: don't hardcode here
    private static String python_home =
            "/data/data/com.sensibility_testbed/files/python";
    private static String python_path =
            "/data/data/com.sensibility_testbed/files/seattle/seattle_repy";


    public static void runScript(String[] python_args) {
        PythonInterpreter.runScript(
                python_args, python_home, python_path);
    }

    private static native void runScript(String[] python_args,
                                         String python_home, String python_path);

}
