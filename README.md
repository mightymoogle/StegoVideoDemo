# Video Steganography / Watermarking Demo Tool

## About this project

This is a part of a tool developed by **David Griberman** in 2014 during his Master thesis. 
It was cleaned up and published in 2022 as a command line application.

The code is provided as-is without any warranties or tests. I do not plan to actively maintain this repository. 
Some features have been removed from the code. Please use for reference only on your own risk.

The demo is written in Java and uses the [Xuggler library](http://www.xuggle.com/xuggler/). 
Please note that the library is no longer supported and uses native C++ calls and may cause issues.
I recommend using other approaches if you need to implement video steganography/watermarking yourself.
Application tested only on Windows, does not include any error handling and is very basic. No support will be provided.

## GitHub page
Visit our [GitHub page](https://github.com/mightymoogle/StegoVideoDemo) for the full source code.

## Introduction
You can read a paper that used the original version of the tool to get a better understanding of its features:
[Griberman, David & Rusakov, Pavel. (2016). Comparison of Video Steganography Methods for Watermark Embedding.
Applied Computer Systems. 19. 10.1515/acss-2016-0007.](
https://www.researchgate.net/publication/303914738_Comparison_of_Video_Steganography_Methods_for_Watermark_Embedding)

Please note that some features mentioned could have been removed from this demo (GUI for example).

## What is supported

**The tool currently only supports embedding and extracting black and white BMP images into H264 video files!**
Sound streams and multiple video streams are ignored. 
Xuggler may allow embedding into other video formats on your own risk.

**Please use the latest 32 bit version of Java 8 JDK** to run the project,
as Xuggler has issues with 64 bit versions (at least on Windows).

The watermark is embedded into each frame of the video (with some exceptions). 
During extraction the watermark is extracted from each frame of the video separately and the resulting watermark 
is averaged out from the individual watermarks.

### Features
* Embedding with 4 steganographic algorithms
    * **Kaur** - B. Kaur, A. Kaur and J. Singh, “Steganographic Approach for Hiding
      Image in DCT Domain,” Int. J. of Advances in Engineering & Technology, vol. 1, issue 3, pp. 72–78, July 2011.
    * **Kothari** - A. M. Kothari and V. V. Dwivedi, “Performance Analysis of Digital
      Video Watermarking using Discrete Cosine Transform,” Int. J. of
      Electrical and Computer Engineering Systems, vol. 2, no. 1, pp. 11–16, 2011.
    * **Dubai** - S. AL-Mansoori and A. Kunhu, “Robust Watermarking Technique based
      on DCT to Protect the Ownership of DubaiSat-1 Images agains
      Attacks,” IJCSNS Int. J. of Computer Science and Network Security,
      vol. 12, no. 6, June 2012.
    * **Haar** - K. R. Chetan and K. Raghavendra, “DWT Based Blind Digital Video
      Watermarking Scheme for Video Authentication,” Int. J. of Computer
      Applications, vol. 4, no. 10, pp. 19–26, August 2010. http://dx.doi.org/10.5120/863-1213
* Encoding without any embedding for measurement and testing purposes (**NULL algorithm**).
* Extracting of the embedded watermarks from video files
* PSNR calculations for the videos
* Adaptive embedding mode (adjusts embedding based on movement in the video, but very slow, 
  please refer to the original research paper)
* Attack simulations for robustness testing:
    * Compression (1 / 0.1)
    * Compression (0.5 / 0.1)
    * Overlay (please note that the overlay is not configurable at this time)
    * Resize (Size / 2)
    * Flip (Horizontal)
    * Crop (16:9 to 2.39:1)    

## Building the project
The project uses **Maven** as the build tool. Please note that it uses a custom 
Maven repository www.dcm4che.org which hosts the [Xuggler library](http://www.xuggle.com/xuggler/).

You can simply compile and run the project main class `org.chaosdragon.stegovideo.StegoVideoApp` 
or use the Maven `package` task to build an executable .jar file with dependencies.

Please see CLI parameters for the parameters that need to be specified to run it.

## Usage and CLI parameters

The application can be used in 3 modes by specifying the first parameter: `embed`, `extract` or `psnr`. 
Type them without any additional parameters to see help information.

**The extraction algorithm settings should match the ones used for embedding!**

Usage examples:
```
embed -c R:\riga.mp4 -o R:\riga_encoded.mp4 -w R:\mark_logo.bmp -a 1
extract -s R:\riga_encoded.mp4 -o R:\result.png -width 90 -height 90 -a 1
extract -s R:\riga_encoded.mp4 -o R:\result.png -w R:\mark_logo.bmp -a 1 --watermark-path R:\output
psnr -c R:\riga_compressed.mp4 -s R:\riga_encoded.mp4
```

## Algorithm settings
All algorithms support various parameters. The main ones:

1. `Embedding strength` - this controls how well the watermark is embedded during the DCT transformation. 
   Larger number leads to a more robust watermark, but will most likely be more visible and may result in a lower PSNR.
   The value depends on the algorithm selected and on the video container used.
2. `Compression` - this controls how much the image gets compressed. 
   We recommend keeping it at 0 for minimum additional compression. 2 attacks support compression of the video stream.
3. `Block size` - this controls the block size of the DCT transformation. We recommend keeping it at 8.
4. `Adaptive mode`  - this enables the Adaptive mode described in the article in the introduction. 
   It embeds based on the movement in the video and ignores monotone images. 
   Please note - implementation is very slow and basic. Only `Kaur` and `Kothari` algorithms supported.

## Sample files
The folder `samples` includes 3 short sample video files for testing:
* `flags.mp4` - video of multiple colored flags quickly moving in the wind with lots of movement;  
* `flowers.mp4` - video of red flowers in the wind with some movement;
* `riga.mp4` - video featuring the City of Riga, almost no visible movement.

Also, there are multiple sample watermarks provided:
* `mark_logo.bmp` - logo with the Riga Technical University logo (BMP, Black&White, 90x90 pixels);
* `mark.bmp` - logo with the letters RTU (BMP, Black&White, 90x90 pixels);
* `mark2.bmp` - sames as mark.bmp, but size is 171x96 (stretched for widescreen);
* `mark3.bmp` - same as mark.bmp, but size is 10x10 pixels;
* `mark4.bmp` - same as mark.bmp, but size is 70x70 pixels.

### License - GPLv3
The demo project is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).
The included sample video and watermark files are not covered by the licence and should not be used in your projects.
