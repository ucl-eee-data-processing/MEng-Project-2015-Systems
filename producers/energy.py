import logging
import json
import sys
import time
from kafka import SimpleProducer
from kafka import KafkaClient
from kafka.consumer import  SimpleConsumer
from weather import MetroDataset

FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='/logs/weather.log',format=FORMAT, level=logging.DEBUG)

class LazarusWeatherProducer(object):

    def __init__(self, ip_address, port='9092'):
        self.energy_topic = 'energy'  
        #self.kafka = KafkaClient(ip_address + ':' + '9092')
        #self.producer = SimpleProducer(self.kafka)

    def publish_weather_data(self):
        print "xxxxx"
        metro = MetroDataset()
        while True:
            #time.sleep(3600)
            print metro.publish_data()

if __name__ == '__main__':
    p = LazarusWeatherProducer()
    print p.publish_weather_data()