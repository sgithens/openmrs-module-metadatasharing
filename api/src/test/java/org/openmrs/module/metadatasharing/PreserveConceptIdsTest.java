/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.metadatasharing;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatasharing.integration.ShareTestHelper;
import org.openmrs.module.metadatasharing.integration.BaseShareTest;
import org.openmrs.module.metadatasharing.wrapper.PackageImporter;
import org.springframework.test.annotation.NotTransactional;

/**
 * Tests the exporting of ids on objects and makes sure the importing server persists them
 *
 */
public class PreserveConceptIdsTest extends BaseShareTest {

	@Override
	public String getInitialDataset() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getImportServerDataset() {
		// adds another concept id 20
		return "MDSCreateTest.xml";
	}
	
	@Test
	@NotTransactional
	public void shouldPreserveConceptIdAcrossServers() throws Exception {
		runShareTest(new ShareTestHelper() {
			
			Integer newConceptId = null;
			
			@Override
			public List<?> prepareExportServer() throws Exception {
				Concept c = new Concept();
				c.addName(new ConceptName("1234567890-1234567890", Locale.US));
				
				Context.getConceptService().saveConcept(c);
				
				// check to make sure the child server uses this one
				newConceptId = c.getConceptId();
				
				return Collections.nCopies(1, c);
			}
			
			@Override
			public void prepareImportServer() throws Exception {
				// sanity check
				Concept c = Context.getConceptService().getConcept(newConceptId);
				Assert.assertNull("uh oh, there is a concept with this id already, check your dataset", c);
				
				GlobalProperty gp = new GlobalProperty(MetadataSharingConsts.GP_PERSIST_IDS_FOR_CLASSES, "org.openmrs.Concept");
				Context.getAdministrationService().saveGlobalProperty(gp);
			}
			
			@Override
			public void runOnImportServerAfterImport() throws Exception {
				Concept c = Context.getConceptService().getConcept(newConceptId);
				
				Assert.assertNotNull("The concept id was not found, so the id wasn't preserved...", c);
			}
		});
	}
	
	@Test
	@NotTransactional
	public void shouldNotPreserveConceptIdAcrossServersIfImportingServerDoesntRequestIt() throws Exception {
		runShareTest(new ShareTestHelper() {
			
			Integer newConceptId = null;
			
			@Override
			public List<?> prepareExportServer() throws Exception {
				
				Concept c = new Concept();
				c.addName(new ConceptName("Test", Locale.US));
				
				Context.getConceptService().saveConcept(c);
				
				// check to make sure the child server uses this one
				newConceptId = c.getConceptId();
				
				return Collections.nCopies(1, c);
			}
			
			@Override
			public void prepareImportServer() throws Exception {
				// sanity check
				Concept c = Context.getConceptService().getConcept(newConceptId);
				Assert.assertNull("uh oh, there is a concept with this id already, check your dataset", c);
				
				// no GP for importing ids for Concepts, so we shouldn't get one
				//GlobalProperty gp = new GlobalProperty(MetadataSharingConsts.GP_PERSIST_IDS_FOR_CLASSES, "org.openmrs.Concept");
				//Context.getAdministrationService().saveGlobalProperty(gp);
			}
			
			@Override
			public void runOnImportServerAfterImport() throws Exception {
				Concept c = Context.getConceptService().getConcept(newConceptId);
				
				Assert.assertNull("The concept was found with the same id, wha happened??", c);
			}
		});
	}
}
