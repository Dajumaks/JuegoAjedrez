package autonoma.JuegoAjedrez.models;

import autonoma.JuegoAjedrez.views.MateHaciaNegras;



/**
 * bot que nos ayudara a tener un juego semiautomatico
 *
 * @author Dajuma / autonoma
 * @version 6.3.8
 */
//////////
///la base para implementar un bot para jugar ajedrez, 
////con lógica para el manejo de movimientos y la evaluación de posiciones en el tablero.
public class Bot {

    String[][] tablero;
    Movimientos movimientos = new Movimientos();
    boolean primerMovimiento = true;
    int[][] puntuacionEnCasoDeFichaAtaque = new int[8][8];
    int[][] puntuacionesPosiciones    = new int[8][8];
    int[][] puntuacionSiEstaDefendiendo    = new int[8][8];
    int[][] puntuacionCasillaSegura  = new int[8][8];
    int[][] puntuacionMovimientosEnFuturo    = new int[8][8];
    int[][] puntuacionParaDefenderComiendo    = new int[8][8];
    

    //Te retorna las posicion antigua[0] y la posicion nueva[1]
    public String[] movimientoBot(String[][] tablero) {
        this.tablero = tablero;
        examinarLasCasillasAtacadasAlInicio();
        return arregloFinal();
    }

