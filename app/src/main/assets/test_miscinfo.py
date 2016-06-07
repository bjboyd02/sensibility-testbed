import androidlog, miscinfo, sys, time
l = androidlog.log2

l("Do the info")
info = miscinfo.get_battery_info()
l(repr(info))