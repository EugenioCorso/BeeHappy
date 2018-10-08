import subprocess
import os
import time

def main ():
	if os.fork () == 0:
		# child
		subprocess.call (['fswebcam', '-l' , '2', '/home/flask/media/img.jpg'])
		exit(0)
	else:
		while True:
			print ('padre')
			time.sleep (2)

		
if __name__ == '__main__':
	main()
