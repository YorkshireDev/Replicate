# Replicate
Project Replicate

---

# Summary

Replicate is a file copying utility made in Java that can copy individual file(s) or folder(s) to a destination directory. The program supports checksumming the copied
data and will automatically retry the copy if something checksums don't match. The program is also multithreaded for those copying files over a high-bandwidth network
or on/between SSDs.

---

# Requirements

1. **Windows**\*, **Linux**\** or **macOS**\***
2. **Java 17** Runtime or Greater
3. (For Developers Only) **Apache Commons IO 2.11.0**, **Apache Commons Codec 1.15** and **Zip4j**

\* Developed and Tested on Windows 10 21H2 64-bit

\** Untested on Linux, but it is Java with well-known dependencies so it should work.

\*** Untested on macOS, but it is Java with well-known dependencies so it should work.

---

# Features (Current)

1. Ability to select single or multiple file(s), and single or multiple folder(s).

2. User-toggalable option for checksumming, can be turned off if the user wishes (speed advantage at risk of data loss). Is enabled by default.

3. User-toggalable option for multithreading, can be turned off if the user wishes (will most likely be faster turned OFF on a HDD or slow SD cards). Is enabled by default.

4. Graphical user interface that shows the stage (copy(ing), checksum, completed) that each individual file/folder is in.

5. Multithreaded copying (as mentioned above), each item in the transfer list will get assigned its own thread to copy in parallel. Thread-count is limited by number of CPU threads. Smaller files will have more parallelism than larger files in an attempt to not bog down a drive.

6. Checksum (as mentioned above) will not only verify integrity of files, but will restart the copy for that file if the file is corrupted in-flight.

7. Ability to set a limit on how many times to retry a transfer/checksum (default is 8). Set it to -1 for infinite retries.

8. Duplicate prevention and other input filtering are present.

9. Option to compress the selected files/folders at the end (may be very slow!)

10. Select items in the transfer list (clicking, shift-clicking, control-clicking) and then click the "Remove Item(s)" button to remove items before copying.

---

# Features (Upcoming)

1. Command-line-interface version to run headlessly in terminals.

2. Ability to remove files from the transfer list in-case of mistakes.

---

# Usage

1. Double click the `Replicate.jar` file provided, or open up a terminal and type `java -jar Replicate.jar`

2. Click the "Select Source" button and choose a file, multiple files (with ctrl-click or shift-click), a folder, or multiple folders (with ctrl-click or shift-click), and then press "Select".

3. Continue to repeat this process until you've selected everything you wanted to copy from anywhere on the system.

4. Click the "Select Destination" button and choose a folder. This is where all the file(s) and/or folder(s) will be copied to. Directory structures are preserved.

5. Click the "Start" button.

6. Once all the items are in the "Completed List" on the right, the copy is fully complete and you may either continue or close the program.

7. Feel free to toggle the checkboxes as described in the "Options" menu. Note that these apply once "Start" is pressed and cannot be modified during a copy.
