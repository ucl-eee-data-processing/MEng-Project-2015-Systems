#!/bin/bash
mvn package
#sudo rm /home/hadoop/oryx/deploy/bin/lazarus-2.1.1.jar
sudo cp target/lazarus-2.1.1.jar /home/hadoop/oryx/deploy/bin/
sudo chown hadoop /home/hadoop/oryx/deploy/bin/lazarus-2.1.1.jar
rm -rf target
