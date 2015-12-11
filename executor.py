from pre_processor.dept_of_transport import DepartmentOfTransportDatasetProcessor

if __name__ == '__main__':
    dataset = DepartmentOfTransportDatasetProcessor()
    print dataset._time_current_dataset()