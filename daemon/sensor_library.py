try:
	import Adafruit_DHT
except:
	import dummy_class as Adafruit_DHT
import signal
from threading import Lock, Thread
import daemon
import mic

noiseSensor = mic.Noise(13)


class SensorLib:
    # this class polls the sensors to obtain data
    # it updates itself every 5 seconds through a SIGALRM
    def __init__(self, db):
        self.thSensor = Adafruit_DHT.AM2302
        self.thPin = 4
        #threshold for the values 
        self.thresholdH = 43
        self.tresholdTemp = 20
        self.thresholdNoiseHigh = 200
        self.thresholdNoiseLow = -1
        self.enhanceGrooming = True
        self.swarming = False
        self.noise = False

        # sensor values
        self.humidity = None
        self.temperature = None
        self.noise = None
        self.lock = Lock ()
        signal.signal (signal.SIGALRM, self.handler)
        signal.alarm (5)

        # now detach a daemon that regurarly checks class values
        # and makes changes based on them
        Thread (target = daemon.start, args=(self, db) ).start ()

    def set_swarm (self, val):
        self.lock.acquire ()
        self.swarming = val
        self.lock.release ()

    def get_values (self):
        # return tuple containing class values read from sensors
        self.lock.acquire ()
        tup = (self.temperature, self.humidity, self.noise, self.swarming)
        self.lock.release ()
        return tup

    def handler (self, signum, frame):
        # signum and frame are required for signal to work properly
        # sense temperature and humidity
        # sense noise
        print ("Ho ricevuto l'handler")
        humidity, temperature = Adafruit_DHT.read_retry(self.thSensor, self.thPin)
        noise = self.sense_noise()

        # lock and update library values

        self.lock.acquire()
        self.noise = noise
        print ("Noise: ", noise)
        self.humidity = humidity
        self.temperature = temperature
        self.lock.release ()

        # resend alarm
        signal.alarm (5)

    def get (self, *args):
        for el in args:
            if el == 'humidity':
                pass
            else:
                pass
        return str(self.__dict__[args[0]])

    def sense_noise (self):
        global noiseSensor
        return noiseSensor.get_noise ()
