import os
f = open("/sdcard/python.log", "w+")
f.write("I am a python\n")
f.write("And I even know my pid " + str(os.getpid()) + "\n")
f.close()