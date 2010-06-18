package org.faktorips.devtools.htmlexport.test.standard;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractFipsDocTest;

public class StandardDocumentorScriptTest extends AbstractFipsDocTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        documentorConfig.addDocumentorScript(new StandardDocumentorScript());
        documentorConfig.setLinkedIpsObjectTypes(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());
    }

    public void testWriteWithoutException() throws Exception {
        deletePreviousGeneratedFiles();

        createMassivProjekt();
        operation.run(new NullProgressMonitor());
    }

    /*
     * public void testPaths() { createMassivProjekt(); for (IIpsObject ipsObject :
     * documentorConfig.getLinkedObjects()) {
     * System.out.println("=================================================");
     * System.out.println(ipsObject.getName() + " " + ipsObject.getIpsObjectType());
     * System.out.println(ipsObject.getIpsPackageFragment().getRelativePath());
     * 
     * String upPath = HtmlPathUtil.getPathToRoot(ipsObject); System.out.println(upPath); } }
     */
}
