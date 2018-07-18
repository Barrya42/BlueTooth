#include <SoftwareSerial.h>

SoftwareSerial mySerial(3, 2); // указываем пины RX и TX
int pinReley = 4;
char openChar = '1';
long openMoment;
long openDelay = 6000;
void setup()
{
  pinMode(pinReley,OUTPUT);
  digitalWrite(pinReley,LOW);
  pinMode(13, OUTPUT);
//pinMode(2,INPUT);
//pinMode(3,OUTPUT);
//Serial.begin(9600);
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
char bpData = mySerial.read(); // читаем из software-порта
if(bpData == openChar)
{
  digitalWrite(pinReley,HIGH);
  openMoment = millis();
}
//Serial.write(bpData); // пишем в hardware-порт
}
//if (Serial.available())
//{
//  int pcData = Serial.read(); // читаем из hardware-порта
//  mySerial.write(pcData); // пишем в software-порт
//  
//}

}


//8C3A:E3:ED8235
//18F0:E4:2F8153

//0018:e4:34bf20

