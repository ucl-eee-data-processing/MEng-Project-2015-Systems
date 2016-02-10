import logging
import sys
import json
import time
import datetime
from kafka import KafkaClient
from kafka import SimpleProducer
from kafka.consumer import  SimpleConsumer
from kafka.common import LeaderNotAvailableError
from processor.weather import MetroDataset

FORMAT = '%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s'\
        ':%(process)d:%(message)s' 
logging.basicConfig(filename='logs/weather.log',format=FORMAT, level=logging.DEBUG)

class WeatherProducer(object):

    def __init__(self, ip_address, topic='weather', port='9092'):
        self.topic = topic  
        #self.kafka = KafkaClient(ip_address + ':' + '9092')
        #self.producer = SimpleProducer(self.kafka)

    def publish_weather_data(self):
        metro = MetroDataset()
        while True:
            current_time = datetime.datetime.now().strftime('%d/%m/%y %H:%M')
            date_time = current_time.split(" ")
            time_now = date_time[1]
            time_list = time_now.split(":")
            minutes = time_list[1]
            interval_flag = int(minutes)/30.0
            if interval_flag == 0.0 or interval_flag == 1.0:
                try:
                    response = self.producer.send_messages(self.topic,
                                                json.dumps(metro.publish_data()))
                except LeaderNotAvailableError:
                    time.sleep(1)
                    response = self.producer.send_messages(self.topic,
                                                json.dumps(metro.publish_data()))
                print response
                time.sleep(70)
        self.producer.stop()


    def publish_to_file(self):
        metro = MetroDataset()
        data = {}
        while True:
            try:
                current_time = datetime.datetime.now().strftime('%d/%m/%y %H:%M')
                date_time = current_time.split(" ")
                time_now = date_time[1]
                time_list = time_now.split(":")
                minutes = time_list[1]
                interval_flag = int(minutes)/30.0
                metro_data = metro.publish_data()      
                #if interval_flag == 0.0 or interval_flag == 1.0:
                if data.has_key(metro_data['date']):
                    if not data[metro_data['date']].has_key(metro_data['time']):
                        data[metro_data['date']].update({metro_data['time']: metro_data['data']})
                else:
                    data[metro_data['date']] = {metro_data['time']: metro_data['data']}
                print data
            except KeyboardInterrupt:
                with open('data.json', 'w') as outfile:
                    json.dump(data, outfile)

                sys.exit()


if __name__ == '__main__':
    producer = WeatherProducer(ip_address='10.20.30.12')
    producer.publish_to_file()