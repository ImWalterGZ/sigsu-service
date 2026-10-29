package mx.utch.sigsu.seguridad.service;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PeticionRepetible extends HttpServletRequestWrapper {
    private static final int LIMITE = 6 * 1024 * 1024;
    private final byte[] cuerpo;
    private java.util.List<Part> partes;

    public PeticionRepetible(HttpServletRequest original) throws IOException {
        super(original);
        cuerpo = original.getInputStream().readNBytes(LIMITE + 1);
        if (cuerpo.length > LIMITE)
            throw new mx.utch.sigsu.compartido.BusinessException(413, "la petición supera 6 MB");
    }

    // El contenedor ya no puede leer el flujo original. Su mismo analizador recibe la copia.
    @Override
    public java.util.Collection<Part> getParts() throws IOException, ServletException {
        if (partes == null) {
            var analizador = new org.apache.tomcat.util.http.fileupload.FileUpload();
            var fabrica = new org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory();
            fabrica.setSizeThreshold(LIMITE);
            analizador.setFileItemFactory(fabrica);
            analizador.setFileSizeMax(5 * 1024 * 1024);
            analizador.setSizeMax(LIMITE);
            analizador.setFileCountMax(20);
            try {
                partes =
                        analizador
                                .parseRequest(
                                        new org.apache.tomcat.util.http.fileupload.UploadContext() {
                                            public String getCharacterEncoding() {
                                                return "UTF-8";
                                            }

                                            public String getContentType() {
                                                return PeticionRepetible.this.getContentType();
                                            }

                                            public InputStream getInputStream() {
                                                return new ByteArrayInputStream(cuerpo);
                                            }

                                            public long contentLength() {
                                                return cuerpo.length;
                                            }
                                        })
                                .stream()
                                .map(
                                        elemento ->
                                                (Part)
                                                        new org.apache.catalina.core
                                                                .ApplicationPart(
                                                                elemento,
                                                                new File(
                                                                        System.getProperty(
                                                                                "java.io.tmpdir"))))
                                .toList();
            } catch (org.apache.tomcat.util.http.fileupload.FileUploadException error) {
                throw new ServletException("multipart inválido o supera el límite", error);
            }
        }
        return partes;
    }

    @Override
    public Part getPart(String nombre) throws IOException, ServletException {
        return getParts().stream()
                .filter(parte -> parte.getName().equals(nombre))
                .findFirst()
                .orElse(null);
    }

    public byte[] cuerpo() {
        return cuerpo;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream entrada = new ByteArrayInputStream(cuerpo);
        return new ServletInputStream() {
            public int read() {
                return entrada.read();
            }

            public boolean isFinished() {
                return entrada.available() == 0;
            }

            public boolean isReady() {
                return true;
            }

            public void setReadListener(ReadListener escucha) {
                throw new UnsupportedOperationException("solo lectura síncrona");
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
