Multi-party face blur app

Vision - 
Privacy app to help users blur their faces from pictures being taken by random users in a public setting. 

Assumption - 
- Users wishing to have their pictures blurred should be anonymously sending their location to centralized server
- User taking pictures is using this application


current bugs - 
LocationService.java - ensure service stops gracefully in exceptions
caused by errors related to Network Connectivity or when Stop request is sent

LocationService.java - make code independent of mRunService flag

LocationService.java - pull out reusable functions needed for future calls to server and append to Utility.java

features to add - 
CaptureImageActivity.java - separate code to take image and parse images, this activity should only take a picture, save it and pass metadata (direction camera was pointing to) to the processing activity

DetectFacesActivity.java - take image from CaptureImageActivity, detect all
faces, get positional metadata of users in proximity requesting their faces to be removed, map position of faces in image to location of users, remove matching faces

Currently location based mapping is being tested and is not integrated and
FdActivity.java both captures the image and processes all faces in the
image
