import subprocess
import time
import os
import sys


def activate_webcam ():
    if os.path.isfile('/home/flask/camlock.lock'):
        pass
    else :
        if not os.fork ():
            with open('/home/flask/camlock.lock', 'w') as fp:
                fp.write('lock')
            name = '/home/flask/media/_'
            cnt = 0
            # child
            isActive = True
            while cnt < 50:
               cmd = ["fswebcam", name + str(cnt) + '.jpg']
               cnt += 1
               subprocess.call (cmd)
               print (cmd)
               time.sleep (30)
            os.execv ('/bin/rm', ['rm', '/home/flask/camlock.lock'])
