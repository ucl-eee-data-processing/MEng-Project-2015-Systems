import requests
import random
from pytz import timezone
from datetime import datetime
from requests.exceptions import RequestException

CORNELL_API_ENDPOINT = "http://portal.emcs.cornell.edu/Campus/Total.Electric" 

class CornellEnergyDataset(object):

    def __init__(self):
        self.base_url = CORNELL_API_ENDPOINT
        self.timezone = timezone('US/Eastern')

    def energy_consumption(self):
        dt = datetime.now(self.timezone)
        unix_time = int(dt.strftime("%s"))
        payload = {'cmd' : 'UTILREAD',
                   'random': int(random.uniform(0,1)*1100),
                   'lasttime': 0}
        try: 
            response = requests.get(CORNELL_API_ENDPOINT, params=payload)
            return response.json()['status'][0]['value']
        except RequestException:
            return None

    def current_time(self):
        dt = datetime.now(self.timezone)
        current_time = datetime.now(self.timezone).strftime('%d/%m/%y %H:%M')
        date_time = current_time.split(" ")
        date_now ,time_now = date_time[0].split("/") ,date_time[1]
        date, month, year = date_now
        year = '20' + year
        time_list = time_now.split(":")
        hour ,minutes = time_list[0], time_list[1]
        if float(minutes)/29 > 1 :
            minutes = 30
        else:
            minutes = 0
        dt1 = datetime(int(year),int(month), int(date), int(hour),minutes)
        return date_time[0], dt1.strftime("%s")

if __name__ == '__main__':
    c = CornellEnergyDataset()
    print c.energy_consumption()
    print c.current_time()
    
