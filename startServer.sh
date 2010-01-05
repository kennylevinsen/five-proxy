#!/bin/bash

cd build
java -cp ./:../libs/protobuf-2.0.3.jar:../libs/sqlitejdbc-v056.jar org.joushou.FiveProxy.Main
