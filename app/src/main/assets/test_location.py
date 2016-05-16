import androidlog, location, sys, time
l = androidlog.log2
path = "/storage/emulated/legacy/Android/data/com.sensibility_testbed/files/"
filename = "locations_" + str(time.time())

with open(path + filename, "w+") as out:
clear

    l('Lets do the location')
    out.write("time_polled, time_sample, accuracy, altitude, bearing, latitude, longitude, speed\n")
    while True:
        loc = location.get_location()
        nw = loc.get("network")
        if nw:
            out.write("%s,%f,%f,%f,%f,%f,%f,%f,%f\n" % ("nw", nw.get("time_polled"), nw.get("time_sample"), nw.get("accuracy"),
              nw.get("altitude"), nw.get("bearing c"), nw.get("latitude"), nw.get("longitude"), nw.get("speed")))

        gps = loc.get("gps")
        if gps:
            out.write("%s,%f,%f,%f,%f,%f,%f,%f,%f\n" % ("gps", gps.get("time_polled"), gps.get("time_sample"), gps.get("accuracy"),
              gps.get("altitude"), gps.get("bearing"), gps.get("latitude"), gps.get("longitude"), gps.get("speed")))

        l(repr(loc))
        time.sleep(0.5)
        out.flush()

l('KTHXBI')