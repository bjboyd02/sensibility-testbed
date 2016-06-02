import androidlog, location, sys, time
l = androidlog.log2

path = "/storage/emulated/legacy/Android/data/com.sensibility_testbed/files/"
filename = "locations_" + str(time.time())

with open(path + filename, "w+") as out:
    l('Lets do the location')
    out.write("time_polled, time_sample, accuracy, altitude, bearing, latitude, longitude, speed\n")
    while True:
        data = location.get_location()
        l(repr(data))
        for provider in ["network", "gps", "fused"]:
            loc = data.get(provider)
            if loc:
                out.write("%s,%f,%f,%f,%f,%f,%f,%f,%f\n" % (provider, loc.get("time_polled", -1), loc.get("time_sample", -1), loc.get("accuracy", -1),
                  loc.get("altitude", -1), loc.get("bearing", -1), loc.get("latitude", -1), loc.get("longitude", -1), loc.get("speed", -1)))


        
        time.sleep(0.5)
        out.flush()

l('KTHXBI')