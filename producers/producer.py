import logging
import json
import time
import datetime
from threading import Thread 
from kafka import KafkaClient
from kafka import SimpleProducer
from kafka.consumer import  SimpleConsumer
from kafka.common import LeaderNotAvailableError
from processor.weather import MetroDataset
from processor.energy import EnergyDataset


FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s'

logging.basicConfig(filename='logs/energy.log',format=FORMAT, level=logging.DEBUG)

class LazarusProducer(Thread):

    def __init__(self, ip_address, topic='OryxTest', port='9092'):
        super(LazarusProducer, self).__init__()
        self.topic = topic  
        self.kafka = KafkaClient(ip_address + ':' + '9092')
        self.producer = SimpleProducer(self.kafka)
        self.energy = EnergyDataset()
        self.weather = MetroDataset()

    def _produce(self):
        date, curr_time = self.weather.current_time()
        data = { 'date': date,
                 'time' : curr_time,
                 'energy': self.energy.energy_consumption(),
                 'weather': self.weather.publish_data()} 
        response = self.producer.send_messages(self.topic,json.dumps(data))

    def run(self):
        while True:
            time.sleep(10)
            self._produce()

if __name__ == '__main__':
    timer = LazarusProducer(ip_address='192.168.33.30')
    timer.start()
    #timer._produce()
    