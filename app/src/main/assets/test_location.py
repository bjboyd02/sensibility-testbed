import androidlog, location, sys, time
l = androidlog.log2
l('Lets do the location')
while True:
    loc = location.get_location()
    l("Location: " + str(loc))
    lkloc = location.get_lastknown_location()
    l("Last Known Location: " + str(lkloc))
    time.sleep(0.5)

l('KTHXBI')