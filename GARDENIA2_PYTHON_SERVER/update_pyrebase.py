import RPi.GPIO as GPIO
import time
import datetime
import pyrebase
import Adafruit_DHT
#import urllib2, urllib, httplib
import os 
import smbus
import threading
import requests
from pyfcm import FCMNotification

# variables
LOGGER = 1
buttonTest = 23
#switchedOnRelayWater = False
registrationId = ""
timerSwitchedOn = False
ON = 0
OFF = 0
setPlantTemp = 0
setWateringTime = ""
#sensor_pin_ number = 21;

#==piny przekaznikow, servo=======
RELAY_WATER_PUMP = 31
SERVO_WINDOW_OPENER = 32 #PWM
RELAY_HEATER = 37
RELAY_FAN = 38
#==========================

#==czujnik wilgotnosci, temperatury DHT11==
DHT_TYPE = Adafruit_DHT.DHT11
DHT_PIN  = 4
#==========================================

#==konwerter AD/DA PCF8591 + czujnik wilgotnosci==
AIN0 = 10 #fotorezystor
AIN1 = 0x41 #termistor
AIN2 = 12 #AIN2 - podpiety czujnik wilgotnosci
AIN3 = 0x43 #potencjometr
adress_i2c = 0x48
bus_i2c = smbus.SMBus(1)
#=================================================

# ============================================================================================
# GPIO
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(RELAY_WATER_PUMP, GPIO.OUT)
GPIO.setup(SERVO_WINDOW_OPENER, GPIO.OUT)
GPIO.setup(RELAY_FAN, GPIO.OUT)
GPIO.setup(RELAY_HEATER, GPIO.OUT)
pwm = GPIO.PWM(SERVO_WINDOW_OPENER, 50)
pwm.start(0)

GPIO.output(RELAY_WATER_PUMP, 0)#wylacz na start

