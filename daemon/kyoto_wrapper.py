from kyotocabinet import *
import sys

class Kyoto:
    def __init__(self, filename):
        # create the database object
        self.db = DB()
        # open the database
        if not self.db.open(filename+'.kch', DB.OWRITER | DB.OCREATE):
            print("open error: " + str(self.db.error()), file=sys.stderr)

    def store (self, key, val):
        # store records
        self.db.set(key, val)
        
    def append(self, key, val):
        return self.store(key, val)
    
    def get (self, key):
        # retrieve records
        value = self.db.get_str(key)
        return value 

    def traverse (self):
        # traverse records
        cur = self.db.cursor()
        cur.jump()
        while c.step() is True :
            rec = cur.get_str(True)
            if not rec: break
            yield(rec[0],rec[1])
        cur.disable()

    def __del__(self):
        # close the database
        self.db.close()
