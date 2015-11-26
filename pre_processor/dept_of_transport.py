import wget
import datetime
import csv

class DepartmentOfTransportDatasetProcessor(object):

    def __init__(self, url):
        self.base_url = url

    def _serialize(self):
        pass

    def _generate_filename(self):
        # TODO add defualts replay might need this too
        time_now = datetime.datetime.now()
        month = time_now.strftime('%B')[:3].lower()
        year = time_now.year
        filename = 'deptfortransport_' + month + '-' + str(year) + '.csv'
        return filename

    def _retrieve_data(self):
        # TODO check whether file exist
        link_address = self.base_url + self._generate_filename()
        print link_address
        response = wget.download(link_address)

    def _delete_files(self):
        pass

    def replay(self,from_date):
        # Replays the data from a certain date to today
        pass

    def publish_data(self):
        pass

if __name__ == '__main__':
    dataset = DepartmentOfTransportDatasetProcessor(
                                'http://www.ecodriver.uk.com/eCMS/Files/DFT/')
    dataset._retrieve_data()


