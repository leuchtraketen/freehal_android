#!/bin/bash

adb shell dumpsys meminfo | grep "freehal.app"
