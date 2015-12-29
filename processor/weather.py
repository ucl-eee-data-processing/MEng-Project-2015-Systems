import requests
from requests.exceptions import ConnectionError
from geopy.geocoders import Nominatim

class MetroDataset(object):

    def __init__(self, api_key="795c1ff0b7c8af640f1f88310e296cd8"):
        self.api_key = api_key
        self.url = 'http://api.openweathermap.org/data/2.5/weather'
        self.geolocator = Nominatim()

    def publish_current_data(self, address="2 Marsham St, London SW1P 4DF"):
        location = self.geolocator.geocode(address)
        payload = {'lat': str(location.latitude),'lon': str(location.longitude),
                  'APPID': self.api_key}
        try:
            # Decode Unicode
            api_request = requests.get(self.url, params=payload)
            return api_request.json()
        except ConnectionError:
            return None

if __name__ == '__main__':
    met = MetroDataset()
    print met.publish_current_data()

