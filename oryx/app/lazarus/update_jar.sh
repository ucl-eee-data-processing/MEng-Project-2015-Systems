#!/bin/bash
mvn package
sudo rm /home/hadoop/java8/oryx/deploy/bin/lazarus-2.1.1.jar
sudo cp target/lazarus-2.1.1.jar /home/hadoop/java8/oryx/deploy/bin/
sudo chown hadoop /home/hadoop/java8/oryx/deploy/bin/lazarus-2.1.1.jar
rm -rf target
