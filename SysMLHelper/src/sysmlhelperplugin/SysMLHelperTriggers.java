package sysmlhelperplugin;

import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import requirementsanalysisplugin.NestedActivityDiagram;

import com.telelogic.rhapsody.core.*;

public class SysMLHelperTriggers extends RPApplicationListener {

	public SysMLHelperTriggers(IRPApplication app) {
		Logger.writeLine("SysMLHelperPlugin is Loaded - Listening for Events\n"); 
	}

	public boolean afterAddElement(IRPModelElement modelElement) {
		
		boolean doDefault = false;

		if (modelElement != null && 
			modelElement instanceof IRPDependency && 
			modelElement.getUserDefinedMetaClass().equals("Derive Requirement")){
			
			IRPDependency theDependency = (IRPDependency)modelElement;
			Logger.writeLine(modelElement, "was found");
			
			IRPModelElement theDependent = theDependency.getDependent();
			
			if (theDependent instanceof IRPRequirement){
				
				IRPStereotype theExistingGatewayStereotype = getStereotypeAppliedTo(theDependent, "from.*");
				
				if (theExistingGatewayStereotype != null){
					
					modelElement.setStereotype(theExistingGatewayStereotype);
					modelElement.changeTo("Derive Requirement");
				}			
			}
		}

		return doDefault;
	}
	
	public static IRPStereotype getStereotypeAppliedTo(IRPModelElement theElement, String thatMatchesRegEx){
		
		IRPStereotype foundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStereotype> theStereotypes = theElement.getStereotypes().toList();
		
		int count=0;
		
		for (IRPStereotype theStereotype : theStereotypes) {
			
			count++;
			
			String theName = theStereotype.getName();
			
			if (theName.matches(thatMatchesRegEx)){
				foundStereotype = theStereotype;
				
				if (count > 1){
					Logger.writeLine("Error in getStereotypeAppliedTo related to " + Logger.elementInfo(theElement) + " count=" + count);
				}
			}		
		}
		
		return foundStereotype;
	}
	
	@Override
	public boolean afterProjectClose(String bstrProjectName) {
		return false;
	}

	@Override
	public boolean onDoubleClick(IRPModelElement pModelElement) {
		
		boolean theReturn = false;
		
		try {	
			Set<IRPDiagram> allDiagrams = new HashSet<IRPDiagram>();
			
			Set<IRPDiagram> theHyperLinkedDiagrams = getHyperLinkDiagramsFor(pModelElement);	
			allDiagrams.addAll(theHyperLinkedDiagrams);
			
			Set<IRPDiagram> theNestedDiagrams = getNestedDiagramsFor(pModelElement);
			allDiagrams.addAll(theNestedDiagrams);
			
			List<IRPModelElement> optionsList = new ArrayList<IRPModelElement>();
			optionsList.addAll( allDiagrams );
			
			int numberOfDiagrams = optionsList.size();
			
			if (numberOfDiagrams==0){
				
				if (pModelElement instanceof IRPUseCase){
				
					boolean theAnswer = UserInterfaceHelpers.askAQuestion(
							"This use case has no nested Activity Diagram. Do you want to create one?");
						
					if (theAnswer==true){
						Logger.writeLine("User chose to create a new activity diagram");
						NestedActivityDiagram.createNestedActivityDiagram( (IRPUseCase)pModelElement );
					}
						
					theReturn = true; // don't launch the Features  window	
					
				} else {
					theReturn = false; // do default, i.e. open the features dialog
				}	
				
			} else if (numberOfDiagrams==1){
				
				IRPDiagram theDiagramToOpen = (IRPDiagram) optionsList.get(0);
				
				if (theDiagramToOpen != null){

					String theType = theDiagramToOpen.getUserDefinedMetaClass();
					String theName = theDiagramToOpen.getName();
					
					if (theDiagramToOpen instanceof IRPFlowchart){
					
						theDiagramToOpen = (IRPDiagram) theDiagramToOpen.getOwner();

						
					} else if (theDiagramToOpen instanceof IRPActivityDiagram){
						
						theType = theDiagramToOpen.getOwner().getUserDefinedMetaClass();
						theName = theDiagramToOpen.getOwner().getName();	
					}
					
					boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"The " + pModelElement.getUserDefinedMetaClass() + " called '" +
						pModelElement.getName() + "' has an associated " + theType + " called '" + theName + "'.\n" +
						"Do you want to open it?");
					
					if (theAnswer==true){
						
						theDiagramToOpen.openDiagram();	
						theReturn = true; // don't launch the Features  window
						
					} else {
						theReturn = false; // open the features dialog
					}
				}
				
			} else if (numberOfDiagrams>1){
				
				IRPModelElement theSelection = UserInterfaceHelpers.launchDialogToSelectElement(
						optionsList, 
						"The " + pModelElement.getUserDefinedMetaClass() + " called '" +
						pModelElement.getName() + "' has " + numberOfDiagrams + " associated diagrams.\n" +
						"Which one do you want to open?",
						true);
				
				if (theSelection != null && theSelection instanceof IRPDiagram){

					IRPDiagram theDiagramToOpen = (IRPDiagram) theSelection;
					theDiagramToOpen.openDiagram();
					theReturn = true; // don't launch the Features  window
				}
			}

		} catch (Exception e) {
			Logger.writeLine("Unhandled exception in onDoubleClick()");			
		}
		
		return theReturn;
	}

	private static Set<IRPDiagram> getHyperLinkDiagramsFor(IRPModelElement theElement){
		
		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();
		
		@SuppressWarnings("unchecked")
		List<IRPHyperLink> theHyperLinks = theElement.getHyperLinks().toList();
		
		for (IRPHyperLink theHyperLink : theHyperLinks) {
			
			IRPModelElement theTarget = theHyperLink.getTarget();
			
			if (theTarget != null){
				
				if (theTarget instanceof IRPDiagram){
					theDiagrams.add( (IRPDiagram) theTarget );
					
				} else if (theTarget instanceof IRPFlowchart){
					IRPFlowchart theFlowchart = (IRPFlowchart)theTarget;
					theDiagrams.add( theFlowchart.getStatechartDiagram() );
					
				} else if (theTarget instanceof IRPStatechart){
					IRPStatechart theStatechart = (IRPStatechart)theTarget;
					theDiagrams.add( theStatechart.getStatechartDiagram() );			
				}
			}
		}
		
		return theDiagrams;
	}
	
	private static Set<IRPDiagram> getNestedDiagramsFor(IRPModelElement pModelElement) {
		
		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theNestedElements = pModelElement.getNestedElementsRecursive().toList();
		
		for (IRPModelElement theNestedElement : theNestedElements) {
			
			if (theNestedElement instanceof IRPDiagram){
				theDiagrams.add( (IRPDiagram) theNestedElement );
			}
		}

		return theDiagrams;
	}

	@Override
	public boolean onFeaturesOpen(IRPModelElement pModelElement) {
		return false;
	}

	@Override
	public boolean onSelectionChanged() {
		return false;
	}

	@Override
	public boolean beforeProjectClose(IRPProject pProject) {
		return false;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public boolean onDiagramOpen(IRPDiagram pDiagram) {
		return false;
	}
	
}

/**
 * Copyright (C) 2016  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #001 31-MAR-2016: Added ListenForRhapsodyTriggers (F.J.Chadburn)
    #003 09-APR-2016: Added double-click UC to open ACT (F.J.Chadburn)
    #004 10-APR-2016: Re-factored projects into single workspace (F.J.Chadburn)
    #017 11-MAY-2016: Double-click now works with both nested and hyper-linked diagrams (F.J.Chadburn)
    
    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
*/
