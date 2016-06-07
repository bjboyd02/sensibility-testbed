import androidlog, miscinfo, sys, time
l = androidlog.log2

l("Do the info")
l("Battery Info")
battery_info = miscinfo.get_battery_info()
l(repr(battery_info))

l("Wifi enabled:")
l(repr(miscinfo.get_wifi_state()))

l("Wifi Connection info:")
l(repr(miscinfo.get_wifi_connection_info()))

l("Wifi Scan info:")
l(repr(miscinfo.get_wifi_scan_info()))
