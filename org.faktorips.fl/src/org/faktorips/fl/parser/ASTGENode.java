/* Generated By:JJTree: Do not edit this line. ASTGENode.java */

package org.faktorips.fl.parser;

public class ASTGENode extends SimpleNode {
  public ASTGENode(int id) {
    super(id);
  }

  public ASTGENode(FlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(FlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
