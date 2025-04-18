package lib;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Set;

public class ASTVisitor extends VoidVisitorAdapter<Set<String>> {

    @Override
    public void visit(ClassOrInterfaceType n, Set<String> arg) {
        super.visit(n, arg);
        arg.add(n.getNameAsString());
    }
}
