#!/usr/bin/env python2

import socket
from threading import Thread
import time

class Connection2Rio(Thread):
    # roboRIO_url = "robotRIO-4914-frc.local"
    robotRIO_url = "0.0.0.0"
    robotRIO_port = 44869
    _isConnected = False
    value = 0

    def setValue(self, theValue):
        self.value = theValue

    def run(self):
        err_count = 0
        while True:
            try:
                if err_count >= 3:
                    self._s.close()
                    self._s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    self.connect()
                self.sendTelemetry(self.value)
                time.sleep(0.1)
                err_count = 0
            except Exception as e:
                err_count += 1
                time.sleep(1)
                print "Failed to send the telemetry."
                print e

    def __init__(self):
        super(Connection2Rio, self).__init__()
        while True:
            try:
                print "Trying connect to the socket server."
                self._s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.connect()
                print "Server connected."
                self._isConnected = True
                break
            except Exception as e:
                print "Failed to connect to the server, try again."
                self._s.close()
                time.sleep(1)
                print e.message
        self.setDaemon(True)
        self.start()
        print "Threading started."


    def connect(self):
        self._s.connect((self.robotRIO_url, self.robotRIO_port))

    def isConnected(self):
        return self._isConnected

    def sendTelemetry(self, value):
        totalsent = 0
        valueWithLine = str(value) + "\n"
        while totalsent < len(valueWithLine):
            sent = self._s.send(valueWithLine[totalsent:])
            if sent == 0:
                raise RuntimeError("Socket connection broken")
            totalsent += sent

if __name__ == "__main__":
    instance = Connection2Rio()
    value = 2
    while True:
        time.sleep(1)
        value = value+3
        instance.setValue(value)
        print "value set as " + str(value)
