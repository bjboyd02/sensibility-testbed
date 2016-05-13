import androidlog, sensor, sys, time
l = androidlog.log2
l('Lets get some sensor info')

for sensor_info in sensor.get_sensor_list():
  l(repr(sensor_info))

l('Oh, wow, lovely sensors, why not poll them?')
l('Lets start with some of the existing sensors...')

for i in xrange(512):
  l('Accelerometer: ' + repr(sensor.get_acceleration()))
  #l('Magnetic field: ' + repr(sensor.get_magnetic_field()))
  #l('Proximity: ' + repr(sensor.get_proximity()))
  #l('Light: ' + repr(sensor.get_light()))


l('Bye, bye!')