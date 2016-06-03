import androidlog, miscinfo, sys, time
l = androidlog.log2

l("Do the info")
info = miscinfo.jsontest()
l(repr(info))