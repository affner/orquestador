package com.digipro.emulator.adapter;

import com.digipro.emulator.database.DatabaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Adaptador de acceso al repositorio de archivos en disco.
 * Lee la ruta base desde database.properties (clave: files.base.path).
 */
public class FileSystemAdapter {

    private final DatabaseManager db;

    public FileSystemAdapter() {
        this.db = DatabaseManager.getInstance();
    }

    /** Obtiene la ruta base del repositorio desde  properties. */
    private String basePath() {
        String bp = db.getProperty("files.base.path");
        if (bp == null || bp.trim().isEmpty()) {
            // fallback seguro
            bp = "C:/ImagenesSOAP";
        }
        return bp.trim();
    }

    /**
     * Devuelve el path absoluto combinando la base con una ruta relativa de BD.
     * Soporta separadores con / o \ .
     */
    public Path resolveAbsolutePath(String relative) {
        if (relative == null) {
            throw new IllegalArgumentException("relative path is null");
        }
        // Normaliza separadores
        String norm = relative.replace("\\", "/");
        // Evita doble separador
        String bp = basePath().replace("\\", "/");
        if (norm.startsWith("/")) norm = norm.substring(1);
        return Paths.get(bp, norm).normalize();
    }

    /**
     * Lee el archivo indicado por la ruta relativa guardada en BD.
     * @param relativePath ejemplo: "Estados/02202501456.pdf"
     * @return contenido en bytes o null si no existe o no se puede leer
     */
    public byte[] readFile(String relativePath) {
        try {
            Path p = resolveAbsolutePath(relativePath);
            if (!Files.exists(p) || !Files.isRegularFile(p)) {
                System.err.println("[FileSystemAdapter] No existe: " + p);
                return null;
            }
            return Files.readAllBytes(p);
        } catch (IOException e) {
            System.err.println("[FileSystemAdapter] Error leyendo archivo: " + relativePath + " -> " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica existencia del archivo relativo.
     */
    public boolean exists(String relativePath) {
        try {
            Path p = resolveAbsolutePath(relativePath);
            return Files.exists(p) && Files.isRegularFile(p);
        } catch (Exception e) {
            return false;
        }
    }
}
