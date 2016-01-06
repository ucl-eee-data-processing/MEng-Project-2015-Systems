import logging
import json
import time
from kafka import KafkaClient
from kafka import SimpleProducer
from kafka.consumer import  SimpleConsumer
from processor.weather import MetroDataset

FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='logs/weather.log',format=FORMAT, level=logging.DEBUG)

class WeatherProducer(object):

    def __init__(self, ip_address, topic='weather', port='9092'):
        self.topic = topic  
        self.kafka = KafkaClient(ip_address + ':' + '9092')
        self.producer = SimpleProducer(self.kafka)

    def publish_weather_data(self):
        metro = MetroDataset()
        while True:
            # Wait for 15 minutes before sending data
            time.sleep(10)
            response = self.producer.send_messages(self.topic,
                                            json.dumps(metro.publish_data()))
            print response
        self.producer.stop()

if __name__ == '__main__':
    producer = WeatherProducer(ip_address='10.20.30.12')
    producer.publish_weather_data()