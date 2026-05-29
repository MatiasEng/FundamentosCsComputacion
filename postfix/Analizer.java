package postfix;
import postfix.parser.*;
import postfix.lexer.*;
import postfix.node.*;
import java.io.*;


public class Analizer {
    public static void main(String[] args ){
        try{
            System.out.println("Inrese la expresion aritmetica");
            //crear una instancia del parser
            Parser p= new Parser(new Lexer(new PushbackReader(new InputStreamReader (System.in),1024)));
            //genera el arbol de parsing
            Start tree = p.parse();
            tree.apply(new ASTDisplay());
            tree.apply(new ASTPrinter());
            tree.apply(new Translation());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
