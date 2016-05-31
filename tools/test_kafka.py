from kafka import SimpleConsumer, SimpleClient

# To consume messages
client = SimpleClient('192.168.33.31:9092')
consumer = SimpleConsumer(client, "my-group", "OryxInput",max_buffer_size=1000000)
for message in consumer:
    # message is raw byte string -- decode if necessary!
    # e.g., for unicode: `message.decode('utf-8')`
    print(message)




