# HoverRobotApp

Aplicación desarrollada en kotlin para el control de un balancing robot

## Descripción

Esta aplicación está diseñada para controlar un balancing robot, un tipo de robot que utiliza algoritmos de control para mantenerse equilibrado en posición vertical. Esta aplicación te permite interactuar con el robot de una manera intuitiva y eficiente.

### Características Principales

- **Transmisión en Vivo**: Disfruta de una transmisión en vivo desde un servidor HTTP (WIP) a través de la conexión WiFi a una Raspberry Pi. Mantente al tanto de lo que está viendo el robot en tiempo real.

- **Análisis de Performance**: Observa un gráfico en tiempo real que muestra múltiples parámetros clave, lo que te permite analizar la performance del sistema de control PID de manera fácil y precisa. 

- **Configuración Personalizada**: Ajusta diferentes variables del robot según tus necesidades específicas, incluyendo configuraciones de PID, límites de seguridad y ángulo del centro de gravedad.

### Desarrollado en Kotlin con Android Studio

Esta aplicación está desarrollada en Kotlin utilizando Android Studio, al ser una aplicación nativa con el soporte de google, garantiza un rendimiento óptimo y una experiencia de desarrollo sólida.

### Compatibilidad con ESP32 y Bluetooth Standard

El protocolo de comunicación utilizado en esta aplicación es Bluetooth Standard, no Bluetooth de Baja Energía (BLE). Esto significa que es compatible solo con los dispositivos ESP32 que admiten Bluetooth estándar(No ESP32s3!).

### Instalación y Uso

#### Requisitos Previos

- Dispositivo Android compatible
- ESP32 con el firmware para el balancing robot cargado, que puedes encontrar en: https://github.com/patoGarces/HoverRobotMainBoard
- No es necesario tener conectada la app con wifi a una raspberry pi para poder utilizarla!

#### Pasos para Correr la Aplicación

1. Clona este repositorio en tu máquina local.
2. Abre el proyecto en Android Studio.
3. Conecta tu dispositivo Android al ordenador.
4. Compila y ejecuta la aplicación en tu dispositivo Android.

#### Capturas de pantalla

App inicial, solicitud de permiso bluetooth
![Captura 1](https://github.com/patoGarces/HoverRobotApp-balancing-robot/assets/34481371/94284a00-69e1-4c19-a39b-b29b7adcb026)

Lista de devices bluetooth disponibles
![Captura 2](https://github.com/patoGarces/HoverRobotApp-balancing-robot/assets/34481371/e1e8e132-2f5d-4009-967c-1e28e722331c)

Pantalla de status
![Captura 3](https://github.com/patoGarces/HoverRobotApp-balancing-robot/assets/34481371/a73e8de6-efa4-460e-9dc4-93441408b287)

Grafico para el analisis del sistema de control PID
![Captura 4](https://github.com/patoGarces/HoverRobotApp-balancing-robot/assets/34481371/a29883c7-613d-493c-b484-71cb54ea8e5d)

Pantalla de configuración
![Captura 5](https://github.com/patoGarces/HoverRobotApp-balancing-robot/assets/34481371/3f6a52e6-67a2-4f6b-8c24-272257c3a837)


## Contribuciones

¡Toda colaboración es bienvenida! Si tienes ideas para mejorar esta aplicación, has encontrado algún error o simplemente quieres contribuir al proyecto, aquí hay algunas formas en las que puedes hacerlo:

- **Envía un Pull Request**: Si tienes cambios que te gustaría agregar al proyecto, no dudes en enviar un pull request. ¡Estaremos encantados de revisarlo y fusionarlo si es apropiado!

- **Hacer un Fork**: Si prefieres trabajar en tu propio espacio de desarrollo, puedes hacer un fork de este repositorio y trabajar en tus propias modificaciones. ¡No dudes en compartir tu trabajo con la comunidad!

- **Abrir un Issue**: Si encuentras algún problema o tienes alguna idea para mejorar la aplicación, no dudes en abrir un issue en el repositorio. Estaremos encantados de escuchar tus comentarios y ayudarte a resolver cualquier problema que encuentres.

## Contacto

Para cualquier consulta o sugerencia, no dudes en contactarnos a través de [correo electrónico](patricio.garces@outlook.com) o en nuestra página de [GitHub](https://github.com/patoGarces).

## Licencia

Este proyecto está bajo la Licencia Pública General de GNU versión 3 (GPLv3). Esto significa que cualquier persona que modifique o distribuya este software debe hacerlo bajo los términos de la GPLv3, lo que garantiza que las versiones modificadas también sean de código abierto.

Puedes ver los detalles completos de la licencia en el archivo [LICENSE](LICENSE).
