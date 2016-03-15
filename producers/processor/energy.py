import os
import urllib
import json
import datetime
import csv
from time import strptime
from calendar import monthrange
from calendar import month_abbr

WORKING_DIR = os.getcwd()


class EnergyDataset(object):

    def __init__(self, offset=None, year=None, month=None):
        self.base_url = 'http://webview.ecodriver.net/eCMS/Files/DFT/'

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
        for file in os.listdir(WORKING_DIR):
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
                if date in row.values():
                    data_list.append(row)
        for data in data_list:
            data_points.update({data['Meter']: data[time]})
        return data_points


    def _download_dataset(self, filename):
        link_address = self.base_url + filename
        response = urllib.urlretrieve(link_address, filename)

    def energy_consumption(self,date=None):
        time_now = datetime.datetime.now().strftime('%d/%m/%y %H:%M')
        date_time = time_now.split(" ")
        date ,curr_time = date_time[0] ,date_time[1]
        month = date.split("/")[1]
        year = '20' + date.split("/")[2]
        prev_year = '20' + str(int(date.split("/")[2]) - 1)
        prev_date = date.split("/")[0] + '/' + date.split("/")[1] + '/' + prev_year
        if month[0] == '0':
           month = month_abbr[int(month[1])].lower()
        else:
            month = month_abbr[int(month)].lower()
        time_list = curr_time.split(":")
        if int(time_list[1]) < 30 :
            minutes = '00'
        else:
            minutes = '30'
        time_stamp = time_list[0] + ':' + minutes
        curr_file = self._generate_filename(month=month,year=year)
        prev_file = self._generate_filename(month=month,year=prev_year)
        if os.path.isfile(WORKING_DIR + '/' + curr_file) or os.path.isfile(WORKING_DIR + '/' + prev_file) :
            if os.path.isfile(WORKING_DIR + '/' + curr_file):
                return self._get_datapoint(curr_file,date,time_stamp)
            else:
                return self._get_datapoint(prev_file,prev_date,time_stamp)
        else:
            link_address = self.base_url + curr_file
            response = urllib.urlopen(link_address)
            if response.getcode() == 404 :
                self._download_dataset(prev_file)
                return self._get_datapoint(prev_file,prev_date,time_stamp)
            else:
                self._download_dataset(curr_file)
                return self._get_datapoint(curr_file,date,time_stamp)


    def _replay_helper(self, month, year):
        filename = self._generate_filename(month=month, year=year)
        numerical_month = strptime(month,'%b').tm_mon
        month_range = monthrange(year, numerical_month)
        month_data = {}
        for iter_date in range(1, month_range[1]):
            if len(str(iter_date)) == 1:
                iter_date = '0' + str(iter_date)
            if len(str(numerical_month)) == 1:
                numerical_month = '0' + str(numerical_month)
            date = str(iter_date) + '/' + str(numerical_month) + '/' + str(year)
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

    def replay(self, WORKING_DIR, month, year):
        filename = self._generate_filename(month=month, year=year)
        if os.path.isfile(WORKING_DIR + '/' + filename):
            month_dataset = self._replay_helper(month=month, year=year)
        else:
            self._download_dataset(filename)
            month_dataset = self._replay_helper(month=month, year=year)
        os.remove(os.path.join(WORKING_DIR, filename))
        return month_dataset

if __name__ == '__main__':
    dataset = EnergyDataset()
    filename = dataset._generate_filename(month='mar',year='2015')
    #dataset._download_dataset(filename)
    #print dataset._get_datapoint(filename,'01/03/2015', '00:00')
    print dataset.energy_consumption()


