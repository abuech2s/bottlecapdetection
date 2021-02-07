# BottleCap Detection
CV20 BottleCap Detection

created by Alexander Buechel
Delivery date: 07.02.2021 at CV20 course

## Intention

* Java based program to detect bottle caps in mp4 files.
* Traditional algorithmic steps are implemented: Load MP4-Files, Detect static scene, Detect ROI, Detect Objects, Classification at objects
* Bottle Cap Face Up, Bottle Cap Face Down, Bottle Cap Deformed, Distractors

* Backend: Uses JavaCv (1.5.5-SNAPSHOT), Mvn, Adopt JDK 11

## How to build

### Windows:
```shell
mvn clean package -DskipTests -Djavacpp.platform=windows-x86_64
```

### Linux:
```shell
mvn clean package -DskipTests -Djavacpp.platform=linux-x86_64
```
## Usage

### Linux:
Call: detect.sh <Path-to-mp4-file> <Path-to-result-dir>
Example: detect.sh ~/Desktop/BottleCap/testdata/CV20_video_1.mp4 ~/Desktop/BottleCap/testdata

### Windows:
Call: detect.bar <Path-to-mp4-file> <Path-to-result-dir>
Example: detect.bat c:\\tmp\\BottleCap\\testdata\\CV20_video_1.mp4 c:\\result

## Technical information
* You should have >8GB Memory (especially at virtual machines)

## Results

Are written into csv-file in Result-Directory

