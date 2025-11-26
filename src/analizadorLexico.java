import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Analizador Léxico ISABEL
 * 
 */
public class analizadorLexico {

    
    private static final Map<String, Integer> palabrasReservadas = new LinkedHashMap<>();
    private static final Map<String, Integer> operadores = new LinkedHashMap<>();
    private static final Map<String, Integer> signos = new LinkedHashMap<>();
    private static final Map<String, Integer> agrupacion = new LinkedHashMap<>();

   
    private static int tokenIdentificador = 6000;
    private static int tokenConstanteEntera = 7000;
    private static int tokenConstanteFlotante = 8000;

    static {
      
        palabrasReservadas.put("abstract", 1010);
        palabrasReservadas.put("assert", 1020);
        palabrasReservadas.put("boolean", 1030);
        palabrasReservadas.put("break", 1040);
        palabrasReservadas.put("byte", 1050);
        palabrasReservadas.put("case", 1060);
        palabrasReservadas.put("catch", 1070);
        palabrasReservadas.put("char", 1080);
        palabrasReservadas.put("class", 1090);
        palabrasReservadas.put("continue", 1100);
        palabrasReservadas.put("default", 1110);
        palabrasReservadas.put("do", 1120);
        palabrasReservadas.put("double", 1130);
        palabrasReservadas.put("else", 1140);
        palabrasReservadas.put("enum", 1150);
        palabrasReservadas.put("extends", 1160);
        palabrasReservadas.put("final", 1170);
        palabrasReservadas.put("float", 1180);
        palabrasReservadas.put("for", 1190);
        palabrasReservadas.put("if", 1200);
        palabrasReservadas.put("import", 1210);
        palabrasReservadas.put("int", 1220);
        palabrasReservadas.put("interface", 1230);
        palabrasReservadas.put("long", 1240);
        palabrasReservadas.put("new", 1250);
        palabrasReservadas.put("return", 1260);
        palabrasReservadas.put("void", 1270);

        
        operadores.put("!=", 2113);
        operadores.put("==", 2111);
        operadores.put(">=", 2072);
        operadores.put("<=", 2082);
        operadores.put("&&", 2132);
        operadores.put("||", 2142);
        operadores.put("!", 2010);
        operadores.put("*", 2020);
        operadores.put("/", 2030);
        operadores.put("%", 2040);
        operadores.put("+", 2050);
        operadores.put("-", 2060);
        operadores.put(">", 2070);
        operadores.put("<", 2080);
        operadores.put("=", 2110);
        operadores.put("&", 2130);
        operadores.put("|", 2140);
        operadores.put("^", 2150);

        signos.put(";", 3010);
        signos.put(",", 3020);
        // no tratamos '.' aquí como signo porque puede ser parte de un número
        // signos.put(".", 3030);

        agrupacion.put("{", 4010);
        agrupacion.put("}", 4011);
        agrupacion.put("(", 5010);
        agrupacion.put(")", 5011);
        agrupacion.put("[", 5030);
        agrupacion.put("]", 5031);
    }

    // Representación de un token
    private static class Token {
        final int id;
        final String lexema;
        final int linea;
        final int columna;

        Token(int id, String lexema, int linea, int columna) {
            this.id = id;
            this.lexema = lexema;
            this.linea = linea;
            this.columna = columna;
        }

        @Override
        public String toString() {
            return id + " " + lexema + " " + linea + ":" + columna;
        }
    }

