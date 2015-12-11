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

    def _current_dataset_month(self):
        dataset = ''
        for file in os.listdir(PROCESSOR_DIR):
            if file.endswith(".csv"):
                dataset = file
        try:
            dataset_month = dataset.split("_")[1].split("-")[0]
            return strptime(dataset_month,'%b').tm_mon
        except IndexError:
            return None

    def _get_datapoint(self, filename, date, time):
        print time
        print date
        data_list = []
        data_points = {}
        with open(filename, 'rb') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                if date in row.values():
                    data_list.append(row)
        for data in data_list:
            data_points.update({data['Meter']: data[time]})
        return data_points

    def download_dataset(self):
        # TODO check whether file exist
        link_address = self.base_url + self._generate_filename()
        wget.download(link_address)

    def remove_dataset(self, filename):
        pass

    def replay(self,from_date):
        # Replays the data from a certain date to today
        pass

    def publish_real_time_data(self):
        time = datetime.datetime.now().strftime('%d/%m/%y %H:%M')
        date_time = time.split(" ")
        date ,time_today = date_time[0] ,date_time[1]
        if self._current_dataset_month() == int(date.split("/")[1]):
            time_list = time_today.split(":")
            hour ,minutes = time_list[0], time_list[1]
            if int(minutes) < 31:
                #self._generate_filename() 
                datapoint = self._get_datapoint('deptfortransport_dec-2015.csv' ,'26/11/2015', hour + ':00')
                print datapoint
            else:
                datapoint = self._get_datapoint('deptfortransport_dec-2015.csv' , '26/11/2015', hour + ':30')
                print datapoint
        else:
            pass 
            # Remove the old one and download the lastest one

if __name__ == '__main__':
    dataset = DepartmentOfTransportDatasetProcessor()
    #print dataset._time_current_dataset()
    dataset.publish_real_time_data()


