import os
import logging
import json
import time
import datetime
from kafka import KafkaClient
from kafka import SimpleProducer
from kafka.consumer import  SimpleConsumer
from processor.energy import EnergyDataset

PRODUCER_DIR = os.path.abspath(os.path.dirname(__file__))

FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='logs/producer.log',format=FORMAT, level=logging.DEBUG)

class EnergyProducer(object):

    def __init__(self, ip_address, topic='energy', port='9092'):
        self.topic = topic  
        self.kafka = KafkaClient(ip_address + ':' + '9092')
        self.producer = SimpleProducer(self.kafka)

    def replay_energy_data(self,WORKING_DIR=PRODUCER_DIR, start=10 ,end=11, year=2015):
        dataset = EnergyDataset()
        for num_month in range(start, end + 1):
            month = datetime.date(1900, num_month, 1).strftime('%b')
            print month
            data = dataset.replay(PRODUCER_DIR, month.lower() ,year)
            response = self.producer.send_messages(self.topic,json.dumps(data))
            print response
        self.producer.stop()

if __name__ == '__main__':
    producer = EnergyProducer(ip_address='192.168.33.30')
    producer.replay_energy_data()