import requests
import random
import json
from pytz import timezone
from datetime import datetime
from requests.exceptions import RequestException

CORNELL_API_ENDPOINT = "http://portal.emcs.cornell.edu/Campus/Total.Electric" 

class CornellEnergyDataset(object):

    def __init__(self):
        self.base_url = CORNELL_API_ENDPOINT
        self.timezone = timezone('US/Eastern')
    def consumption(self):
        dt = datetime.now(self.timezone)
        unix_time = int(dt.strftime("%s")) - 15*60
        payload = {'cmd' : 'UTILREAD',
                   'random': random.randint(10000,99999),
                   'lasttime': unix_time}
        try: 
            response = requests.get(CORNELL_API_ENDPOINT, params=payload)
            return json.loads(response.text)['status'][0]['value']
        except RequestException:
            raise RequestException


if __name__ == '__main__':
    c = CornellEnergyDataset()
    print c.consumption()
    
