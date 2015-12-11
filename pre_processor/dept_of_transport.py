import os
import wget
import datetime
import csv
import glob
from time import strptime

PROCESSOR_DIR = os.path.abspath(os.path.dirname(__file__))


class DepartmentOfTransportDatasetProcessor(object):

    def __init__(self, offset=None, year=None, month=None):
        self.base_url = 'http://www.ecodriver.uk.com/eCMS/Files/DFT/'
        self.offset = offset
        self.month = month
        self.year = year 

    def _serialize(self):
        pass

    def _generate_filename(self):
        # TODO add defualts replay might need this too
        time_now = datetime.datetime.now()
        month = time_now.strftime('%B')[:3].lower()
        year = time_now.year
        filename = 'deptfortransport_' + month + '-' + str(year) + '.csv'
        return filename

    def _time_current_dataset(self):
        dataset = ''
        for file in os.listdir(PROCESSOR_DIR):
            if file.endswith(".csv"):
                dataset = file
        try:
            dataset_month = dataset.split("_")[1].split("-")[0]
            return strptime(dataset_month,'%b').tm_mon
        except IndexError:
            return None 

    def download_dataset(self):
        # TODO check whether file exist
        link_address = self.base_url + self._generate_filename()
        wget.download(link_address)

    def remove_dataset(self, filename):
        pass

    def replay(self,from_date):
        # Replays the data from a certain date to today
        pass

    def publish_data(self):
        # Check the date today
        # Check current time if minutes 0 : 30 read previous hour
        # if minutes  30 - 59 read  data point at half pass
        time = datetime.datetime.now().strftime('%d/%m/%y %H:%M')
        time_arr = time.split(" ")
        date = time_arr[0]
        hours = time_arr[1]
        print date 
        print hours

if __name__ == '__main__':
    dataset = DepartmentOfTransportDatasetProcessor()
    print dataset._time_current_dataset()
    dataset.publish_data()


