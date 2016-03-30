import androidlog
l = androidlog.log2

for i in range(100):
  l("*** Number " + str(i))
  sleep(2)
