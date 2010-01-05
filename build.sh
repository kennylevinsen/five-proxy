#!/bin/bash

mkdir -p build
mkdir -p build/songs
javac -cp ./:libs/protobuf-2.0.3.jar -d build/ org/joushou/FiveProxy/*.java