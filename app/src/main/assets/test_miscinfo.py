import androidlog, miscinfo, sys, time
l = androidlog.log2

l("Do the info")
l("Battery Info")
battery_info = miscinfo.get_battery_info()
l(repr(battery_info))

l("Wifi enabled:")
for x in range(20):
    l(repr(miscinfo.get_wifi_state()))
    time.sleep(1)