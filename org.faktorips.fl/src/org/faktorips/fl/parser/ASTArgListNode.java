/* Generated By:JJTree: Do not edit this line. ASTArgListNode.java */

package org.faktorips.fl.parser;

public class ASTArgListNode extends SimpleNode {
  public ASTArgListNode(int id) {
    super(id);
  }

  public ASTArgListNode(FlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(FlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
