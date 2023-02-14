#include "logger.h"

#include "sysState.h"

char udpAddress[32] = {0};
int udpPort;
WiFiUDP udp;

void loggerSetup(void)
{
    String s;
//предполагается, что преференсес уже инициализированы
    s = pref.getString("loggerIP", "192.168.0.1");
    s.remove(30);
    // sysState.logOn = false;
    if (s.length() > 1)
    {
        strncpy(udpAddress, s.c_str(), 30);
        udpAddress[s.length()] = 0;
    }
//это заготовка для встроенного логгера ЕСП, но в реальной сти на той
//версии ардуино кор - для есп - не срабатывает... но на будущее оставим.
    udpPort = pref.getInt("loggerPort", 3333);
    esp_log_set_vprintf(vUDPDebug);
}

int UDPDebug(const char *s, ...)
{
    va_list arg;
    int ret = 0;


// sysState  этоструктура с сосоянием все системы, её обычно хранят в переференсес
// но другой программист може писать по своему.
    if (!sysState.logOn)
        return ret;

    va_start(arg, s);
    ret = vUDPDebug(s, arg);
    va_end(arg);
    return ret;
}

int vUDPDebug(const char *s, va_list arg)
{
    char szBuff[256];
    int ret;

    ret = vsnprintf(szBuff, 254, s, arg);

    udp.beginPacket(udpAddress, udpPort);
    udp.printf("%s", szBuff);
    udp.endPacket();

    return ret;
}
