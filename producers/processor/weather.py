import os
import requests
import datetime
import json
import time
from time import strptime
from requests.exceptions import ConnectionError
from geopy.geocoders import Nominatim

PROCESSOR_DIR = os.path.abspath(os.path.dirname(__file__))

class MetroDataset(object):

    def __init__(self, api_key="795c1ff0b7c8af640f1f88310e296cd8", address="2 Marsham St, London SW1P 4DF"):
        self.api_key = api_key
        self.url = 'http://api.openweathermap.org/data/2.5/weather'
        #self.geolocator = Nominatim()
        #self.location = self.geolocator.geocode(address)

    def current_time(self):
        current_time = datetime.datetime.now().strftime('%d/%m/%y %H:%M')
        date_time = current_time.split(" ")
        date ,time_now = date_time[0] ,date_time[1]
        time_list = time_now.split(":")
        hour ,minutes = time_list[0], time_list[1]
        if float(minutes)/29 > 1 :
            minutes = "30"
        else:
            minutes = "00"
        return date ,hour + ":" + minutes

    def _current_data(self):
        #self.location.latitude
        payload = {'lat': str(51.5,),'lon': str(-0.12),
                  'APPID': self.api_key}
        try:
            # Decode Unicode
            api_request = requests.get(self.url, params=payload)
            return api_request.json()
        except ConnectionError:
            return None

    def publish_data(self):
        timestamp = self.current_time()
        if timestamp != None:
            return self._current_data()['main']
        else:
            return None

if __name__ == '__main__':
    met = MetroDataset()
    print met.publish_data()

