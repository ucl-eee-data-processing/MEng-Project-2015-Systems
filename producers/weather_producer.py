import logging
import json
import sys
import time
from kafka import SimpleProducer
from kafka import KafkaClient
from kafka.consumer import  SimpleConsumer
from processor.weather import MetroDataset

FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='logs/weather.log',format=FORMAT, level=logging.DEBUG)

class LazarusWeatherProducer(object):

    def __init__(self, ip_address, port='9092'):
        self.energy_topic = 'energy'  
        self.kafka = KafkaClient(ip_address + ':' + '9092')
        self.producer = SimpleProducer(self.kafka)

    def publish_weather_data(self):
        metro = MetroDataset()
        while True:
            print "A"*50
            # Wait for 15 minutes before sending data
            time.sleep(6)
            response = self.producer.send_messages(self.energy_topic,
                                            json.dumps(metro.publish_data()))
        self.producer.stop()

if __name__ == '__main__':
    p = LazarusWeatherProducer(ip_address='10.20.30.12')
    print p.publish_weather_data()