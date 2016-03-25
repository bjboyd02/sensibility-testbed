from androidlog import log as l
import os
import sys

l(str(sys.path))
l(str(os.environ.keys()))
l("Bye, then!")

"""
l("This part ran...")
f = open("/sdcard/foo_was_bar", "w")
l("File opened")
try:
  import os
  l("Have os")
  os.chdir("/sdcard/")
  f.write(os.getcwd())
  l("Wrote")
except Exception, e:
  l("Ouch: " + repr(e))
  f.write("no joy: " + repr(e))

l("Closing")
f.close()
l("Bye")
"""
