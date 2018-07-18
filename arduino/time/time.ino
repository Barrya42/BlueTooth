#include <SoftwareSerial.h>

SoftwareSerial mySerial(6, 8); // указываем пины RX и TX
int pinReley = 4;
char openChar = '1';
String helloStringFromPhone = "Hello";
String OpenDoorCommand = "OpenDoor";
bool isHelloed = false;
long openMoment;
String request;
long openDelay = 6000;
void setup()
{
  pinMode(pinReley,OUTPUT);
  digitalWrite(pinReley,LOW);
  pinMode(13, OUTPUT);
//pinMode(2,INPUT);
//pinMode(3,OUTPUT);
Serial.begin(9600);
mySerial.begin(9600);
}


void loop()
{
  if(millis()-openMoment >= openDelay)
  {
    digitalWrite(pinReley,LOW);
  }
  if (mySerial.available())
  {
    if(!isHelloed)
    {
      request = ReciveRequest();
      if(request == helloStringFromPhone)
      //if("11" == "11")
      {
        sendHello();
        isHelloed = true;
      }
    }
    request = ReciveRequest(); // читаем из software-порта
    if(request == OpenDoorCommand )
    {
      if(isHelloed)
      {
        digitalWrite(pinReley,HIGH);
        openMoment = millis();
      }
      else
      {
        sendNotHello();
      }
    }
    
  }
  
//Если вдруг с PC команду нужно отправить
if (Serial.available())
{
  int pcData = Serial.read(); // читаем из hardware-порта
  mySerial.println(pcData); // пишем в software-порт
  
}

//логи
//Serial.println(request); // пишем в hardware-порт
}

String ReciveRequest()
{
  String str = Serial.readString();
  Serial.println(str);
  return str;
}

void sendHello()
{
  mySerial.println("200:Helloed");
}
void sendNotHello()
{
  mySerial.println("400:NotYetHelloed");
}

//8C3A:E3:ED8235
//18F0:E4:2F8153

//0018:e4:34bf20

