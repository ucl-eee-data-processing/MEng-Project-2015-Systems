import logging
import json
import sys
import time
from kafka import SimpleProducer
from kafka import KafkaClient
from kafka import  KafkaConsumer
from kafka.consumer import  SimpleConsumer
from energy import EnergyDataset
from weather import MetroDataset

FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='kafka.logs',format=FORMAT, level=logging.DEBUG)


class LazarusConsumer(object):

    def __init__(self,ip_address, port='9092', topic='energy'):
        #self.kafka = KafkaClient(ip_address + ':' + port)
        #self.consumer = SimpleConsumer(self.kafka,"my_group", "weather",max_buffer_size=1000000)
        self.consumer = KafkaConsumer('weather', group_id='my_group',
                                    bootstrap_servers=[ip_address + ':9092'])
     
    def consume_message(self):
        data = []
        try:
            for message in self.consumer:
                data.append(message.value)
            print data
        except KeyboardInterrupt:
            with open("weather.json", "w") as myfile:
                myfile.write(json.dumps(data))



if __name__ == '__main__':
    #l = LazarusProducer(ip_address='10.20.30.12')
    #l.replay_energy_data()
    #l.publish_weather_data()
    c = LazarusConsumer(ip_address='10.20.30.12')
    c.consume_message()


