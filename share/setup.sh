adb="/Users/jsvirzi/Library/Android/sdk/platform-tools/adb"
tgtDirectory="/storage/emulated/0/Download/" 
${adb} push beep.mp3 ${tgtDirectory}
${adb} push may-i-have-your-attention.mp3 ${tgtDirectory}
${adb} push no-trespassing.mp3 ${tgtDirectory}
${adb} push tiny-bell.mp3 ${tgtDirectory}
${adb} install rocketlauncher222.apk 
# ${adb} push rocketlauncher.apk ${tgtDirectory}

