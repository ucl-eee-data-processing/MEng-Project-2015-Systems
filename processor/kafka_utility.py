import logging
import json
import sys
from kafka import SimpleProducer
from kafka import KafkaClient
from kafka.consumer import  SimpleConsumer
from energy import EnergyDataset
from weather import MetroDataset


FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='kafka.logs',format=FORMAT, level=logging.DEBUG)

class LazarusProducer(object):

    def __init__(self, ip_address, port='9092'):
        self.energy_topic = 'energy'
        self.weather_topic = 'weather'  
        self.kafka = KafkaClient(ip_address + ':' + '9092')
        self.producer = SimpleProducer(self.kafka)

    def publish_energy_data(self):
        msg = self.producer.send_messages(self.energy_topic,'XX'*50)
        print msg
        self.producer.stop()

    def replay_energy_data(self,start='jul',end='sept'):
        dataset = EnergyDataset()
        data = dataset.replay('jan',2015)
        print '\n'
        print sys.getsizeof(json.dumps(data))
        response = self.producer.send_messages(self.energy_topic,json.dumps(data))
        self.producer.stop()
    
    
    def publish_weather_data(self):
        pass


class LazarusConsumer(object):

    def __init__(self,ip_address, port='9092', topic='energy'):
        self.kafka = KafkaClient(ip_address + ':' + port)
        self.consumer = SimpleConsumer(self.kafka,"my_group", "energy",max_buffer_size=1000000)

    def consume_message(self):
        for message in self.consumer:
            print message

if __name__ == '__main__':
    l = LazarusProducer(ip_address='10.20.30.12')
    l.replay_energy_data()
    c = LazarusConsumer(ip_address='10.20.30.12')
    c.consume_message()

