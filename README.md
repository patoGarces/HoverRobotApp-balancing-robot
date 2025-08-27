# HoverRobotApp

Aplicación desarrollada en kotlin para el control del proyecto [HoverRobot ESP32](https://github.com/patoGarces/HoverRobot_ProyectoFinal).

## Descripción

Esta aplicación está diseñada para controlar un balancing robot, un tipo de robot que utiliza algoritmos de control para mantenerse equilibrado en posición vertical. Esta aplicación te permite interactuar con el robot de una manera intuitiva y eficiente.

### Características Principales

- **Transmisión en Vivo**: Disfruta de una transmisión en vivo desde un servidor HTTP (WIP) a través de la conexión WiFi a una Raspberry Pi. Mantente al tanto de lo que está viendo el robot en tiempo real.

- **Análisis de Performance**: Observa un gráfico en tiempo real que muestra múltiples parámetros clave, lo que te permite analizar la performance del sistema de control PID de manera fácil y precisa. 

- **Configuración Personalizada**: Ajusta diferentes variables del robot según tus necesidades específicas, incluyendo configuraciones de PID, límites de seguridad y ángulo del centro de gravedad.

- **Control del robot mediante joystick**: Permite la posibilidad de controlar la dirección del robot mediante un joystick analógico.

### Desarrollado en Kotlin con Android Studio y Jetpack Compose

Esta aplicación está desarrollada **100% en Kotlin** utilizando **Android Studio** como IDE principal. Está construida con **Jetpack Compose**, el moderno toolkit de UI declarativa de Android, lo que permite interfaces **flexibles, reactivas y altamente mantenibles**, sin la necesidad de usar XML.

Entre las características técnicas destacadas se incluyen:

- **Arquitectura moderna basada en Compose**: toda la UI está diseñada con composables, siguiendo las mejores prácticas de **State Hoisting**, **ViewModel** y **unidirectional data flow**.
- **Navegación con Compose Navigation**: gestión de pantallas y rutas de manera declarativa, con soporte para argumentos y animaciones de transición.
- **Material3 y theming dinámico**: uso de **Material You** para colores, tipografía y componentes, con soporte para modos claro y oscuro.
- **Compatibilidad y rendimiento optimizado**: al ser una aplicación nativa, garantiza fluidez, rápido tiempo de renderizado y bajo consumo de memoria.
- **Integración con APIs modernas**: fácil extensión para llamadas a servicios REST, manejo de datos en **Flow/StateFlow**, y arquitectura preparada para **pruebas unitarias y de UI**.
- **Inyección de dependencias y escalabilidad**: preparada para integrar **Hilt/Dagger**, lo que facilita el manejo de dependencias y mejora la testabilidad.
- **Coroutines y programación reactiva**: para operaciones asíncronas eficientes, evitando bloqueos en el hilo principal.
- **Animaciones avanzadas**: soporte completo de animaciones declarativas en Compose para transiciones y feedback visual.

Esta implementación refleja el **estado del arte en desarrollo de aplicaciones Android**, garantizando escalabilidad y mantenibilidad, además de proporcionar una **experiencia de usuario moderna y consistente**.

### Comunicación mediante Wi-Fi con Sockets TCP

La aplicación se comunica ahora mediante **Wi-Fi utilizando Sockets TCP**, reemplazando la implementación anterior basada en Bluetooth Standard.  

Esto permite:

- **Conexión más estable y rápida** con los dispositivos ESP32, sin limitaciones de compatibilidad de Bluetooth.  
- **Mayor alcance y flexibilidad**, ya que la comunicación puede realizarse dentro de la red local sin necesidad de emparejamiento físico.  
- **Compatibilidad con múltiples dispositivos** conectados simultáneamente.  
- **Preparada para integración con ROS 2** o cualquier otro módulo que requiera comunicación en red.  

> Nota: La aplicación ya no depende de Bluetooth y es compatible con todos los ESP32 que soporten Wi-Fi.
### Instalación y Uso

#### Requisitos Previos

- Dispositivo Android compatible
- ESP32 con el firmware para el balancing robot cargado, que puedes encontrar [aqui](https://github.com/patoGarces/Imu-ESP32-HoverRobot)
- No es necesario tener conectada la app con wifi a una raspberry pi para poder utilizarla!

#### Pasos para Correr la Aplicación

1. Clona este repositorio en tu máquina local.
2. Abre el proyecto en Android Studio.
3. Conecta tu dispositivo Android al ordenador.
4. Compila y ejecuta la aplicación en tu dispositivo Android.

#### Capturas de pantalla

Pantalla inicial de navegacion
![app-navigation](https://github.com/user-attachments/assets/1c80b68e-5921-4382-9f80-e05869f88b9a)

Pantalla de status
![app-status](https://github.com/user-attachments/assets/65812812-bffc-4be5-853c-b66ca013d852)

Grafico para el analisis del sistema de control PID y otras variables
![app-analisis](https://github.com/user-attachments/assets/f77fa208-e06c-42a5-9f63-53753bdf1cb0)

Pantalla de configuración
![app-settings](https://github.com/user-attachments/assets/2799a547-c04b-4f6d-b19d-8bbf6ccd9eb8)

## Contribuciones

¡Toda colaboración es bienvenida! Si tienes ideas para mejorar esta aplicación, has encontrado algún error o simplemente quieres contribuir al proyecto, aquí hay algunas formas en las que puedes hacerlo:

- **Envía un Pull Request**: Si tienes cambios que te gustaría agregar al proyecto, no dudes en enviar un pull request. ¡Estaremos encantados de revisarlo y fusionarlo si es apropiado!

- **Hacer un Fork**: Si prefieres trabajar en tu propio espacio de desarrollo, puedes hacer un fork de este repositorio y trabajar en tus propias modificaciones. ¡No dudes en compartir tu trabajo con la comunidad!

- **Abrir un Issue**: Si encuentras algún problema o tienes alguna idea para mejorar la aplicación, no dudes en abrir un issue en el repositorio. Estaremos encantados de escuchar tus comentarios y ayudarte a resolver cualquier problema que encuentres.

## Agradecimientos

Especial agradecimiento a los creadores de las siguientes dependencias que fueron utilizadas en este proyecto:

- **Joystick**: Agradecimientos al equipo de desarrollo de `io.github.controlwear` por su biblioteca de joystick virtual, que proporciona una interfaz intuitiva para el control del robot.

- **Plotter**: Un agradecimiento especial a `com.github.PhilJay` por su biblioteca MPAndroidChart, que facilita la visualización de datos en forma de gráficos en la aplicación.

## Contacto

Para cualquier consulta o sugerencia, no dudes en contactarnos a través de [correo electrónico](patricio.garces@outlook.com) o en nuestra página de [GitHub](https://github.com/patoGarces).

## Licencia

Este proyecto está bajo la Licencia Pública General de GNU versión 3 (GPLv3). Esto significa que cualquier persona que modifique o distribuya este software debe hacerlo bajo los términos de la GPLv3, lo que garantiza que las versiones modificadas también sean de código abierto.

Puedes ver los detalles completos de la licencia en el archivo [LICENSE](LICENSE).
