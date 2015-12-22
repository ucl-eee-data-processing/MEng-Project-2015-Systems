import os
import wget
import datetime
import csv
import glob
from time import strptime
from calendar import monthrange

PROCESSOR_DIR = os.path.abspath(os.path.dirname(__file__))


class DepartmentOfTransportDatasetProcessor(object):

    def __init__(self, offset=None, year=None, month=None):
        self.base_url = 'http://www.ecodriver.uk.com/eCMS/Files/DFT/'

    def _generate_filename(self, month=None, year=None):

        if month == None and year == None:
            time_now = datetime.datetime.now()
            month = time_now.strftime('%B')[:3].lower()
            year = time_now.year
            filename = 'deptfortransport_' + month + '-' + str(year) + '.csv'
            return filename
        else:
            return 'deptfortransport_' + month + '-' + str(year) + '.csv'

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
        data_list = []
        data_points = {}
        with open(filename, 'rb') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                #print row
                if date in row.values():
                    data_list.append(row)
        for data in data_list:
            data_points.update({data['Meter']: data[time]})
        return data_points

    def _download_dataset(self, filename):
        link_address = self.base_url + filename
        wget.download(link_address)

    def remove_dataset(self, filename):
        pass

    def replay(self, month, year):
        filename = self._generate_filename(month=month, year=year)
        numerical_month = strptime(month,'%b').tm_mon
        month_range = monthrange(year, numerical_month)
        month_data = {}
        if os.path.isfile(PROCESSOR_DIR + '/' + filename):
            for iter_date in range(month_range[0], month_range[1] +1):
                # Month is hard coded
                if len(str(iter_date)) == 1:
                    iter_date = '0' + str(iter_date)
                date = str(iter_date) + '/' + '11' + '/' + str(year)
                date_data = {}
                for iter_minutes in range(0,1440,30):
                    hour = iter_minutes // 60
                    minutes = iter_minutes % 60
                    if len(str(hour)) == 1:
                        hour = '0' + str(hour)
                    if len(str(minutes)) == 1:
                        minutes = '0' + str(minutes)
                    time = str(hour) + ':' + str(minutes)
                    datapoint = self._get_datapoint(filename ,date, time)
                    date_data.update({time: datapoint})
                month_data.update({date: date_data})
            return month_data            
        else:
            self._download_dataset(filename)



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
            # There is no current data 

if __name__ == '__main__':
    dataset = DepartmentOfTransportDatasetProcessor()
    #print dataset._time_current_dataset()
    #dataset.publish_real_time_data()
    #print dataset._get_datapoint('deptfortransport_dec-2015.csv' , '1/11/15', '00' + ':30')
    print dataset.replay('dec',2015)['02/11/2015']


