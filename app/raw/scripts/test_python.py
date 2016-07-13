import androidlog, sys, os
l = androidlog.log

l("New file layout")
l(repr(sys.path))
l(repr(os.path))

# try:
# 	l(repr(sys.builtin_module_names))
# except Exception, e:
# 	l("no builtin modules")
# 	l(str(e))

# try:
# 	l(repr(sys.modules.keys()))
# except Exception, e:
# 	l("no sys.modules")
# 	l(str(e))

# try:
# 	l(repr(sys.executable))
# except Exception, e:
# 	l("no executable")
# 	l(str(e))

# try:
# 	l(repr(sys.argv))
# except Exception, e:
# 	l("no arv")
# 	l(str(e))




