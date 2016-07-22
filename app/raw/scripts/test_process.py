import androidlog, os, time
l = androidlog.log


while True:
    l("PID: " + str(os.getpid()) + " PPID: " + str(os.getppid()) + " UID: " + str(os.getuid()))
    time.sleep(1)