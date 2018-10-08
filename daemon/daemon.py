from time import sleep, time
import webcam_lib
from fan_lib import Fan

def start(sens, db):
    fan = Fan(15)
    while True:
        #read the values passed from the class sens
        temp, hum, noise, _ = sens.get_values()
        # obtain control of the fan (pin 15, GPIO 22)

        #hum = sens.thresholdH + 10
        print(str(hum) + " " + str(temp))
       
	#check all the values with the threshold
        if (temp!=None and hum != None and noise != None):
            if (hum > sens.thresholdH and fan.status == False):
                #activate fan to decrease the humidity
                fan.start_fan()
                db.append ('humidity', str(time()))
                sens.enhanceGrooming = False
            elif (hum < sens.thresholdH and fan.status == True):
                # stop the fan, it is not needed anymore
                sens.enhanceGrooming = True
                fan.stop_fan()

            if noise == True:
                print ("Attivo webcam")
                #actvate camera to record swarm direction
                webcam_lib.activate_webcam()
                sens.set_swarm(True)
                db.append ('swarming', str(time()))

            if noise > sens.thresholdNoiseHigh:
                #activate smoke if enabled
                pass

        sleep(10)
