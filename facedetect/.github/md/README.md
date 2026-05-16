# Video4j - Facedetect

This project contains APIs that provide access to different [Face detection](https://en.wikipedia.org/wiki/Face_detection) implementations via the [Video4j](https://github.com/metaloom/video4j) library.

![ezgif-2-66ea4664a0](https://user-images.githubusercontent.com/326605/213294042-a36913c8-8c94-4194-8e80-bc47ca32f99c.gif)

## Maven

```xml
<dependency>
	<groupId>io.metaloom.video</groupId>
	<artifactId>video4j-facedetect-[variant]</artifactId>
	<version>${project.version}</version>
</dependency>
```

## Status

There are still some kinks which need to be worked out. Thus the library has not yet been published. The API may change in the future.


## Variants

| Variant                              | Description                               |
| ------------------------------------ | ----------------------------------------- |
| video4j-facedetect-opencv            | Uses OpenCV for facedetection. (Uses JNI) |
| video4j-facedetect-dlib              | Uses DLib for facedection.  (Uses JNI)    |
| video4j-facedetect-insightface-http  | Uses insightface face detection server (Uses http)  |
| video4j-facedetect-inspireface       | Uses inspireface (insightface) (Uses FFM) |


### OpenCV

The OpenCV classifier based face detection needs to be initialized before usage.

```java
%{snippet|id=opencv|file=opencv/src/test/java/io/metaloom/video/facedetect/opencv/OpenCVExampleTest.java}
```

Model data:

* [opencv/face_landmark_model.dat](https://raw.githubusercontent.com/opencv/opencv_3rdparty/contrib_face_alignment_20170818/face_landmark_model.dat)
* [opencv/lbfmodel.yaml](https://raw.githubusercontent.com/kurnianggoro/GSOC2017/master/data/lbfmodel.yaml)



### DLib

The face detector implementation loads the needed models automatically.
At the moment two options are available:

* HOG face detector
* CNN face detector which can utilize GPU

```java
%{snippet|id=dlib|file=dlib/src/test/java/io/metaloom/video/facedetect/dlib/DLibExampleTest.java}
```

```java
%{snippet|id=dlib-extract|file=dlib/src/test/java/io/metaloom/video/facedetect/dlib/DLibExampleTest.java}
```

Model data:

* [dlib/dlib_face_recognition_resnet_model_v1.dat](https://github.com/davisking/dlib-models/blob/master/dlib_face_recognition_resnet_model_v1.dat.bz2)
* [dlib/mmod_human_face_detector.dat](http://dlib.net/files/mmod_human_face_detector.dat.bz2)
* [dlib/shape_predictor_68_face_landmarks.dat](https://raw.githubusercontent.com/italojs/facial-landmarks-recognition/master/shape_predictor_68_face_landmarks.dat)


### Insightface (via HTTP)

TBD

### Inspireface


```java
%{snippet|id=inspireface|file=inspireface/src/test/java/io/metaloom/video/facedetect/inspireface/InspirefaceExampleTest.java}
```

```java
%{snippet|id=inspireface-extract|file=inspireface/src/test/java/io/metaloom/video/facedetect/inspireface/InspirefaceExampleTest.java}
```


## Test footage sources

* https://www.pexels.com/video/video-of-woman-being-scanned-8090198/
* https://www.pexels.com/video/video-of-man-getting-examined-8090418/
* https://www.pexels.com/video/frustrated-young-guy-yelling-over-his-laptop-5125919/
* https://www.pexels.com/video/man-people-office-relationship-4100353/
* https://www.pexels.com/video/portrait-of-young-woman-touching-her-necklace-7626566/
* https://www.pexels.com/video/an-upset-man-touching-his-head-8164110/
* https://www.pexels.com/video/man-and-woman-talking-to-each-other-inside-the-office-5977460/
* https://www.pexels.com/video/meeting-new-client-4428751/
* https://www.pexels.com/video/a-man-and-a-woman-working-while-eating-at-a-cafe-5977265/


## Release Process

```bash
# Update maven version to next release
mvn versions:set -DgenerateBackupPoms=false

# Now run tests locally or via GitHub actions
mvn clean package

# Deploy to maven central and auto-close staging repo. 
# Adding the property will trigger the profiles in the parent pom to include gpg,javadoc...
mvn clean deploy -Drelease
```