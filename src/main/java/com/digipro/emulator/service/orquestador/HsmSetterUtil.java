package com.digipro.emulator.service.orquestador;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

final class HsmSetterUtil {

    private HsmSetterUtil() {}

    // ---------- Público: helpers específicos ----------
    static void setArchivo(Object target, byte[] value) {
        // 🛠️ Primero los nombres más comunes en tus stubs:
        if (trySet(target, "setArrayFile", byte[].class, value)) return;   // << añadido
        if (trySet(target, "setArchivo", byte[].class, value)) return;
        if (trySet(target, "setArchivoHSM", byte[].class, value)) return;
        if (trySet(target, "setArrArchivo", byte[].class, value)) return;
        if (trySet(target, "setBytes", byte[].class, value)) return;
        if (trySet(target, "setContenido", byte[].class, value)) return;
        if (trySet(target, "setContenidoArchivo", byte[].class, value)) return;

        // Heurística: cualquier setter byte[] cuyo nombre sugiera archivo/contenido
        trySetByHeuristic(target, byte[].class, value, "arrayfile", "arch", "content", "byte");
    }

    static void setExt(Object target, String value) {
        if (trySet(target, "setExt", String.class, value)) return;
        if (trySet(target, "setExtension", String.class, value)) return;
        if (trySet(target, "setExtencion", String.class, value)) return; // por si acaso
        trySetByHeuristic(target, String.class, value, "ext", "extension");
    }

    static void setLlave(Object target, String value) {
        if (trySet(target, "setLlave", String.class, value)) return;
        if (trySet(target, "setLlaveExpediente", String.class, value)) return;
        if (trySet(target, "setLlaveBusqueda", String.class, value)) return;
        if (trySet(target, "setKey", String.class, value)) return;
        if (trySet(target, "setClave", String.class, value)) return;
        trySetByHeuristic(target, String.class, value, "llave", "key", "busqueda", "expediente", "clave");
    }

    static void setTipoDocId(Object target, int value) {
        if (trySet(target, "setTipoDocID", int.class, value)) return;
        if (trySet(target, "setTipoDocId", int.class, value)) return;
        trySetByHeuristic(target, int.class, value, "tipodoc");
    }

    static void setNombreArchivo(Object target, String value) {
        if (trySet(target, "setNombreArchivo", String.class, value)) return;
        if (trySet(target, "setNombre", String.class, value)) return;
        if (trySet(target, "setFileName", String.class, value)) return;
        if (trySet(target, "setNombreDoc", String.class, value)) return;
        trySetByHeuristic(target, String.class, value, "nombre", "file");
    }

    // ---------- Privado: helpers de reflexión ----------
    private static boolean trySet(Object target, String method, Class<?> paramType, Object value) {
        try {
            Method m = target.getClass().getMethod(method, paramType);
            m.invoke(target, value);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean trySetByHeuristic(Object target, Class<?> paramType, Object value, String... nameHints) {
        String[] hints = Arrays.stream(nameHints)
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toArray(String[]::new);

        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().startsWith("set")) continue;
            Class<?>[] pt = m.getParameterTypes();
            if (pt.length != 1 || !pt[0].equals(paramType)) continue;

            String n = m.getName().toLowerCase(Locale.ROOT);
            boolean matches = hints.length == 0 || Arrays.stream(hints).anyMatch(n::contains);
            if (!matches) continue;

            try {
                m.invoke(target, value);
                return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }
}
