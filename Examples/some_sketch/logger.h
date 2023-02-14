#include <WiFiUdp.h>

extern bool loggerOn;
extern const char * udpAddress;
extern int udpPort;

void loggerSetup (void);
int vUDPDebug(const char *,  va_list);
int UDPDebug(const char *,  ...);

