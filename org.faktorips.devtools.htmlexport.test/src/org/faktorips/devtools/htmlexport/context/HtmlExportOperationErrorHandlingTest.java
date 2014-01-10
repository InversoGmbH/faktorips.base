/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.TestUtil;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.junit.Test;

public class HtmlExportOperationErrorHandlingTest extends AbstractHtmlExportPluginTest {

    public class MockIpsSrcFile extends IpsSrcFile {

        public MockIpsSrcFile(IIpsElement parent, String name) {
            super(parent, name);
        }

        @Override
        public IpsObjectType getIpsObjectType() {
            return IpsObjectType.POLICY_CMPT_TYPE;
        }
    }

    @Test
    public void testSrcFileWithoutIpsObject() throws IOException {
        context = new DocumentationContext() {
            private boolean alreadyAdded = false;

            @Override
            public List<IIpsSrcFile> getDocumentedSourceFiles() {

                List<IIpsSrcFile> documentedSourceFiles = super.getDocumentedSourceFiles();
                if (alreadyAdded) {
                    return documentedSourceFiles;
                }

                IpsSrcFile ipsSrcFile = new MockIpsSrcFile(
                        ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment(), "MichGibtEsNicht."
                                + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension());

                documentedSourceFiles.add(ipsSrcFile);

                alreadyAdded = true;
                return documentedSourceFiles;
            }

        };
        initContext();

        operation = new HtmlExportOperation(context);

        createStandardProjekt();

        context.setPath(zielpfad);
        context.addDocumentorScript(new StandardDocumentorScript(new TestUtil().createMockIoHandler()));

        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        try {
            operation.run(new NullProgressMonitor());
            fail("sollte CoreException werfen");
        } catch (CoreException e) {
            // nix zu tun
        }
    }
}
