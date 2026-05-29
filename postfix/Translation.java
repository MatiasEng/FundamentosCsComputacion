package postfix;

import postfix.analysis.*;
import postfix.node.*;

public class Translation extends DepthFirstAdapter {

    // Para números simples
    public void caseTNumber(TNumber node) {
        System.out.print(node);
    }

    // Para números dentro de paréntesis (como en: (45+36/2))
    public void outANumberTerm(ANumberTerm node) {
        // No imprime nada extra, solo el número ya fue impreso por caseTNumber
    }

    // Para expresiones entre paréntesis
    public void outAExprTerm(AExprTerm node) {
        // Los paréntesis no se imprimen en notación posfija
        // Solo procesamos la expresión interna
    }

    // Operadores de suma y resta (nivel expr)
    public void outAPlusExpr(APlusExpr node) {
        System.out.print(node.getPlus());
    }

    public void outAMinusExpr(AMinusExpr node) {
        System.out.print(node.getMinus());
    }

    // Operadores de multiplicación, división y módulo (nivel factor)
    public void outAMultFactor(AMultFactor node) {
        System.out.print(node.getMult());
    }

    public void outADivFactor(ADivFactor node) {
        System.out.print(node.getDiv());
    }

    public void outAModFactor(AModFactor node) {
        System.out.print(node.getMod());
    }

    // Estos métodos aseguran que se procesen los nodos intermedios
    public void outAFactorExpr(AFactorExpr node) {
        // Nodo intermedio, no imprime nada
    }

    public void outATermFactor(ATermFactor node) {
        // Nodo intermedio, no imprime nada
    }
}