    ////// evalua movimientos posibles para las fichas negras en el tablero y determinando el movimiento 
    /////que maximiza la puntuación de la posición. 
    ///// gestiona el estado de jaqueMate basado en las evaluaciones de las posiciones.
    private String[] arregloFinal() {
        int puntuacionMaxima = -600;
        int yInicial = 1;
        int xInicial = 4;
        int yFinal = 3;
        int xFinal = 4;
        boolean jaqueMate = true;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (comprobarSiLaFichaEsnegra(tablero, i, j)) {
                    String posicionInicial = "" + i + "" + j;
                    String[] movimientosPosibles = movimientos.movimientosFichas(tablero, posicionInicial);

                    String ficha = tablero[i][j];

                    for (int k = 0; k < movimientosPosibles.length; k++) {
                        String posicionFinal = movimientosPosibles[k];
                        ////////este bloque de código intenta manejar la obtención de coordenadas, cálculos de puntuación y 
                        ////actualización de variables de estado basadas en la evaluación de movimientos en el tablero.
                        try {
                            char ejeY = posicionFinal.charAt(0);
                            char ejeX = posicionFinal.charAt(1);
                            int intEjeY = Integer.parseInt(String.valueOf(ejeY));
                            int intEjeX = Integer.parseInt(String.valueOf(ejeX));
                            System.out.println(ficha + " : " + posicionFinal);
                            int puntuacionPosicionEspecifica = punutacionMovimiento(i, j, intEjeY, intEjeX);
                            System.out.println(puntuacionPosicionEspecifica);
                            if (puntuacionMaxima <= puntuacionPosicionEspecifica) {
                                yInicial = i;
                                xInicial = j;
                                yFinal = intEjeY;
                                xFinal = intEjeX;
                                puntuacionMaxima = puntuacionPosicionEspecifica;
                            }

                            if (puntuacionPosicionEspecifica > -600) {
                                jaqueMate = false;
                            }

                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }

        ///////
        ///finaliza el proceso de evaluación de movimientos y devuelve un arreglo que contiene las posiciones de movimiento 
        String posicionAntigua = "" + yInicial + "" + xInicial;
        String posicionNueva = "" + yFinal + "" + xFinal;

        if (primerMovimiento) {
            posicionAntigua = "14";
            posicionNueva = "34";
            primerMovimiento = false;
        }
        System.out.println("Fin turno");

        if (jaqueMate == true) {
            MateHaciaNegras ventana = new MateHaciaNegras(null, true);
            ventana.setVisible(true);
        }
        String arrayPosiciones[] = {posicionAntigua, posicionNueva};
        return arrayPosiciones;

    }

    //////
    ///la suma de todas estas puntuaciones se devuelve como el resultado final de la evaluación del movimiento. Esto sugiere que el método tiene en cuenta múltiples 
    ////factores para determinar la calidad de un movimiento en el contexto del juego.
    private int punutacionMovimiento(int yInicial, int xInicial, int yFinal, int xFinal) {
        int puntuacion = 0;
        puntuacion += puntuacionEnCasoDeFichaAtaque(yInicial, xInicial, yFinal, xFinal);
        puntuacion += puntuacionesPosiciones[yInicial][xInicial];
        puntuacion += puntuacionSiEstaDefendiendo(yInicial, xInicial);
        puntuacion += puntuacionCasillaSegura(yFinal, xFinal);
        puntuacion += puntuacionMovimientosEnFuturo(yInicial, xInicial, yFinal, xFinal);
        puntuacion += puntuacionParaDefenderComiendo(tablero, yInicial, xInicial, yFinal, xFinal);
        return puntuacion;
    }

    //////simula el efecto del movimiento actual en el estado futuro del juego y evalúa las posibles implicaciones en términos de amenazas al rey,
    ////posibilidad de jaque mate y capacidad de defender o atacar posiciones importantes en el tablero.
    private int puntuacionMovimientosEnFuturo(int yInicial, int xInicial, int yFinal, int xFinal) {
        int puntuacion = 0;
        String tableroFuturo[][] = new String[8][8];
        copiarTablero(tableroFuturo);
        tableroFuturo[yFinal][xFinal] = tableroFuturo[yInicial][xInicial];
        tableroFuturo[yInicial][xInicial] = "";

        if (miReyEnJaque(tableroFuturo)) {
            return -2000;
        } else if (jaqueMateHaciaEl(tableroFuturo)) {
            return 1000;
        } else if (podriaHacerJaqueMate(tableroFuturo)) {
            return -2000;
        } else {
            puntuacion += fichaQuePuedeDefender(tableroFuturo, yFinal, xFinal);
            puntuacion += fichaQuePuedeAtacar(tableroFuturo, yFinal, xFinal);
            return puntuacion;
        }
    }

    /////evalúa la capacidad de una ficha para defender otras fichas en el tablero considerando sus movimientos de ataque posibles y la posibilidad de atacar fichas del oponente.
    private int fichaQuePuedeDefender(String[][] tableroM, int y, int x) {
        String[] ataquesEsaFicha = movimientos.movimientoAtaqueFichaB(tableroM, y, x);
        int valor = 0;
        for (int i = 0; i < ataquesEsaFicha.length; ++i) {
            int yAtaque = Integer.parseInt(String.valueOf(ataquesEsaFicha[i].charAt(0)));
            int xAtaque = Integer.parseInt(String.valueOf(ataquesEsaFicha[i].charAt(1)));

            if (comprobarSiLaFichaEsnegra(tableroM, yAtaque, xAtaque)) {
                valor += 2;
            }

        }
        return valor;
    }

    /////////evalúa la capacidad de una ficha en una posición específica para atacar fichas del oponente en el tablero
    private int fichaQuePuedeAtacar(String[][] tableroM, int y, int x) {
        String[] ataquesEsaFicha = movimientos.movimientoAtaqueFichaB(tableroM, y, x);
        int valor = 0;
        for (int i = 0; i < ataquesEsaFicha.length; ++i) {
            int yAtaque = Integer.parseInt(String.valueOf(ataquesEsaFicha[i].charAt(0)));
            int xAtaque = Integer.parseInt(String.valueOf(ataquesEsaFicha[i].charAt(1)));

            if (comprobarSiLaFichaEsBlanca(tableroM, yAtaque, xAtaque)) {
                valor += 2;
            }

        }
        return valor;
    }

    ///////busca movimientos posibles para las fichas blancas en el tablero futuro y verifica si alguno de estos movimientos resultaría en un jaque mate al rey del oponente.
    private boolean podriaHacerJaqueMate(String[][] tableroFuturo) {
        ////mama metame en arroz
        ///dia martes programando y no siento mi coxis
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (comprobarSiLaFichaEsBlanca(tablero, i, j)) {
                    String posicion = "" + i + "" + j;
                    String[] movimientosBlanca = movimientos.movimientosAmodificados(tableroFuturo, posicion);
                    if (!movimientosBlanca[0].equals("")) {
                        for (int k = 0; k < movimientosBlanca.length; k++) {

                            int iF = Integer.parseInt(String.valueOf(movimientosBlanca[k].charAt(0)));
                            int jF = Integer.parseInt(String.valueOf(movimientosBlanca[k].charAt(1)));

                            String[][] tableroFichaCambiada = new String[8][8];
                            copiarTablero(tableroFuturo, tableroFichaCambiada);

                            tableroFichaCambiada[iF][jF] = tableroFichaCambiada[i][j];
                            tableroFichaCambiada[i][j] = "";
                            if (miReyEnJaqueMate(tableroFichaCambiada)) {
                                return true;
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    ////este método determina si el rey propio está en jaque mate examinando todos los movimientos posibles de las fichas negras en el tablero. Si no hay movimientos legales que el rey pueda realizar para escapar del jaque, 
    ////se considera que está en jaque mate.
    private boolean miReyEnJaqueMate(String[][] tableroM) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (comprobarSiLaFichaEsnegra(tableroM, i, j)) {
                    String posicion = "" + i + "" + j;
                    String[] movimientosNegra = movimientos.movimientosBmodificados(tableroM, posicion);
                    if (movimientosNegra != null) {
                        if (!movimientosNegra[0].equals("")) {
                            return false;
                        }
                    }

                }
            }
        }
        return true;
    }

    /////
    ///evalúa la situación en la que una ficha defiende a una ficha valiosa al comer a la ficha que la está atacando, considerando la relación entre los valores de las fichas 
    //involucradas y si la ficha atacada está o no defendida. (la parte mas dificil) (pronto todo acabará)
    private int puntuacionParaDefenderComiendo(String[][] tableroM, int yInicial, int xInicial, int yFinal, int xFinal) {
        String posicionFichaQueAtacaValiosa = posicionFichaQueAtacaAlaMasValiosa(tableroM);
        if (!posicionFichaQueAtacaValiosa.equals("ninguna")) {
            if (comprobarSiLaPosicionDeAtaqueCoincideConLaQueAtaca(yFinal, xFinal, posicionFichaQueAtacaValiosa)) {
                int valorFichaMasValiosa = valorFichaMasValiosaAtacadaEnElTablero(tableroM);
                if (estaLaFichaDefendidaPorA(tableroM, posicionFichaQueAtacaValiosa) == false) {
                    return valorFichaMasValiosa * 3;
                } else {
                    if (fichaQueAtacaValiosaEsMayorOigualQueLaQueLaAtaca(tableroM, yInicial, xInicial, yFinal, xFinal)) {
                        return valorFichaMasValiosa * 2;
                    }
                }
            }
        }
        return 0;
    }

    ///nuevo dia programando 
    //evalúa la relación de valor entre la ficha que ataca y la ficha que defiende, y devuelve true si la ficha que ataca es igual o más valiosa que la ficha que defiende, y false en caso contrario.
    private boolean fichaQueAtacaValiosaEsMayorOigualQueLaQueLaAtaca(String[][] tableroM, int yInicial, int xInicial, int yFinal, int xFinal) {
        String fichaQueAtacaMasValiosa = tableroM[yFinal][xFinal];
        String fichaQueLaAtaca = tableroM[yInicial][xInicial];

        int valorAtacaMasValiosa = valorDeLaFicha(fichaQueAtacaMasValiosa);
        int valorQueLaAtaca = valorDeLaFicha(fichaQueLaAtaca);

        return (valorAtacaMasValiosa >= valorQueLaAtaca) ? true : false;

    }

    /////
    ///busca una ficha blanca que pueda defender una posición dada en el tablero, 
    ///verificando si algún movimiento de ataque posible de esa ficha coincide con la posición dada.
    private String buscarFichaQueLaDefiende(String[][] tableroM, String posicion) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (comprobarSiLaFichaEsBlanca(tableroM, i, j)) {
                    String[] movimientosAtaque = movimientos.movimientoAtaqueFichaA(tableroM, i, j);
                    for (int k = 0; k < movimientosAtaque.length; k++) {
                        if (movimientosAtaque[k].equals(posicion)) {
                            return tableroM[i][j];
                        }
                    }
                }
            }
        }
        return null;
    }

    ///verifica si la posición de ataque coincide exactamente con la posición dada y devuelve el resultado como un booleano.
    private boolean comprobarSiLaPosicionDeAtaqueCoincideConLaQueAtaca(int yFinal, int xFinal, String posAtaque) {
        String posicionAtaca = "" + yFinal + "" + xFinal;
        return (posicionAtaca.equals(posAtaque)) ? true : false;
    }

    /////
    ///busca todos los movimientos posibles de las fichas del oponente y calcula el valor de la ficha más valiosa que puede ser atacada. Luego, devuelve este valor como resultado.
    private int valorFichaMasValiosaAtacadaEnElTablero(String[][] tableroM) {
        String[] movimientosFichasA = movimientos.todosLosMovimientosFichasA(tableroM);
        int valorMayor = 0;
        if (!movimientosFichasA[0].equals("")) {
            for (int i = 0; i < movimientosFichasA.length; i++) {
                int y = Integer.parseInt(String.valueOf(movimientosFichasA[i].charAt(0)));
                int x = Integer.parseInt(String.valueOf(movimientosFichasA[i].charAt(1)));
                if (comprobarSiLaFichaEsnegra(tableroM, y, x)) {
                    String ficha = tableroM[y][x];
                    int valorTemporal = valorDeLaFicha(ficha);
                    if (valorTemporal > valorMayor) {
                        valorMayor = valorTemporal;
                    }

                }

            }
        }
        return valorMayor;
    }

    private String posicionFichaQueAtacaAlaMasValiosa(String[][] tableroM) {
        String posicionAtacanteMasValiosa = "ninguna";
        int valorMayor = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (comprobarSiLaFichaEsBlanca(tableroM, i, j)) {
                    String[] movimientosFichaBlanca = movimientos.movimientoAtaqueFichaA(tableroM, i, j);
                    if (!movimientosFichaBlanca[0].equals("")) {
                        for (int k = 0; k < movimientosFichaBlanca.length; k++) {
                            int y = Integer.parseInt(String.valueOf(movimientosFichaBlanca[k].charAt(0)));
                            int x = Integer.parseInt(String.valueOf(movimientosFichaBlanca[k].charAt(1)));
                            if (comprobarSiLaFichaEsnegra(tableroM, y, x)) {
                                String ficha = tableroM[y][x];
                                int valorTemporal = valorDeLaFicha(ficha);
                                if (valorTemporal > valorMayor) {
                                    posicionAtacanteMasValiosa = "" + i + "" + j;
                                    valorMayor = valorTemporal;
                                }

                            }
                        }
                    }

                }
            }
        }
        return posicionAtacanteMasValiosa;
    }

    ////busca la posición de la ficha blanca que puede atacar a la ficha negra más valiosa en el tablero y devuelve esta posición como resultado.
    private boolean jaqueMateHaciaEl(String[][] tableroM) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String posicion = "" + i + "" + j;
                if (comprobarSiLaFichaEsBlanca(tableroM, i, j)) {
                    String[] movimientosBlancos = movimientos.movimientosAmodificados(tableroM, posicion);
                    if (!movimientosBlancos[0].equals("")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    ///////////busca el rey propio en el tablero y verifica si está en una posición donde puede ser atacado por las fichas del oponente. 
    private boolean miReyEnJaque(String[][] tableroM) {
        //Buscamos primero al rey y luego vemos todas los movimientos posibles de A
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tableroM[i][j].equals("B_rey")) {
                    String posicionRey = "" + i + "" + j;
                    String[] movimientosBlancas = movimientos.todosLosMovimientosFichasA(tableroM);
                    for (int k = 0; k < movimientosBlancas.length; k++) {
                        if (posicionRey.equals(movimientosBlancas[k])) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    ////realiza una copia exacta del tablero original en otro tablero, preservando la configuración y contenido del tablero original.
    private void copiarTablero(String[][] tableroM) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tableroM[i][j] = tablero[i][j];
            }
        }
    }

    ////realiza una copia exacta del tablero original en otro tablero, preservando la configuración y contenido del tablero original.
    private void copiarTablero(String[][] tableroOriginal, String[][] tableroCopia) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tableroCopia[i][j] = tableroOriginal[i][j];
            }
        }
    }

    /////
    /////este método evalúa si una casilla está en riesgo de ser atacada por las fichas del oponente. 
    /////Si la casilla es un objetivo potencial de ataque, se asigna una puntuación negativa; de lo contrario, 
    //////se asigna una puntuación positiva para indicar que la casilla es segura.
    private int puntuacionCasillaSegura(int y, int x) {
        String posicionFinal = "" + y + "" + x;
        String movimientosAtaqueA[] = movimientos.todosLosMovimientosFichasA(tablero);
        for (int i = 0; i < movimientosAtaqueA.length; i++) {
            if (posicionFinal.equals(movimientosAtaqueA[i])) {
                return -8;
            }
        }
        return 3;
    }

    ////En resumen, este método evalúa si una ficha está defendiendo otras fichas en el tablero y asigna una puntuación en función del valor de las fichas defendidas.
    private int puntuacionSiEstaDefendiendo(int y, int x) {
        int puntuacion = 0;
        String[] movimientosDefensora = movimientos.movimientoAtaqueFichaB(tablero, y, x);
        for (int i = 0; i < movimientosDefensora.length; i++) {
            int ejeY = Integer.parseInt(String.valueOf(movimientosDefensora[i].charAt(0)));
            int ejeX = Integer.parseInt(String.valueOf(movimientosDefensora[i].charAt(1)));
            String fichaDefendida = tablero[ejeY][ejeX];
            if (!fichaDefendida.equals("")) {
                if (fichaDefendida.charAt(0) == 'B') {
                    puntuacion -= valorDeLaFicha(fichaDefendida) / 4;
                }
            }
        }
        return puntuacion;
    }

    
    ////verifica y evalua diferentes condiciones basadas en las fichas involucradas en el ataque y devuelve una puntuación correspondiente en función de estas condiciones.
    private int puntuacionEnCasoDeFichaAtaque(int yInicial, int xInicial, int yFinal, int xFinal) {
        String fichaInicial = fichaDeLaCasilla(tablero, yInicial, xInicial);
        if (comprobarSiLaFichaEsBlanca(tablero, yFinal, xFinal)) {

            String fichaFinal = fichaDeLaCasilla(tablero, yFinal, xFinal);

            String posicionFinal = "" + yFinal + "" + xFinal;

            if (estaLaFichaDefendidaPorA(tablero, posicionFinal) == false) {
                return valorDeLaFicha(fichaFinal) * 3;
            } else if (valeMasLaPrimera(fichaFinal, fichaInicial)) {
                return valorDeLaFicha(fichaFinal) * 3;
            } else if (valenIgual(fichaFinal, fichaFinal)) {
                return valorDeLaFicha(fichaFinal) * 2;
            } else {
                return 0;
            }
        }

        if (fichaInicial.equals("B_rey")) {
            return -10;
        }

        return 0;
    }

    ///extrae y devuelve la versión sintetizada de una ficha dada, eliminando el prefijo que identifica el color de la misma.
    private String devolverFichaSintetizada(String ficha) {
        String[] array = ficha.split("_");
        return array[1];
    }

    /////Examina las casillas que están siendo atacadas por las fichas blancas al inicio del juego y ajusta las puntuaciones 
    /////de las casillas atacadas en función del valor de las fichas involucradas y su defensa.
    private void examinarLasCasillasAtacadasAlInicio() {
        ajustarPuntuacion0();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (comprobarSiLaFichaEsBlanca(tablero, i, j)) {
                    String posicion = "" + i + "" + j;
                    String fichaAtacante = tablero[i][j];
                    String[] movimientosPosibles = movimientos.movimientosFichas(tablero, posicion);
                    try {
                        for (int k = 0; k < movimientosPosibles.length; k++) {
                            int yM = Character.getNumericValue(movimientosPosibles[k].charAt(0));
                            int xM = Character.getNumericValue(movimientosPosibles[k].charAt(1));
                            String fichaAtacada = tablero[yM][xM];
                            if (comprobarSiLaFichaEsnegra(tablero, yM, xM)) {
                                String posicionFichaAtacada = "" + yM + "" + xM;
                                if (valeMasLaPrimera(fichaAtacada, fichaAtacante)) {
                                    puntuacionesPosiciones[yM][xM] += valorDeLaFichaSinRey(fichaAtacada);
                                } else if (estaLaFichaDefendidaPorB(tablero, posicionFichaAtacada) == false) {
                                    puntuacionesPosiciones[yM][xM] += valorDeLaFichaSinRey(fichaAtacada);
                                }
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    //////comprueba si una ficha en una posición dada está siendo defendida por alguna ficha del jugador A (blancas)
    /////devolviendo true si está defendida y false si no lo está.
    private boolean estaLaFichaDefendidaPorA(String[][] tableroM, String posicionFicha) {
        String[] movimientosVariables = movimientos.todosLosMovimientosFichasA(tableroM);
        for (int k = 0; k < movimientosVariables.length; k++) {
            if (posicionFicha.equals(movimientosVariables[k])) {
                return true;
            }
        }
        return false;
    }

    ////comprueba si una ficha en una posición dada está siendo defendida por alguna ficha del jugador B (negras), devolviendo true si está defendida y false si no lo está.
    private boolean estaLaFichaDefendidaPorB(String[][] tableroM, String posicionFicha) {
        String[] movimientosVariables = movimientos.todosLosMovimientosFichasB(tableroM);
        for (int k = 0; k < movimientosVariables.length; k++) {
            if (posicionFicha.equals(movimientosVariables[k])) {
                return true;
            }
        }
        return false;
    }

    ///determina si la ficha en una posición dada en el tablero es blanca (jugador A), devolviendo true si lo es y false si no lo es.
    private boolean comprobarSiLaFichaEsBlanca(String[][] tableroM, int y, int x) {
        if (!tableroM[y][x].equals("")) {
            return (tableroM[y][x].charAt(0) == 'A') ? true : false;
        }
        return false;
    }

    ////determina si la ficha en una posición dada en el tablero es negra (jugador B), devolviendo true si lo es y false si no lo es.
    private boolean comprobarSiLaFichaEsnegra(String[][] tableroM, int y, int x) {
        if (!tableroM[y][x].equals("")) {
            return (tableroM[y][x].charAt(0) == 'B') ? true : false;
        }
        return false;
    }

    ////retorna la ficha ubicada en una posición específica del tablero.
    private String fichaDeLaCasilla(String[][] tableroM, int y, int x) {
        String ficha = tableroM[y][x];
        return ficha;
    }

    ///Retorna la ficha ubicada en una posición específica del tablero, dado su formato de posición en forma de cadena.
    private String fichaDeLaCasilla(String[][] tableroM, String posicion) {
        char ejeY = posicion.charAt(0);
        char ejeX = posicion.charAt(1);
        int y = Integer.parseInt(String.valueOf(ejeY));
        int x = Integer.parseInt(String.valueOf(ejeX));
        String ficha = tableroM[y][x];
        return ficha;
    }

    ///establece todas las puntuaciones de las posiciones del tablero a cero.
    private void ajustarPuntuacion0() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                puntuacionesPosiciones[i][j] = 0;
            }
        }
    }

    ////ompara el valor de dos fichas y devuelve true si la primera ficha tiene un valor mayor que la segunda ficha.
    private boolean valeMasLaPrimera(String ficha1, String ficha2) {
        String[] ficha1Split = ficha1.split("_");
        String[] ficha2Split = ficha2.split("_");

        int valor1 = valorDeLaFicha(ficha1Split[1]);
        int valor2 = valorDeLaFicha(ficha2Split[1]);

        return (valor1 > valor2) ? true : false;
    }

    ///compara el valor de dos fichas y devuelve true si ambas fichas tienen el mismo valor.
    private boolean valenIgual(String ficha1, String ficha2) {
        String[] ficha1Split = ficha1.split("_");
        String[] ficha2Split = ficha2.split("_");

        int valor1 = valorDeLaFicha(ficha1Split[1]);
        int valor2 = valorDeLaFicha(ficha2Split[1]);

        return (valor1 == valor2) ? true : false;
    }

    ////devuelve un valor numérico que representa la importancia relativa de una ficha en el juego.
    private int valorDeLaFicha(String ficha) {
        String ficha1 = ficha;
        if (ficha.charAt(0) == 'A' || ficha.charAt(0) == 'B') {
            ficha1 = devolverFichaSintetizada(ficha);
        }

        int valor = 0;
        if (ficha1.equals("peon")) {
            valor = 1;
        } else if (ficha1.equals("alfil")) {
            valor = 8;
        } else if (ficha1.equals("torre")) {
            valor = 12;
        } else if (ficha1.equals("caballo")) {
            valor = 12;
        } else if (ficha1.equals("reina")) {
            valor = 18;
        } else if (ficha1.equals("rey")) {
            valor = 20;
        }
        return valor;
    }

    ////devuelve un valor numérico que representa la importancia relativa de una ficha en el juego, considerando su tipo
    private int valorDeLaFichaSinRey(String ficha) {
        String ficha1 = ficha;
        if (ficha.charAt(0) == 'A' || ficha.charAt(0) == 'B') {
            ficha1 = devolverFichaSintetizada(ficha);
        }

        int valor = 0;
        if (ficha1.equals("peon")) {
            valor = 1;
        } else if (ficha1.equals("alfil")) {
            valor = 8;
        } else if (ficha1.equals("torre")) {
            valor = 12;
        } else if (ficha1.equals("caballo")) {
            valor = 12;
        } else if (ficha1.equals("reina")) {
            valor = 18;
        }
        return valor;
    }

}
