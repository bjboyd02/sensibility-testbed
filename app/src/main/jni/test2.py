import androidlog
l = androidlog.log

try:
  import time
except Exception, e:
  l("Ouch, " + repr(e))

for i in range(100):
  l("*** Number " + str(i))
  try:
    time.sleep(2)
  except Exception, e:
    l("Yikes, " + repr(e))
  l("All's well!")
