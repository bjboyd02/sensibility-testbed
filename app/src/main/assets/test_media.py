import androidlog, media, sys, time
l = androidlog.log2

path = "/storage/emulated/legacy/Android/data/com.sensibility_testbed/files/"
filename = "recording_" + str(time.time()).replace(".", "") + ".mp4"

l('Lets do the recording')
media.microphone_record(path + filename, 10000)

# Give it some time to speak
time.sleep(15)

l('Au revoir')