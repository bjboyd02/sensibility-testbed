import androidlog, location, sys, time
l = androidlog.log2
l('Lets do the location')
while True:
    loc = location.get_location()
    # l("Location: " + str(loc))
    # lkloc = location.get_lastknown_location()
    # l("Last Known Location: " + str(lkloc))
    # time.sleep(0.5)

    fused = loc.get("fused")
    if (fused):
        lon = fused.get("longitude")
        lat = fused.get("latitude")
        l("Get address for - lon: " + str(lon) + ", lat: " + str(lat))
        address = location.get_geolocation(lat, lon, 2)
        l(repr(address))

    time.sleep(2)

l('KTHXBI')