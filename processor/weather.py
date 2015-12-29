import requests
from geopy.geocoders import Nominatim

class MetroDataset(object):

    def __init__(self, address="london", api_key="795c1ff0b7c8af640f1f88310e296cd8"):
        self.api_key = api_key
        self.url = 'http://api.openweathermap.org/data/2.5/weather'
        self.geolocator = Nominatim()

    def publish_current_data(self, address="2 Marsham St, London SW1P 4DF"):
        location = self.geolocator.geocode(" 2 Marsham St, London SW1P 4DF")
        payload = {'lat': str(location.latitude),'lon': str(location.longitude),
                  'APPID': self.api_key}
        api_request = requests.get(self.url, params=payload)
        print api_request.text

if __name__ == '__main__':
    met = MetroDataset()
    met.publish_current_data()

