package lib;

import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Set;

public class ClassOrInterfaceVisitor extends VoidVisitorAdapter<Set<String>> {

    @Override
    public void visit(ClassOrInterfaceType n, Set<String> arg) {
        super.visit(n, arg);
        arg.add(n.getNameAsString());
    }

    @Override
    public void visit(ObjectCreationExpr n, Set<String> arg) {
        super.visit(n, arg);
        n.getType().ifClassOrInterfaceType(c -> arg.add(c.getNameAsString()));
    }
}
