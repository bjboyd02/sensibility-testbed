import androidlog, location, sys, time
l = androidlog.log2

l('Lets do the location')
for i in xrange(40):
  loc = location.get_location()
  l(repr(loc))
  time.sleep(1)

l('KTHXBI')