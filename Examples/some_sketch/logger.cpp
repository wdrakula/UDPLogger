#include "logger.h"
#include <WiFi.h>


WiFiUDP udp;


void loggerSetup(void)
{
 udp.begin(WiFi.localIP(),udpPort);
//это заготовка для встроенного логгера ЕСП, но в реальной сти на той
//версии ардуино кор - для есп - не срабатывает... но на будущее оставим.
    esp_log_set_vprintf(vUDPDebug);
}

int UDPDebug(const char *s, ...)
{
    va_list arg;
    int ret = 0;

    if (!loggerOn)
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
