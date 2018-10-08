try:
	import RPIO
except:
	import dummy_class as RPIO
# to execute a cleanup before the program exits
import atexit
from threading import Lock, Thread
import time

# set the board mode on GPIOs
# (if it has already been done it has no effect
RPIO.setmode(RPIO.BOARD)

class Feeding:
    def __init__(self, pin):
        self.PIN = pin
        # the default output for the GPIO is 0
        # this means that the fan will be off at startup
        self.status = False
        RPIO.setup(pin, RPIO.OUT)
        RPIO.output(pin, False)
        self.pin = pin
        atexit.register(self._exit_handler_)
        self.repetition = 0
        self.feed = False
        self.timePassed = 0
        self.feedDate = None 
        # cycle and check that timepassed > cron time
        # in that case start feeding if feedBoolean True
        # else close Thread

    def start_feeding (self, time):
        # time in seconds
        self.feed = True
        self.repetition = time # feed when time has passed
        self.timePassed = 0
        self._start_pump()
        #Thread (target = self._start_feeding_routine,  ).start ()
    
    def set_feeding_date(self, date):
        self.feedDate = date

    def stop_feeding (self, time):
        # time in seconds
        self.feed = False
        self.timePassed = 0
        self.repetition = 0

    def feed_now (self):
        print ("feeding now")
        self.start_feeding (60)

    def _start_feeding_routine(self):
        while self.feed == True:
            if self.timePassed > self.repetition:
                self._start_pump()
            time.sleep (60)
            self.timePassed += 60
        # do not feed
        self.timePassed = 0
        thread.exit()

    def _start_pump(self):
        if self.status == False:
            RPIO.output(self.PIN, True)
            self.status = True
            time.sleep (20)
            self._stop_pump()

    def _stop_pump(self):
        if self.status == True:
            RPIO.output(self.PIN, False)
            self.status = False

    # true for active, false for inactive
    def get_timer (self):
        return self.repetition
    def get_status(self):
        return self.status

    def _exit_handler_(self):
        if self.status == True:
            print('[EXIT] Turning OFF the fan.')
            RPIO.output(self.pin, False)

        print('[EXIT] Performing a RESET (cleanup) of the fan GPIO (pin: ' + str(self.PIN) + ')')
        RPIO.cleanup()
