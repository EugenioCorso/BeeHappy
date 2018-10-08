# BeeHappy Flask Interface
#
# This has to be run on a CGI (with Nginx in our case)
# It provides a RESTful api to handle HTTP requests
# Requests come from the Android app and the daemon and are encoded into JSON
# EX: resource = "/humidity" -> JSON = { "humidity", somevalue }
#
# Resources are assigned to classes ( '/humidity' -> Hum )
# Each class implements two basic methods:
#    * get(self) is called when a GET request is received   -> it only returns the current status
#    * post(self) is called when a POST request is received -> it must set the new values received and return a response
import time 
from flask import Flask, request
from flask_restful import Resource, Api 
from sensor_library import SensorLib
from feeding import Feeding
import signal
import daemon
from kyoto_wrapper import Kyoto

def _beehappy_exit (self, signum):
        # signum and frame are required for signal to work properly
        # sense temperature and humidity
        # sense noise
        import sys
        import os
        # daemon.exit_flag = True
        if not os.fork ():
            os.execv ('/bin/rm', ['rm', '/home/flask/camlock.lock'])
        sys.exit (1)


flaskapp = Flask(__name__)
api = Api(flaskapp)

# sensor library to obtain data from sensors
feeding = Feeding(40) # PIN?
kyoto = Kyoto("events")
criticalDB = Kyoto("critical")
sensor = SensorLib(criticalDB)

class Hum(Resource):
    # resource that manages the humidity sensor
    # attributes are: humidity, active
    def __init__(self):
        global sensor
        self.humActive = 'on'

    def get (self):
        print ("humidity = " + sensor.get('humidity'))
        kyoto.db.set(time.time(), str(request))
        return {'humidity': sensor.get('humidity') }


    def post (self):
        json_data = request.get_json(force=True)
        self.humActive = json_data['active'];
        kyoto.db.set(time.time(), str(request))
        if active == 'on':
            print('hum activated!')
            # activate the sensor
        else :
            print('hum deactivated!')
            # deactivate the sensor
        return {'status': 'ok', 'active': self.humActive}

class Temp(Resource):
    # resource that manages the temperature sensor
    # attributes are: temperature, active
    def __init__(self):
        self.tempActive = 'on'

    def get (self):
        kyoto.db.set(time.time(), str(request))
        return {'temperature': sensor.get('temperature')}

    def post (self):
        json_data = request.get_json(force=True)
        kyoto.db.set(time.time(), str(request))
        self.tempActive = json_data['active'];
        if active == 'on':
            print('temp activated!')
            # activate the sensor
        else :
            print('temp deactivated!')
            # deactivate the sensor
        return {'status': 'ok', 'active': self.tempActive}


class Food(Resource):
    # resource that manages the feeding schedule
    # attributes are: active, hour, minutes, days
    # The feeding system manages the hour at which the bees are fed
    # and the days that pass between a food refill and another
    def __init__(self):
        self.foodActive = 'on'
        self.foodDays = 2
        self.foodHour = 14
        self.foodMins = 30
        self.feedNow = False

    def get (self):
        kyoto.db.set(time.time(), str(request))
        return {'active': self.foodActive, 'hour': self.foodHour, 'minutes': self.foodMins, 'days': self.foodDays}

    def post (self):
        kyoto.db.set(time.time(), str(request))
        json_data = request.get_json(force=True)
        self.foodActive = json_data['active'];
        if self.foodActive == 'on':
            print('food activated!')
            ret = self._convert_in_seconds(json_data)
            if type (ret) == bool:
                self.feed_now()
                criticalDB.store ('feeding', str(time.time()))
            else:
                self.set_food_schedule(ret)
        else :
            print('food deactivated!')
            # deactivate the feeding system
            feeding.stop_feeding()
        return {'status': 'ok', 'active': self.foodActive}
    
    def feed_now (self):
        feeding.feed_now()

    def _convert_in_seconds(self, json):
        if json['feedNow'] == 'True':
            return True
        else:
            self.foodHour = json['hour']
            self.foodMins = json['minutes']
            self.foodDays = json['days']
            return self.foodDays * 24 * 60**2 + self.foodHour * 60**2 + self.foodMins * 60


    def set_food_schedule(self, time):
        # TODO this should communicate with the daemon
        feeding.set_feeding_date("Feeding every "+str(self.foodDays)+" days, at: " +str(self.foodHour)+":"+str(self.foodMins))
        feeding.start_feeding(time)
        return

class Smoke(Resource):
    # resource that manages the smoke releaser
    # attributes are: active
    def __init__(self):
        self.smokeActive = True

    def get (self):
        kyoto.db.set(time.time(), str(request))
        return {'active': self.smokeActive}

    def post (self):
        kyoto.db.set(time.time(), str(request))
        json_data = request.get_json(force=True)
        self.smokeActive = json_data['active'];
        if self.smokeActive == True:
            print('smoke activated!')
            # activate the smoke system
        else :
            print('smoke deactivated!')
            # deactivate the smoke system
        return {'status': 'ok', 'active': smokeActive}

# class Swarming (Resource):
    # # resource that manages the swarming situation
    # def __init__(self):
        # self.swarmBool = False

    # def get (self):
        # kyoto.db.set(time.time(), str(request))
        # _, _, _, self.swarmBool = sensor.get_values ()
        # return {'state': self.swarmBool}

    # def post (self):
        # kyoto.db.set(time.time(), str(request))
        # #        json_data = request.get_json(force=True) # not used
        # sensor.set_swarm (False)

class Critical (Resource):
    def __init__(self):
        self.fanOn = criticalDB.get("fanOn")
        self.swarm = criticalDB.get("swarm")
        self.feed = criticalDB.get("feedDate")

    def get (self):
        self.swarm = criticalDB.get("swarm")
        _, _, _, self.swarm = sensor.get_values ()
        self.fanOn = criticalDB.get("fanOn")
        self.feed = criticalDB.get("feedDate")
        self.feed = feeding.feedDate
        kyoto.db.set(time.time(), str(request))
        return {'feedDate': self.feed, 'fanOn' : str(self.fanOn), 'swarm': str(self.swarm)}

class HelloWorld(Resource):
    def get(self):
        return 'Bzzz. Bzzzzzzz Bzzz Bzz.'

#  register endpoint
api.add_resource(HelloWorld, '/')
api.add_resource(Hum, '/humidity')
api.add_resource(Temp, '/temperature')
api.add_resource(Food, '/food')
api.add_resource(Smoke, '/smoke')
# api.add_resource(Swarming, '/swarming')
api.add_resource(Critical, '/critical')

if __name__ == '__main__':
    # add the resources to the endpoints
    # run the server
    signal.signal (signal.SIGINT, _beehappy_exit) 
    flaskapp.run(port=8080, host='0.0.0.0', debug=False)