GPIO.setup(16, GPIO.OUT)
GPIO.setup(15, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

# register callback for each switch retrieved

#===============================================================


#==konfiguracja bazy Firebase==
config = {
    "apiKey": "YOUR API HERE",
    "authDomain": "YOUR API HERE",
    "databaseURL": "YOUR API HERE",
    "storageBucket": "YOUR API HERE"
}
firebase = pyrebase.initialize_app(config)
db = firebase.database()
#==============================

#============FIREBASE CLOUD MESSAGE NOTIFICATION ======
push_service = FCMNotification(api_key="YOUR API KEY HERE")
#======================================================

#==========OPEN WEATHER API====================
openWeatherMapRequest = 'http://api.openweathermap.org/data/2.5/forecast'
paramsOW = dict(
    q='YOUR CITY HERE',
    lang='pl',
    units='metric',
    cnt='5',
    appid='YOUR APID HERE'
)

#==============================================


def readADC(pin):
    bus_i2c.write_byte(adress_i2c, pin)
    value = bus_i2c.read_byte(adress_i2c)

    #if convert == True:
    read = '%.2f' % (100-(value/255*100))
    #else:
    #read = '%.2f' % (value/255*100)

    return read


# LOGGER
def printlog(text):
    if(LOGGER):
        print(text)
        #print('\n')
# LOGGER


# Start script
# ============================================================================
printlog("START SKRYPTU\n")

#Information about temperature, humidity, cpu, disk, RAM

def readDHT22():
    # Get a new reading
    humidity_read, temp_read = Adafruit_DHT.read(DHT_TYPE, DHT_PIN)
    # Save our values
    if humidity_read is None:
        humidity_read = 0
    if temp_read is None:
        temp_read = 0
        
    humidity  = '%.2f' % (humidity_read)
    temp = '%.2f' % (temp_read)

    return (humidity, temp)

# Return CPU temperature as a character string                                      
def getCPUtemperature():
    res = os.popen('vcgencmd measure_temp').readline()
    return(res.replace("temp=","").replace("'C\n",""))

# Return RAM information (unit=kb) in a list                                        
# Index 0: total RAM                                                                
# Index 1: used RAM                                                                 
# Index 2: free RAM                                                                 
def getRAMinfo():
    p = os.popen('free')
    i = 0
    while 1:
        i = i + 1
        line = p.readline()
        if i==2:
            return(line.split()[1:4])

# Return information about disk space as a list (unit included)                     
# Index 0: total disk space                                                         
# Index 1: used disk space                                                          
# Index 2: remaining disk space                                                     
# Index 3: percentage of disk used                                                  
def getDiskSpace():
    p = os.popen("df -h /")
    i = 0
    while 1:
        i = i +1
        line = p.readline()
        if i==2:
            return(line.split()[1:5])

##Date time formatting
dateString = '%d/%m/%Y %H:%M:%S'


def updatePiInfo():

    printlog("## AKTUALIZACJA FIREBASE.. ##")
    db.child("/Settings").update({"last_update_datetime": datetime.datetime.now().strftime(dateString)})

    #retrieve max & min humidity
    maxHumidity = db.child("/Controls/Sensors/Humidity/max_inside").get().val()
    minHumidity = db.child("/Controls/Sensors/Humidity/min_inside").get().val()

    #retrieve max & min temperature
    maxTemperature = db.child("/Controls/Sensors/Temperature/max_inside").get().val()
    minTemperature = db.child("/Controls/Sensors/Temperature/min_inside").get().val()

    #odczyt danych z przetwornika ADC (fotorezystor, czujnik wilgotnosci gleby) 
    lightSensor = readADC(AIN0)
    soilHumidity = readADC(AIN2)
    #pushNotification("UWAGA!!", "Nastapilo update danych")

    #soilFloat = 100.0-float(readADC(AIN2))
    #soilHumidity = '%.2f' % (soilFloat)

    db.child("/Controls/Sensors/Soil").update({"humidity":""+soilHumidity})
    db.child("/Controls/Sensors/Light").update({"photoresistor":""+lightSensor})

    humidity, temperature = readDHT22()
    db.child("/Controls/Sensors/Humidity").update({"current_inside":""+humidity})
    db.child("/Controls/Sensors/Temperature").update({"current_inside":""+temperature})

    #==================OPEN WEATHER API RESPONSE=================
    openWeatherResp = requests.get(url=openWeatherMapRequest, params=paramsOW)
    dataWeather = openWeatherResp.json()

    for weatherStamp in dataWeather['list']:
        printlog("Temperatura na zewnatrz " + str(weatherStamp['dt_txt']) + " : " + str(weatherStamp['main']['temp']))

    for weatherStamp in dataWeather['list']:
        if int(weatherStamp['main']['temp']) <= 0:
            pushNotification("UWAGA!", "Temperatura powietrza spadnie ponizej zera! "+weatherStamp['dt_txt'])
            printlog("Temperatura powietrza spadnie ponizej zera! "+weatherStamp['dt_txt'])
            break
    #===========================================================

    ##check for max values
    if float(humidity) > float(maxHumidity):
        db.child("/Controls/Sensors/Humidity").update({"max_inside":""+humidity})
        printlog("Zaktualizowano Humidity max_inside")
    if float(temperature) > float(maxTemperature):
        db.child("/Controls/Sensors/Temperature").update({"max_inside":""+temperature})
        printlog("Zaktualizowano Temperature max_inside")
        
    ## cehck for min values
    if float(humidity) < float(minHumidity):
        db.child("/Controls/Sensors/Humidity").update({"min_inside":""+humidity})
        printlog("Zaktualizowano Humidity min_inside")
    if float(temperature) < float(minTemperature):
        db.child("/Controls/Sensors/Temperature").update({"min_inside":""+temperature})
        printlog("Zaktualizowano Temperature min_inside")


    #CPU INFO
    CPU_temp = getCPUtemperature()

    #RAM INFO
    RAM_stats = getRAMinfo()
    RAM_total = round(int(RAM_stats[0]) / 1000,1)
    RAM_used = round(int(RAM_stats[1]) / 1000,1)
    RAM_free = round(int(RAM_stats[2]) / 1000,1)

    #DISK INFO
    DISK_stats = getDiskSpace()
    DISK_total = DISK_stats[0]
    DISK_free = DISK_stats[1]
    DISK_perc = DISK_stats[3]
    DISK_used = float(DISK_total[:-1]) - float(DISK_free[:-1])

    data_to_update = {
        "/PI/DISK": {
            "total": str(DISK_total[:-1]),
            "free": str(DISK_free[:-1]),
            "used":  str(DISK_used),
            "percentage": str(DISK_perc)
        },
        "/PI/RAM": {
            "free": str(RAM_free)+"",
            "used": str(RAM_used)+"",
            "total": str(RAM_total)+""
        },
        "/PI/CPU": {
            "temperature": CPU_temp
        }
    }
    db.update(data_to_update)
    
    printlog(datetime.datetime.now().strftime(dateString))
    printlog("Wilgotnosc: Aktualna["+humidity+"], Max["+maxHumidity+"], Min["+minHumidity+"]")
    printlog("Temperatura: Aktualna["+temperature+"], Max["+maxTemperature+"], Min["+minTemperature+"]")
    printlog("CPU temperature: "+CPU_temp)
    printlog("RAM total["+str(RAM_total)+" MB], RAM used["+str(RAM_used)+" MB], RAM free["+str(RAM_free)+" MB]")
    printlog("DISK total["+str(DISK_total)+"], free["+str(DISK_free)+"], perc["+str(DISK_perc)+"]")
    printlog("## Aktualizacja przebiegla pomyslnie ##")
    printlog("======================================================\n")


#switch_number_key = 'switch_number'
#pin_number_key = 'pin_number'


def setServoAngle(Pin, Angle):
    duty = 2+(Angle/18)
    #duty = Angle/1.8
    GPIO.output(Pin, True)
    pwm.ChangeDutyCycle(duty)
    time.sleep(1)
    GPIO.output(Pin, False)
    pwm.ChangeDutyCycle(0)

# angle / 2 + 10
#=======NASLUCHIWANIE zmiany danych elementow wykonawczych w firebase===============
#def lights_handler(message):

    ##GPIO.output(RELAY_LIGHTS, message["data"]) # element wykonawczy
#    printlog("Lights Handler - Nastapila zmiana wartosci w drzewie /Controls/Actuators/Lights_Relay/value na: " + str(message["data"])) 
    ##GPIO.output(RELAY_LIGHTS, (not(bool(message["data"])))) #on relay
    
    ##printlog("Lights Handler - " + str(message["event"]))
    ##printlog("Lights Handler - " + str(message["data"]))

#lights_relay = db.child("/Controls/Actuators/Lights_Relay/value").stream(lights_handler)
#=====================================================
#def watering_handler(message):

    ##GPIO.output(RELAY_LIGHTS, message["data"]) # element wykonawczy
#    printlog("Watering Handler - Nastapila zmiana wartosci w drzewie /Controls/Actuators/Watering_Relay/value na: " + str(message["data"]))  
#    GPIO.output(RELAY_WATER_PUMP, (not(bool(message["data"])))) #on relay
    ##printlog("Watering Handler - " + str(message["event"]))
    ##printlog("Watering Handler - " + str(message["data"]))

#watering_relay = db.child("/Controls/Actuators/Watering_Relay/value").stream(watering_handler)
#=====================================================
#def window_handler(message):

    ##GPIO.output(RELAY_LIGHTS, message["data"]) # element wykonawczy
#    printlog("Window Handler - Nastapila zmiana wartosci w drzewie /Controls/Actuators/Window_Relay/value na: " + str(message["data"])) 
#    setServoAngle(SERVO_WINDOW_OPENER, int(message["data"])) #0-90 degree angle
    ##printlog("Window Handler - " + str(message["event"]))
    ##printlog("Window Handler - " + str(message["data"]))

#window_relay = db.child("/Controls/Actuators/Window_Relay/value").stream(window_handler)
#============================================================




#==nasluchiwanie zadania wyslania biezacych pomiarow do bazy=====
def ref_request_handler(message):
    #print("requested refresh")
    if(message["data"]) == 1:
        printlog("Wymuszono aktualizacje pomiarow z czujnikow do bazy")
        updatePiInfo()

update_request = db.child("/Settings/ref_request").stream(ref_request_handler)
#================================================================


#==nasluchiwanie zmiany tokena messageCloud=====
def tokenMsg_handler(message):
    global registrationId
    registrationId = message["data"]
    #printlog("Nastapila zmiana tokena messageCloud odpowiedzialnego za PushNotification")

update_request = db.child("/Settings/tokenMessaging").stream(tokenMsg_handler)
#================================================


def pushNotification(title, body):

    data_messagee= {
            'priority' : 'high',
            'sound' : 'default',
            'tag': 'example',
            'title' : title,
            'body' : body
    }

    push_service.notify_single_device(registration_id=registrationId, data_message=data_messagee)
    printlog("Wyslany PushNotification: " + title + " " + body)

#==nasluchiwanie zmiany parametrow aktualnej uprawy=====
def current_activity_handler(message):
    dataRead = message["data"]

    global setPlantTemp
    global setWateringTime

    setWateringTime = dataRead["watering"]
    setPlantName = dataRead["plant"]
    setPlantTemp = dataRead["temp"]

    wateringTimeRelay(setWateringTime)

    printlog("Zmiana parametrow aktualnej uprawy: " + setPlantName+" "+setPlantTemp) 

current_activity = db.child("/CurrentActivity").stream(current_activity_handler)
#=======================================================


def wateringTimeRelay(strTime):
    splittedTime = strTime.split("-")
    wateringStart = splittedTime[0].split(":")
    wateringEnd = splittedTime[1].split(":")

    startWateringHour = wateringStart[0]
    startWateringMinute = wateringStart[1]
    endWateringHour = wateringEnd[0]
    endWateringMinute = wateringEnd[1]

    #glowne dzialanie funkcji w timeLoop()
    global ON
    global OFF
    ON = int(startWateringHour) * 60 + int(startWateringMinute)
    OFF = int(endWateringHour) * 60 + int(endWateringMinute)

    printlog("Nowe godziny nawadniania: " + startWateringHour+":"+ startWateringMinute+"-->"+endWateringHour+":"+endWateringMinute)
    
def tempLoop():
    while True:
        try:
            if setPlantTemp != 0:

                tempSet = float(setPlantTemp)
                tempDolna = tempSet - 1.0
                tempGorna = tempSet + 1.0
                histereza = 1.0
                humidity, temperature = readDHT22()
                tempAkt = float(temperature)
                turnOn = True

                if turnOn:
                    if tempAkt < (tempDolna - histereza / 2.0):
                        GPIO.output(RELAY_HEATER, 0) #on relay
                        printlog("GRZALKA ON")
                    elif tempAkt > (tempDolna + histereza / 2.0):
                        printlog("GRZALKA OFF")
                        GPIO.output(RELAY_HEATER, 1) #off relay            
            #xGrzalka := 0;

                    if tempAkt > (tempGorna + histereza / 2.0):
                        printlog("WENTYLATOR ON, SERWO UCHYLA OKNO")
                        setServoAngle(SERVO_WINDOW_OPENER, 90) #0-90 degree angle                        
                        GPIO.output(RELAY_FAN, 0) #on relay        #xWentylator := 1;
                    elif tempAkt < (tempGorna - histereza / 2.0):
                        printlog("WENTYLATOR OFF, SERWO ZAMYKA OKNO")
                        setServoAngle(SERVO_WINDOW_OPENER, 0) #0-90 degree angle    
                        GPIO.output(RELAY_FAN, 1) #off relay        #xWentylator := 0;

                    printlog("Temperatura aktualna: " + temperature + " ---> Zadana: " + str(tempSet))

                else:
                    GPIO.output(RELAY_HEATER, 1) #off relay        
                    GPIO.output(RELAY_FAN, 1) #off relay                

            time.sleep(30)

        except KeyboardInterrupt:
            pwm.stop()
            sys.exit(0)

def timeLoop():

    while True:
        try:
            global timerSwitchedOn

            stm = time.localtime()
            now = stm.tm_hour * 60 + stm.tm_min
            if (now - ON) % 1440 < (OFF - ON) % 1440:
                 timerOn= True
            else:
                 timerOn = False

            if timerOn and not timerSwitchedOn:
                # GPIO pin set to on
                GPIO.output(RELAY_WATER_PUMP, 1) #on relay
                timerSwitchedOn = True
                #printlog("NAWADNIANIE ON: " + str(ON) + " "+ str(OFF))
                printlog("NAWADNIANIE ON: " + setWateringTime)
            elif timerSwitchedOn and not timerOn:
                # GPIO off
                GPIO.output(RELAY_WATER_PUMP, 0) #off relay
                timerSwitchedOn = False
                printlog("NAWADNIANIE OFF: " + setWateringTime)
            
            #regulatorTemp(True, tempAkt, tempSet):
            time.sleep(10)

        except KeyboardInterrupt:
            pwm.stop()
            sys.exit(0)

        #finally:
        #    print("clean up") 
        #    GPIO.cleanup() # cleanup all GPIO


def mainLoop():
    while True:
        try:
            updatePiInfo()
            printlog("")
            #Retrieve sleep time from db and continue the loop
            sleepTime = db.child("/Settings/info_update_time_interval").get().val()
            sleepTime = int(sleepTime)
            time.sleep(sleepTime)
        except KeyboardInterrupt:
            pwm.stop()
            sys.exit(0)



thread1 = threading.Thread(target=mainLoop)
thread1.start()

thread2 = threading.Thread(target=timeLoop)
thread2.start()

thread3 = threading.Thread(target=tempLoop)
thread3.start()
 
