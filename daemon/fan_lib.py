try:
	import RPIO
except:
	import dummy_class as RPIO
# to execute a cleanup before the program exits
import atexit

# set the board mode on GPIOs
# (if it has already been done it has no effect
RPIO.setmode(RPIO.BOARD)

class Fan:
    def __init__(self, pin):
        self.PIN = pin
        # the default output for the GPIO is 0
        # this means that the fan will be off at startup
        self.status = False
        RPIO.setup(pin, RPIO.OUT)
        RPIO.output(pin, False)
        atexit.register(self._exit_handler_)

    def start_fan(self):
        if self.status == False:
            RPIO.output(self.PIN, True)
            self.status = True

    def stop_fan(self):
        if self.status == True:
            RPIO.output(self.PIN, False)
            self.status = False

    # true for active, false for inactive
    def get_status(self):
        return self.status

    def _exit_handler_(self):
        if self.status == True:
            print('[EXIT] Turning OFF the fan.')
            RPIO.output(self.PIN, False)

        print('[EXIT] Performing a RESET (cleanup) of the fan GPIO (pin: ' + str(self.PIN) + ')')
        RPIO.cleanup()
