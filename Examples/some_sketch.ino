#include "time.h"

static const char *TAG __attribute__((unused)) = "MyModule";


void setup()
{


  Serial.begin(115200);

//** тут нужен еще сетап сети, конечно
  loggerSetup();
  configTime(0, 0, "pool.ntp.org");
}

// часть потоков сделана задачами, неблокирующие можно запускать тут.
void loop()
{

  static const int WIFIcheckInterval = 30000;

  static uint32_t omLog = millis();
  static uint32_t omWIFI = millis();
  uint32_t nm = millis();

  if (nm - omLog > 10000)
  {
    struct tm now;
    char s[64] = {0};

    if (getLocalTime(&now))
    {
      strftime(s, 62, "%d.%b.%Y %H:%M:%S", &now);
    }
    UDPDebug("Main loop is still here! %s (%d)s \n", s, millis() / 1000);
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
