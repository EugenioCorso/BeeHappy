import RPIO, atexit
RPIO.setmode(RPIO.BOARD)

class Noise:
    def __init__(self, pin):
        self.PIN = pin
        # the default output for the GPIO is 0
        # this means that the fan will be off at startup
        self.status = False
        RPIO.setup(pin, RPIO.IN)
        atexit.register(self._exit_handler_)
        self.noise = False
        self.cnt = 0

    def get_noise (self):
        self.cnt += 1
        self.noise = RPIO.input(self.PIN)
        print (self.noise,  "noise dal sensore")
        if self.noise == False:
            self.cnt = 0
        return self.noise

    def _exit_handler_(self):
        if self.status == True:
            print('[EXIT] Turning OFF the fan.')
            RPIO.output(self.PIN, False)

        print('[EXIT] Performing a RESET (cleanup) of the fan GPIO (pin: ' + str(self.PIN) + ')')
        RPIO.cleanup()
