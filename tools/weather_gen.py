import json
TEMP = {"clouds": 
                            {"all": 0}, 
                            "name": "City of Westminster", 
                            "coord": {"lat": 51.5, "lon": -0.12},
                             "sys": 
                                {"country": "GB",
                                 "message": 0.0054, 
                                 "sunset": 1455210512, 
                                 "sunrise": 1455175299}, 
                                 "weather": [{"main": "Clear", "id": 800, "icon": "01n", "description": "Sky is Clear"}],
                                 "cod": 200,
                                 "base": "cmc stations", 
                                 "dt": 1455211056, 
                                 "main": 
                                    {"temp": 275.509, 
                                      "grnd_level": 1006.26, 
                                      "temp_max": 275.509, 
                                      "sea_level": 1016.36, 
                                      "humidity": 89, 
                                      "pressure": 1006.26, 
                                      "temp_min": 275.509},
                                 "id": 2634341, 
                                 "wind": {"speed": 1.61, "deg": 173.003}}

WEATHER_DATA ={"11/02/16":{}}
for iter_minutes in range(0,1440,30):
    hour = iter_minutes // 60
    minutes = iter_minutes % 60
    if len(str(hour)) == 1:
        hour = '0' + str(hour)
    if len(str(minutes)) == 1:
        minutes = '0' + str(minutes)
    time = str(hour) + ':' + str(minutes)
    WEATHER_DATA["11/02/16"].update({time:TEMP})

with open("weather.json", "w") as myfile:
    myfile.write(json.dumps(WEATHER_DATA))

    