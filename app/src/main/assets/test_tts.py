import androidlog, media, sys, time
l = androidlog.log2
l('Lets do the media')
text = """Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod
tempor incididunt ut labore et dolore magna aliqua."""

result = media.tts_speak(text)

# Give it some time to speak
for x in range(20):
    if media.is_tts_speaking():
        l("TTS is speaking")
    else:
        l("TTS is not speaking")
    time.sleep(1)


l('KTHXBI')