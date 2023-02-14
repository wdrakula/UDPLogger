#include <WiFi.h>

#include "time.h"
#include "logger.h"


const char *ssid = "42km";
const char *password = "1029384756";

bool loggerOn = true;
const char *udpAddress = "10.168.13.13";
int udpPort = 3333;


void setup()
{

  Serial.begin(115200);

  WiFi.mode(WIFI_STA);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  
  loggerSetup();
  configTime(0, 0, "pool.ntp.org");
}

void loop()
{

  static const int WIFIcheckInterval = 30000;

  static uint32_t omLog = millis();
  static uint32_t omWIFI = millis();
  uint32_t nm = millis();

  if (nm - omLog > 1000)
  {
    struct tm now;
    char s[64] = {0};

    if (getLocalTime(&now))
    {
      strftime(s, 62, "%d.%b.%Y %H:%M:%S", &now);
    }
    UDPDebug("Main loop is still here! %s (%d)s", s, (int)millis() / 1000);
    //Serial.printf("Main loop is still here! %s (%d)s \r\n", s, (int)millis() / 1000);
    omLog = nm;
  }

  if ((WiFi.status() != WL_CONNECTED) && (nm - omWIFI > WIFIcheckInterval))
  {
    Serial.print(millis());
    Serial.println("Reconnecting to WiFi...");
    WiFi.disconnect();
    WiFi.reconnect();
    omWIFI = nm;
  }
}
