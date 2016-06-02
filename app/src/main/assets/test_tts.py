import androidlog, media, sys, time
l = androidlog.log2
l('Lets do the media')
result = media.tts_speak("Hello from Python")
# Give it some time to speak
time.sleep(10)

l('KTHXBI')