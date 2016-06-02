import androidlog, media, sys, time
l = androidlog.log2

while True:
	if media.is_media_playing():
		l("Media is playing")
	else:
		l("Media is not playing")
	time.sleep(1)

# path = "/storage/emulated/legacy/Android/data/com.sensibility_testbed/files/"
# filename = "recording_" + str(time.time()).replace(".", "") + ".mp4"

# l('Lets do the recording')
# media.microphone_record(path + filename, 10000)

# # Give it some time to speak
# time.sleep(15)

l('Au revoir')