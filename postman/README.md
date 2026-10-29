# Pruebas SIGSU con Newman

La colección crea estudiantes únicos y un servicio nuevo en cada ejecución. Usa los UUID de los tres servicios de la semilla, sin modificar sus formularios. Puede repetirse sin reiniciar PostgreSQL ni Redis. Las variables encadenadas se guardan en el entorno de ejecución; Newman no modifica el archivo de entorno original.

Desde la raíz del repositorio, para empezar con datos limpios (borra los datos locales):

```bash
podman compose down -v
podman compose up -d --build
```

Esperar a que `/actuator/health` responda `UP`, y ejecutar:

```bash
set -o pipefail
npx --yes newman run postman/SIGSU.postman_collection.json \
  -e postman/SIGSU.local.postman_environment.json --working-dir postman \
  --reporters cli,json,junit \
  --reporter-json-export docs/evidencias/newman-resultado.json \
  --reporter-junit-export docs/evidencias/newman-junit.xml \
  --color off 2>&1 | tee docs/evidencias/newman-salida.txt
```

La versión instalada de Newman rechaza `--no-color` con `unknown option`; `--color off` es la opción admitida equivalente. Los reportes `newman-segunda-*` registran una segunda ejecución completa sin reiniciar.

La carpeta de vencimientos está al final porque afecta todas las solicitudes abiertas. El autor sistema se expone como `autorId: null` y `observacion: "plazo vencido"`. Mailpit se consulta después de otras operaciones para dar tiempo al envío asíncrono. La descarga compara todo el contenido del archivo de prueba.

Las credenciales y los archivos son exclusivamente datos locales de demostración. `../scripts/evidencias.sh` prueba directamente PostgreSQL, Redis, carreras con curl y el scheduler; permite `COMPOSE='docker compose'` o `COMPOSE='podman compose'`. Cambia temporalmente el cron a cada minuto y habilita el log SQL, verifica la transición sin llamar al endpoint manual y restaura ambos ajustes con un trap. No borra volúmenes y crea un estudiante distinto cada vez.
