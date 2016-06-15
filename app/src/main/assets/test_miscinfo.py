import androidlog, miscinfo, sys, time
l = androidlog.log2

l("Do the info")
l("Battery Info")
l(repr(miscinfo.get_battery_info()))

l("Wifi enabled:")
l(repr(miscinfo.get_wifi_state()))

l("Wifi Connection info:")
l(repr(miscinfo.get_wifi_connection_info()))

l("Wifi Scan info:")
l(repr(miscinfo.get_wifi_scan_info()))

l("Bluetooth info")
l(repr(miscinfo.get_bluetooth_info()))

l("Bluetooth scan info")
l(repr(miscinfo.get_bluetooth_scan_info()))
