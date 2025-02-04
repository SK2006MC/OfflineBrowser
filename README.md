# OfflineBrowser

A browser that stores everything locally.

## Description

OfflineBrowser is a unique browser designed to store all web content locally. This allows users to access and browse their favorite websites without needing an internet connection. This project is primarily written in Java.

DO NOT USE SENSITIVE INFORMATIONS IN THIS BROWSER LIKE PASSWORDS,EMAIL,OTP,ETC..

## Features

- Store web pages locally
- Access stored web pages offline
- Simple and intuitive user interface
- Has the Option to update the stored webpages and contentents.
- Has the Option to Download webpages and contents.
- A javascript console.

## Installation

To install and run OfflineBrowser, follow these steps:

### 1.Method
Click [here](/release) to download the apk and then install.
Now it ready to use.

### 2.Method
1. Clone the repository:
   ```sh
   git clone https://github.com/SK2006MC/OfflineBrowser.git
   ```
2. Navigate to the project directory:
   ```sh
   cd OfflineBrowser
   ```
3. Build the apk:
  ```sh
  ./gradlew assembleRelease
  ```
4. Or Debug:
  ```sh
  ./gradlew assembleDebug
  ```
5. install it to your phone by sending the apk,the apk will be located at `build/output/release` or `debug`.
6. or enable usb debugging in you phone ,allow access for the computer to install the apk.to install run
  ```sh
	./gradlew installAssemble
  ```
  or
  ```sh
	./gradlew installDebug
  ```
## Usage

1. Open the application.
  1.1. If its the first time select the location(folder) where you want to store the webpages.
  1.2. Then click Start.
2. Swipe from left side of the screen to right side to access the URL bar.
3. Enter the URL and Clike the Load Url button.
4. The webpage will be automatically stored in the locatiom you specified.

## Contributing

Contributions are welcome! Please create a pull request or open an issue for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For any questions or inquiries, please contact [SK2006MC](https://github.com/SK2006MC).