    // Excepción léxica con posición
    private static class AnalizadorLexicoException extends Exception {
        AnalizadorLexicoException(String mensaje) {
            super(mensaje);
        }
    }

    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso: java analizadorLexico <archivo_entrada> <archivo_salida>");
            return;
        }

        String in = args[0];
        String out = args[1];

        try {
            String codigo = leerArchivo(in);
            List<Token> tokens = scan(codigo);
            escribirTokens(out, tokens);
            System.out.println("Análisis léxico completado. Tokens guardados en " + out);
        } catch (AnalizadorLexicoException e) {
            System.err.println("Error léxico: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error E/S: " + e.getMessage());
        }
    }

    private static String leerArchivo(String ruta) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    
    private static void escribirTokens(String ruta, List<Token> tokens) throws IOException {
        try (FileWriter fw = new FileWriter(ruta)) {
            for (Token t : tokens) {
                fw.write(t.toString());
                fw.write(System.lineSeparator());
            }
        }
    }

    
    private static List<Token> scan(String codigo) throws AnalizadorLexicoException {
        List<Token> tokens = new ArrayList<>();
        int length = codigo.length();
        int i = 0;
        int linea = 1, columna = 1;

        while (i < length) {
            char c = codigo.charAt(i);

            // AVANZA POR ESPACIOS : línea/columna
            if (Character.isWhitespace(c)) {
                if (c == '\n') {
                    linea++;
                    columna = 1;
                } else {
                    columna++;
                }
                i++;
                continue;
            }

         
            if (c == '/' && (i + 1 < length) && codigo.charAt(i + 1) == '/') {
                i += 2;
                columna += 2;
                while (i < length && codigo.charAt(i) != '\n') {
                    i++; columna++;
                }
                continue;
            }

      
            if (c == '/' && (i + 1 < length) && codigo.charAt(i + 1) == '*') {
                i += 2; columna += 2;
                boolean closed = false;
                while (i < length) {
                    char d = codigo.charAt(i);
                    if (d == '\n') { linea++; columna = 1; i++; continue; }
                    if (d == '*' && (i + 1 < length) && codigo.charAt(i + 1) == '/') {
                        i += 2; columna += 2; closed = true; break;
                    }
                    i++; columna++;
                }
                if (!closed) throw new AnalizadorLexicoException("Comentario sin cerrar en linea " + linea);
                continue;
            }

         
            if (c == '"') {
                int startCol = columna;
                StringBuilder lit = new StringBuilder();
                lit.append(c);
                i++; columna++;
                boolean closed = false;
                while (i < length) {
                    char d = codigo.charAt(i);
                    lit.append(d);
                    i++; columna++;
                    if (d == '\\' && i < length) {
                   
                        char e = codigo.charAt(i);
                        lit.append(e);
                        i++; columna++;
                    } else if (d == '"') {
                        closed = true;
                        break;
                    } else if (d == '\n') {
                        linea++; columna = 1;
                    }
                }
                if (!closed) throw new AnalizadorLexicoException("Cadena sin cerrar en linea " + linea);
                tokens.add(new Token(9000, lit.toString(), linea, startCol)); // 9000 = string literal token (ejemplo)
                continue;
            }

        
            if (c == '\'') {
                int startCol = columna;
                StringBuilder lit = new StringBuilder();
                lit.append(c);
                i++; columna++;
                boolean closed = false;
                while (i < length) {
                    char d = codigo.charAt(i);
                    lit.append(d);
                    i++; columna++;
                    if (d == '\\' && i < length) {
                        char e = codigo.charAt(i);
                        lit.append(e);
                        i++; columna++;
                    } else if (d == '\'') {
                        closed = true; break;
                    } else if (d == '\n') {
                        linea++; columna = 1;
                    }
                }
                if (!closed) throw new AnalizadorLexicoException("Literal de carácter sin cerrar en linea " + linea);
                tokens.add(new Token(9010, lit.toString(), linea, startCol)); // 9010 = char literal token
                continue;
            }

            // Identificadores : [a-zA-Z_][a-zA-Z0-9_]*
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                int startCol = columna;
                StringBuilder sb = new StringBuilder();
                while (i < length && (Character.isLetterOrDigit(codigo.charAt(i)) || codigo.charAt(i) == '_')) {
                    sb.append(codigo.charAt(i));
                    i++; columna++;
                }
                String palabra = sb.toString();
                if (palabrasReservadas.containsKey(palabra)) {
                    tokens.add(new Token(palabrasReservadas.get(palabra), palabra, linea, startCol));
                } else {
                    // validar identificador inválido 
                    if (palabra.matches(".*[@$].*")) {
                        throw new AnalizadorLexicoException("Identificador no válido '" + palabra + "' en " + linea + ":" + startCol);
                    }
                    tokens.add(new Token(tokenIdentificador, palabra, linea, startCol));
                    tokenIdentificador += 10;
                }
                continue;
            }

            
            if (Character.isDigit(c) || (c == '.' && (i + 1 < length) && Character.isDigit(codigo.charAt(i + 1)))) {
                int startCol = columna;
                int start = i;
                StringBuilder num = new StringBuilder();
                boolean hasDot = false;

               
                if (codigo.charAt(i) == '.') {
                    hasDot = true;
                    num.append('.');
                    i++; columna++;
                }

                while (i < length) {
                    char d = codigo.charAt(i);
                    if (Character.isDigit(d)) {
                        num.append(d);
                        i++; columna++;
                    } else if (d == '.' && !hasDot) {
                        hasDot = true;
                        num.append(d);
                        i++; columna++;
                    } else {
                        break;
                    }
                }

                String raw = num.toString();
                // validar múltiples puntos
                if (raw.chars().filter(ch -> ch == '.').count() > 1) {
                    throw new AnalizadorLexicoException("Constante numérica inválida '" + raw + "' en " + linea + ":" + startCol);
                }

                if (hasDot) {
                    tokens.add(new Token(tokenConstanteFlotante, raw, linea, startCol));
                    tokenConstanteFlotante += 10;
                } else {
                    tokens.add(new Token(tokenConstanteEntera, raw, linea, startCol));
                    tokenConstanteEntera += 10;
                }
                continue;
            }

         
            String two = (i + 1 < length) ? "" + c + codigo.charAt(i + 1) : null;
            if (two != null && operadores.containsKey(two)) {
                tokens.add(new Token(operadores.get(two), two, linea, columna));
                i += 2; columna += 2;
                continue;
            }

         
            String one = "" + c;
            if (operadores.containsKey(one)) {
                tokens.add(new Token(operadores.get(one), one, linea, columna));
                i++; columna++;
                continue;
            }
            if (signos.containsKey(one)) {
                tokens.add(new Token(signos.get(one), one, linea, columna));
                i++; columna++;
                continue;
            }
            if (agrupacion.containsKey(one)) {
                tokens.add(new Token(agrupacion.get(one), one, linea, columna));
                i++; columna++;
                continue;
            }

            // Cualquier otro carácter inesperado:
            throw new AnalizadorLexicoException("Símbolo no reconocido '" + c + "' en " + linea + ":" + columna);
        }

        return tokens;
    }
}
