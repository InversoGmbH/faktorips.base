/* Generated By:JJTree: Do not edit this line. ASTEQNode.java */

package org.faktorips.fl.parser;

public class ASTEQNode extends SimpleNode {
  public ASTEQNode(int id) {
    super(id);
  }

  public ASTEQNode(FlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(FlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
